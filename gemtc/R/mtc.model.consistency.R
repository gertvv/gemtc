# Master function to generate consistency models
mtc.model.consistency <- function(model) {
    style.tree <- function(tree) {
        tree <- set.edge.attribute(tree, 'arrow.mode', value=2)
        tree <- set.edge.attribute(tree, 'color', value='black')
        tree <- set.edge.attribute(tree, 'lty', value=1)
        tree
    }
	model$tree <-
		style.tree(minimum.diameter.spanning.tree(mtc.network.graph(model$network)))

    model$code <- mtc.model.code(model)
    model$data <- mtc.model.data(model)
    model$inits <- mtc.init(model)
    class(model) <- "mtc.model"

    model
}
