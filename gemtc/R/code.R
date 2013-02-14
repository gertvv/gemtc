mtc.basic.parameters <- function(model) {
	tree <- model$tree
	sapply(E(tree), function(e) {
		v <- get.edge(tree, e)
		paste("d", V(tree)[v[1]]$name, V(tree)[v[2]]$name, sep=".")
	})
}

mtc.model.consistency <- function(network, factor, n.chain) {
	model <- list(
		type = 'Consistency',
		network = network,
		tree = minimum.diameter.spanning.tree(mtc.network.graph(network)),
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

mtc.model.code <- function(model) {
	fileName <- system.file('gemtc.model.template.txt', package='gemtc')
	template <- readChar(fileName, file.info(fileName)$size)

	lik.code <- do.call(paste("mtc.code.likelihood", model$likelihood, model$link, sep="."), list())
	template <- sub('$likelihood$', lik.code, template, fixed=TRUE)

	network <- model$network
	tree <- model$tree
	re <- tree.relative.effect(tree, V(tree)[1], t2=NULL)
	params <- mtc.basic.parameters(model)

	# Generate list of linear expressions
	expr <- apply(re, 2, function(col) { paste(sapply(which(col != 0), function(i) {
		paste(if (col[i] == -1) "-" else "", params[i], sep="")
	}), collapse = " + ") })
	expr <- sapply(1:length(expr), function(i) { paste('d[', i + 1, '] <- ', expr[i], sep='') })
	expr <- c('d[1] <- 0', expr)
	expr <- paste(expr, collapse="\n")
	template <- sub('$relativeEffectVector$', expr, template, fixed=TRUE)

	# Generate parameter priors
	priors <- paste(params, "~", "dnorm(0, prior.prec)", collapse="\n")
	template <- sub('$relativeEffectPriors$', priors, template, fixed=TRUE)

	template
}
