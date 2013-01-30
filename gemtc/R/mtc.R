## mtc.network class methods
forest <- function (x, ...)  
	UseMethod("forest")

print.mtc.network <- function(x, ...) {
	cat("MTC dataset: ", x$description, "\n", sep="")
	print(x$data)
}

summary.mtc.network <- function(object, ...) {
	studies <- levels(object$data[,1])
	m <- sapply(object$treatments[,1], function(treatment) {
		sapply(studies, function(study) { 
			any(object$data[,1] == study & object$data[,2] == treatment)
		})
	})
	colnames(m) <- object$treatments[,1]
	x <- as.factor(apply(m, 1, sum))
	levels(x) <- sapply(levels(x), function(y) { paste(y, "arm", sep="-") })
	list("Description"=paste("MTC dataset: ", object$description, sep=""),
			 "Studies per treatment"=apply(m, 2, sum), 
			 "Number of n-arm studies"=summary(x)) 
}

plot.mtc.network <- function(x, ...) {
	igraph::plot.igraph(mtc.network.graph(x), ...)
}

## mtc.model class methods
print.mtc.model <- function(x, ...) {
	cat("MTC ", x$type, " model: ", x$description, "\n", sep="")
}

summary.mtc.model <- function(object, ...) {
	list("Description"=paste("MTC ", object$type, " model: ", object$description, sep=""), 
			 "Parameters"=mtc.parameters(object))
}

plot.mtc.model <- function(x, ...) {
	igraph::plot.igraph(mtc.model.graph(x), ...)
}

## mtc.result class methods
print.mtc.result <- function(x, ...) {
	cat("MTC ", x$model$type, " results: ", x$model$description, sep="")
	print(x$samples)
}

summary.mtc.result <- function(object, ...) {
	summary(object$samples)
}

plot.mtc.result <- function(x, ...) {
	plot(x$samples, ...)
}

forest.mtc.result <- function(x, ...) { 
	quantiles <- summary(x)$quantiles 
	stats <- quantiles[-dim(quantiles)[1],]
	if(class(stats) == "numeric") { # Selecting a single row returns a numeric 
		stats <- as.matrix(t(stats))
		row.names(stats) <- row.names(quantiles)[[1]]
	}
	data <- data.frame(id=rownames(stats), pe=stats[,3], ci.l=stats[,1], ci.u=stats[,5], group=NA, style="normal")
	blobbogram(data,
		columns=c(), column.labels=c(),
		id.label="Comparison", ci.label="Odds Ratio (95% CrI)", log.scale=TRUE,
		grouped=FALSE)
}

as.mcmc.list.mtc.result <- function(x, ...) {
	x$samples
}

####
mtc.network.graph <- function(network) {
	comparisons <- mtc.network.comparisons(network)
	treatments <- as.character(network$treatments$id)
	graph.create(treatments, comparisons, arrow.mode=0)
}

filter.parameters <- function(parameters, criterion) { 
	parameters <- lapply(parameters, function(x) { 
	path <- unlist(strsplit(x, '\\.')) 
	if(criterion(path)) { 
		path[-1]
	}})
	parameters[!sapply(parameters, is.null)]
}

mtc.spanning.tree <- function(parameters) {
	parameters <- unlist(filter.parameters(parameters, function(x) { x[1] == 'd' }))
	treatments <- unique(as.vector(parameters))
	graph.create(treatments, parameters, arrow.mode=2, color="black", lty=1)
}

graph.create <- function(v, e, ...) {
	g <- graph.empty()
	g <- g + vertex(v, label=v)
	g <- g + edges(as.vector(e), ...)
	g
}

w.factors <- function(parameters) {
	basic <- do.call(rbind, filter.parameters(parameters, function(x) { x[1] == 'd' }))
	extract.unique <- function(f, basic) {
		f <- c(f, f[1])
		factors <- lapply(1:length(f), function(x, pars) { c(pars[x - 1], pars[x]) }, f)[-1]
		factors <- do.call(rbind, factors)
		apply(factors, 1, function(fac) {
			if(!any(basic[,1]==fac[1] & basic[,2] == fac[2]) &&
				 !any(basic[,2]==fac[1] & basic[,1] == fac[2])) {
				fac
			} else NULL
		})
	}
	w.factors <- filter.parameters(parameters, function(x) { x[1] == 'w' })
	w.factors <- unlist(lapply(w.factors, extract.unique, basic), recursive=FALSE)
	w.factors[!sapply(w.factors, is.null)]
}

