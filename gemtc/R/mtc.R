library('coda')
library('igraph')

## mtc.network class methods
print.mtc.network <- function(x, ...) {
	cat("MTC dataset: ", x$description, "\n", sep="")
}

summary.mtc.network <- function(object, ...) {
	object
}

plot.mtc.network <- function(x, ...) {
	plot(mtc.network.graph(x), ...)
}

## mtc.model class methods

print.mtc.model <- function(x, ...) {
	cat("MTC ", x$type, " model: ", x$description, "\n", sep="")
}

summary.mtc.model <- function(object, ...) {
	object
}

plot.mtc.model <- function(x, ...) {
	plot(mtc.model.graph(x), ...)
}
## mtc.result class methods

print.mtc.result <- function(x, ...) {
	cat("MTC ", x$model$type, " results: ", x$model$description, "\n", sep="")
}

summary.mtc.result <- function(object, ...) {
	summary(object$samples)
}

plot.mtc.result <- function(x, ...) {
	plot(x$samples)
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
	parameters <- sapply(parameters, function(x) { 
	path <- unlist(strsplit(x, '\\.')) 
	if(criterion(path)) { 
		path[-1]
	}})
	parameters[!sapply(parameters, is.null)]
}

mtc.spanning.tree <- function(parameters) {
	parameters <- unlist(filter.parameters(parameters, function(x) { x[1] == 'd' }))
	treatments <- unique(as.vector(parameters))
	graph.create(treatments, parameters, arrow.mode=2, color=1)
}

graph.create <- function(v, e, ...) {
	g <- graph.empty()
	g <- g + vertex(v, label=v)
	g <- g + edges(as.vector(e), ...)
	g
}

w.factors <- function(parameters) {
	lapply(filter.parameters(parameters, function(x) { x[1] == 'w' }),
	function(x) {
		c(x[length(x)], x[1])
	})
}

mtc.model.graph <- function(model) { 
	comparisons <- mtc.model.comparisons(model)
	parameters <- mtc.parameters(model$j.model)
	g <- mtc.spanning.tree(parameters)
	g <- g + edges(w.factors(parameters), arrow.mode=2, color=2)
	g <- g + edges(as.vector(unlist(non.edges(g, comparisons))), arrow.mode=0, color=3)
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
	} else { 
		if(length(t1) > length(t2)) t2 <- rep(t2, length.out=length(t1))
		if(length(t2) > length(t1)) t1 <- rep(t1, length.out=length(t2))
	}
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
	g <- mtc.spanning.tree(mtc.parameters(result$model$j.model))
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
	as.mcmc.list(lapply(result$samples, function(chain) { 
		mcmc(chain %*% effects, start=start(chain), end=end(chain), thin=thin(chain))
	}))
}

rank.probability <- function(result) {
	model <- result$model
	data <- result$samples

	treatments <- as.vector(mtc.treatments(model$j.network)$id)
	mtcGraph <- mtc.spanning.tree(mtc.parameters(model$j.model))

	n.alt <- length(treatments)

	# count ranks given a matrix d of relative effects (treatments as rows)
	rank.count <- function(d) {
		n.iter <- dim(d)[2]
		.C("rank_count",
			as.double(d), as.integer(n.iter), as.integer(n.alt),
			counts=matrix(0.0, nrow=n.alt, ncol=n.alt),
			NAOK=FALSE, DUP=FALSE, PACKAGE="gemtc")$counts
	}

	d <- relative.effect(result, treatments[1], treatments, preserve.extra=FALSE)
	counts <- lapply(d, function(chain) { rank.count(t(chain)) })
	ranks <- Reduce(function(a, b) { a + b }, counts)
	colnames(ranks) <- treatments

	n.iter <- nchain(data) * (end(data) - start(data) + 1) / thin(data)

	t(ranks / n.iter)
}
