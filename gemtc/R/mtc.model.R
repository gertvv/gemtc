## mtc.model class methods

mtc.model <- function(network, type="Consistency", factor=2.5, n.chain=4) {
    typeMap <- c(
        'Consistency'='Consistency',
        'consistency'='Consistency',
        'cons'='Consistency')

    if (is.na(typeMap[type])) {
        stop(paste(type, 'is not an MTC model type.'))
    }
    type <- typeMap[type]

    if (type == 'Consistency') {
        mtc.model.consistency(network, factor=factor, n.chain=n.chain)
    }
}

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

mtc.model.graph <- function(model) { 
    if (model$type == 'Inconsistency') {
        network <- model$network
        comparisons <- mtc.comparisons(model$network)
        parameters <- mtc.parameters(model)
        g <- mtc.spanning.tree(parameters, network)
        g <- g + edges.create(w.factors(parameters, network), arrow.mode=2, color="black", lty=2)
        g <- g + edges(as.vector(unlist(non.edges(g, comparisons))), arrow.mode=0, lty=1, color="grey")
        g
    } else if (model$type == 'Consistency') {
        comparisons <- mtc.comparisons(model$network)
        parameters <- mtc.basic.parameters(model)
        g <- mtc.spanning.tree(parameters, network)
        g <- g + edges(as.vector(unlist(non.edges(g, comparisons))), arrow.mode=0, lty=1, color="grey")
        g
    }
}

# filters list of comparison by edges that are not yet present in graph g 
non.edges <- function(g, comparisons) { 
    sapply(1:nrow(comparisons), function(i) {
        x <- c(comparisons$t1[i], comparisons$t2[i])
        if (are.connected(g, x[1], x[2]) || are.connected(g, x[2], x[1])) c() else x
    })
}
