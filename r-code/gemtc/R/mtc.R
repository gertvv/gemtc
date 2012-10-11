library('coda')
library('igraph')

## mtc.network class methods
print.mtc.network <- function(x, ...) {
  cat("MTC dataset: ", x$description, "\n", sep="")
}

summary.mtc.network <- function(model, ...) {
  x
}

plot.mtc.network <- function(model, ...) {
  plot(mtc.graph(model), layout=layout.spring)
}

mtc.graph <- function(model) { 
  connections <- sapply(mtc.parameters(model$j.model), function(x) { unlist(strsplit(x, '\\.')) } )[-1,]
  treatments <-  unique(as.vector(connections))

  g <- graph.empty()
  g <- g + vertex(treatments, label=treatments)
  g <- g + edges(as.vector(connections))
  g
}

relative.effect <- function(graph, t1, t2) { 
	p <- get.shortest.paths(as.undirected(g), t1, t2)[[1]]
	p <- matrix(c(p[1:length(p)-1], p[-1]), ncol=2)
	edgeFn <- apply(p, 1, function(row) {
		f <- g[row[1], row[2]]
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
