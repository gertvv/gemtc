# Consistency model
mtc.model.consistency <- function(model) {
  style.tree <- function(tree) {
    tree <- set.edge.attribute(tree, 'arrow.mode', value=2)
    tree <- set.edge.attribute(tree, 'color', value='black')
    tree <- set.edge.attribute(tree, 'lty', value=1)
    tree
  }
  model[['tree']] <-
    style.tree(minimum.diameter.spanning.tree(mtc.network.graph(model[['network']])))

  model[['data']] <- mtc.model.data(model)
  model[['inits']] <- mtc.init(model)

  model[['code']] <- mtc.model.code(model, mtc.basic.parameters(model), consistency.relative.effect.matrix(model))

  monitors <- inits.to.monitors(model[['inits']][[1]])
  model[['monitors']] <- list(
    available=monitors,
    enabled=c(monitors[grep('^d\\.', monitors)], monitors[grep('^sd.d$', monitors)])
  )

  class(model) <- "mtc.model"

  model
}

mtc.model.name.consistency <- function(model) {
  "consistency"
}

func.param.matrix.consistency <- function(model, t1, t2) {
  tree.relative.effect(model[['tree']], t1, t2)
}

consistency.relative.effect.matrix <- function(model) {
  # Generate list of linear expressions
  params <- mtc.basic.parameters(model)
  tree <- model[['tree']]
  re <- tree.relative.effect(tree, V(tree)[1], t2=NULL)
  expr <- apply(re, 2, function(col) { paste(sapply(which(col != 0), function(i) {
    paste(if (col[i] == -1) "-" else "", params[i], sep="")
  }), collapse = " + ") })
  expr <- sapply(1:length(expr), function(i) { paste('d[1, ', i + 1, '] <- ', expr[i], sep='') })
  expr <- c('d[1, 1] <- 0', expr, 'for (i in 2:nt) {\n\tfor (j in 1:nt) {\n\t\td[i, j] <- d[1, j] - d[1, i]\n\t}\n}')
  paste(expr, collapse="\n")
}
