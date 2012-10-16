library('coda')
library('igraph')

## mtc.network class methods
print.mtc.network <- function(network, ...) {
	cat("MTC dataset: ", network$description, "\n", sep="")
}

summary.mtc.network <- function(network, ...) {
	network
}

plot.mtc.network <- function(network, ...) {
	plot(mtc.network.graph(network), ...)
}

## mtc.model class methods

print.mtc.model <- function(model, ...) {
	cat("MTC ", model$type, " model: ", model$description, "\n", sep="")
}

summary.mtc.model <- function(model, ...) {
	model
}

plot.mtc.model <- function(model, ...) {
	plot(mtc.model.graph(model), ...)
}

## mtc.result class methods

print.mtc.result <- function(result, ...) {
	cat("MTC ", result$model$type, " results: ", result$model$description, "\n", sep="")
}

summary.mtc.result <- function(result, ...) {
	summary(result$samples)
}

plot.mtc.result <- function(result, ...) {
	plot(result$samples)
}

####

mtc.network.graph <- function(network) {
	comparisons <- mtc.network.comparisons(network)
	treatments <- as.character(network$treatments$id)

	g <- graph.empty()
	g <- g + vertex(treatments, label=treatments)
	g <- g + edges(as.vector(comparisons), arrow.mode=0)
	g
}

mtc.spanning.tree <- function(model) {
	parameters <- sapply(mtc.parameters(model$j.model), function(x) { unlist(strsplit(x, '\\.')) } )[-1,]
	treatments <- unique(as.vector(parameters))

	g <- graph.empty()
	g <- g + vertex(treatments, label=treatments)
	g <- g + edges(as.vector(parameters), arrow.mode=2, color=1)
	g
}

mtc.model.graph <- function(model) { 
	comparisons <- mtc.model.comparisons(model)
	g <- mtc.spanning.tree(model)
	comparisons <- unlist(
		apply(comparisons, 2,
			function(x) { if (are.connected(g, x[1], x[2]) || are.connected(g, x[2], x[1])) c() else x }))
	g <- g + edges(as.vector(comparisons), arrow.mode=0, color=2)
}

relative.effect <- function(g, t1, t2) { 
	if (t1 == t2) {
		return(function(m) {
			mcmc(rep(0, times=(end(m) - start(m) + 1)/thin(m)),
					start=start(m), end=end(m), thin=thin(m))
		})
	}
	p <- get.shortest.paths(as.undirected(g), t1, t2)[[1]]
	p <- matrix(c(p[1:length(p)-1], p[-1]), ncol=2)
	edgeFn <- apply(p, 1, function(row) {
		f <- are.connected(g, row[1], row[2])
		v1 <- if (f) row[1] else row[2]
		v2 <- if (f) row[2] else row[1]
		f <- f * 2 - 1
		edgeLabel <- paste('d', V(g)[v1]$label, V(g)[v2]$label, sep='.')
		function(m) { f * m[,edgeLabel] }
	})
	function(m) {
		data <- apply(sapply(edgeFn, function(fn) { fn(m) }), 1, sum)
		mcmc(data, start=start(m) , end=end(m) , thin=thin(m))
	}
}

mtc.relative.effect <- function(data, g, t1, t2) { 
	as.mcmc.list(lapply(data, relative.effect(g, t1, t2)))
}

rank.probability <- function(data, model) { 
	treatments <- as.vector(mtc.treatments(model$j.network)$id)
	mtcGraph <- mtc.spanning.tree(model)

	n.alt <- length(treatments)

	# count ranks given a matrix d of relative effects (treatments as rows)
	rank.count <- function(d) {
		n.iter <- dim(d)[2]
		.C("rank_count",
			as.double(d), as.integer(n.iter), as.integer(n.alt),
			counts=matrix(0.0, nrow=n.alt, ncol=n.alt),
			NAOK=FALSE, DUP=FALSE, PACKAGE="gemtc")$counts
	}

	d <- lapply(treatments, function(x) { mtc.relative.effect(data, mtcGraph, treatments[1], x) })
	counts <- lapply(1:nchain(data), function(chain) { rank.count(do.call(rbind, lapply(d, function(x) { x[[chain]] }))) })
	ranks <- Reduce(function(a, b) { a + b}, counts)
	colnames(ranks) <- treatments

	n.iter <- nchain(data) * (end(data) - start(data) + 1) / thin(data)

	ranks / n.iter
}
