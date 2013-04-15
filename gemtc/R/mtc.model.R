## mtc.model class methods

mtc.model <- function(network, type="consistency",
		factor=2.5, n.chain=4,
		likelihood=NULL, link=NULL, ...) {
    typeMap <- c(
        'Consistency'='consistency',
        'consistency'='consistency',
        'cons'='consistency')

    if (is.na(typeMap[type])) {
        stop(paste(type, 'is not an MTC model type.'))
    }
    type <- typeMap[type]

	model <- list(
        type = type,
        network = network,
        n.chain = n.chain,
        var.scale = factor)

	model$likelihood <- likelihood
	model$link <- link
	if (!is.null(network[['data']]) && 'responders' %in% colnames(network[['data']])) {
		if (is.null(likelihood)) {
			model$likelihood = 'binom'
		}
		if (is.null(link)) {
			model$link = 'logit'
		}
	} else if (!is.null(network[['data']]) && 'mean' %in% colnames(network[['data']])) {
		if (is.null(likelihood)) {
			model$likelihood = 'normal'
		}
		if (is.null(link)) {
			model$link = 'identity'
		}
	} else {
		if (is.null(likelihood)) {
			warning('Likelihood can not be inferred. Defaulting to normal.')
			model$likelihood = 'normal'
		}
		if (is.null(link)) {
			warning('Link can not be inferred. Defaulting to identity.')
			model$link = 'identity'
		}
	}
	if (!ll.defined(model)) {
		stop(paste('likelihood = ', model$likelihood,
			', link = ', model$link, ' not found!', sep=''))
	}

    model$om.scale <- guess.scale(model)

    if (type == 'consistency') {
        mtc.model.consistency(model, ...)
    }
}

print.mtc.model <- function(x, ...) {
    cat("MTC ", x$type, " model: ", x$description, "\n", sep="")
}

summary.mtc.model <- function(object, ...) {
    list("Description"=paste("MTC ", object$type, " model: ", object$description, sep=""), 
         "Parameters"=mtc.basic.parameters(object))
}

plot.mtc.model <- function(x, layout=igraph::layout.circle, ...) {
    igraph::plot.igraph(mtc.model.graph(x), layout=layout, ...)
}

mtc.model.graph <- function(model) { 
    if (tolower(model$type) == 'consistency') {
        comparisons <- mtc.comparisons(model$network)
        g <- model$tree
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

mtc.basic.parameters <- function(model) {
    tree <- model$tree
    sapply(E(tree), function(e) {
        v <- get.edge(tree, e)
        paste("d", V(tree)[v[1]]$name, V(tree)[v[2]]$name, sep=".")
    })
}
