# Master function to generate consistency models
mtc.model.consistency <- function(network, factor, n.chain) {
    style.tree <- function(tree) {
        tree <- set.edge.attribute(tree, 'arrow.mode', value=2)
        tree <- set.edge.attribute(tree, 'color', value='black')
        tree <- set.edge.attribute(tree, 'lty', value=1)
        tree
    }

	model <- list(
		type = 'Consistency',
		network = network,
		tree = style.tree(minimum.diameter.spanning.tree(mtc.network.graph(network))),
		n.chain = n.chain,
		var.scale = factor
	)

	if ('responders' %in% colnames(network$data)) {
		model$likelihood = 'binom'
		model$link = 'logit'
	} else if ('mean' %in% colnames(network$data)) {
		model$likelihood = 'normal'
		model$link = 'identity'
	}

	model$om.scale <- guess.scale(model)
	model$data <- mtc.model.data(model)
	model$code <- mtc.model.code(model)
	model$inits <- mtc.init(model)
	class(model) <- "mtc.model"

	model
}