mtc.model.graph <- function(model) { 
	comparisons <- mtc.model.comparisons(model)
	parameters <- mtc.parameters(model)
	g <- mtc.spanning.tree(parameters)
	g <- g + edges(w.factors(parameters), arrow.mode=2, color="black", lty=2)
	g <- g + edges(as.vector(unlist(non.edges(g, comparisons))), arrow.mode=0, lty=1, color="grey")
	g
}

# filters list of comparison by edges that are not yet present in graph g 
non.edges <- function(g, comparisons) { 
	apply(comparisons, 2,
		function(x) { if (are.connected(g, x[1], x[2]) || are.connected(g, x[2], x[1])) c() else x })
}

tree.relative.effect <- function(g, t1, t2) {
	if((is.null(t2) || length(t2) == 0) && length(t1) == 1) {
		t2 <- V(g)[V(g)$name != t1]$name
	}
	if(length(t1) > length(t2)) t2 <- rep(t2, length.out=length(t1))
	if(length(t2) > length(t1)) t1 <- rep(t1, length.out=length(t2))
	pairs <- matrix(c(t1, t2), ncol=2)
	paths <- apply(pairs, 1, function(rel) {
		p <- unlist(get.shortest.paths(g, rel[1], rel[2], mode='all'))
		p <- matrix(c(p[1:length(p)-1], p[-1]), ncol=2)
		edges <- sapply(E(g), function(e) {
			v <- get.edge(g, e)
			if (sum(p[,1] == v[1] & p[,2] == v[2])) 1
			else if (sum(p[,1] == v[2] & p[,2] == v[1])) -1
			else 0
		})
	})
	colnames(paths) <-	apply(pairs, 1, function(pair) { 
		paste('d', pair[1], pair[2], sep='.')
	})
	paths
}

relative.effect <- function(result, t1, t2 = c(), preserve.extra=TRUE) {
	if(result$model$type != "Consistency") stop("Cannot apply relative.effect to this model")

	# Build relative effect transformation matrix
	g <- mtc.spanning.tree(mtc.parameters(result))
	effects <- tree.relative.effect(g, t1, t2)

	# Add rows/columns for parameters that are not relative effects
	nOut <- ncol(effects)
	nIn <- nrow(effects)
	nExtra <- ncol(result$samples[[1]]) - nIn
	effects <- rbind(effects, matrix(0, nrow=nExtra, ncol=nOut))
	if (preserve.extra) {
		allNames <- c(colnames(effects), colnames(result$samples[[1]])[nIn+(1:nExtra)])
		effects <- cbind(effects, 
			rbind(matrix(0, nrow=nIn, ncol=nExtra), diag(nExtra)))
		colnames(effects) <- allNames
	}

	# Apply tranformation to each chain
	samples <- as.mcmc.list(lapply(result$samples, function(chain) { 
		mcmc(chain %*% effects, start=start(chain), end=end(chain), thin=thin(chain))
	}))
	effects <- list(
		samples=samples,
		model=result$model,
		sampler=result$model$sampler)

	class(effects) <- "mtc.result"
	effects
}

rank.probability <- function(result) {
	model <- result$model
	data <- result$samples

	treatments <- as.vector(mtc.treatments(model$j.network)$id)
	mtcGraph <- mtc.spanning.tree(mtc.parameters(result))
	n.alt <- length(treatments)

	# count ranks given a matrix d of relative effects (treatments as rows)
	rank.count <- function(d) {
		n.iter <- dim(d)[2]
		.C("rank_count",
			as.double(d), as.integer(n.iter), as.integer(n.alt),
			counts=matrix(0.0, nrow=n.alt, ncol=n.alt),
			NAOK=FALSE, DUP=FALSE, PACKAGE="gemtc")$counts
	}

	d <- relative.effect(result, treatments[1], treatments, preserve.extra=FALSE)$samples
	counts <- lapply(d, function(chain) { rank.count(t(chain)) })
	ranks <- Reduce(function(a, b) { a + b }, counts)
	colnames(ranks) <- treatments

	n.iter <- nchain(data) * (end(data) - start(data) + 1) / thin(data)

	t(ranks / n.iter)
}
