# Node-split model
mtc.model.nodesplit <- function(model, t1, t2) {
  # Rewrite the dataset
  print("Rewriting data...")

  network <- model[['network']]
  data.ab <- nodesplit.rewrite.data.ab(network[['data.ab']], t1, t2)
  data.re <- nodesplit.rewrite.data.re(network[['data.re']], t1, t2)
  print("Constructing network...")
  network <- mtc.network(data.ab=data.ab,
                         data.re=data.re,
                         treatments=model[['network']][['treatments']])
  model[['network']] <- network

  print("Constructing indirect network...")
  print(network)
  has.both <- sapply(mtc.studies.list(network)$values,
         function(study) {
           all(c(t1, t2) %in% mtc.study.design(network, study))
         })
  print(has.both)
  network.indirect <- filter.network(network,
                                     function(row) {
                                       !has.both[row['study']]
                                     })

  print("Constructing basic parameters...")
  style.tree <- function(tree) {
    tree <- set.edge.attribute(tree, 'arrow.mode', value=2)
    tree <- set.edge.attribute(tree, 'color', value='black')
    tree <- set.edge.attribute(tree, 'lty', value=1)
    tree
  }

  # FIXME: this needs to be a graph that includes the 'direct' node
  model[['tree']] <-
    style.tree(minimum.diameter.spanning.tree(mtc.network.graph(network.indirect)))

  model[['data']] <- mtc.model.data(model)
  model[['inits']] <- mtc.init(model)

  model[['code']] <- mtc.model.code(model, mtc.basic.parameters(model), nodesplit.relative.effect.matrix(model))

  monitors <- inits.to.monitors(model[['inits']][[1]])
  model[['monitors']] <- list(
    available=monitors,
    enabled=c(monitors[grep('^d\\.', monitors)], monitors[grep('^sd.d$', monitors)])
  )

  class(model) <- "mtc.model"

  model
}

mtc.model.name.nodesplit <- function(model) {
  paste("node-split (", model$t1, " / ", model$t2, ")", sep="")
}

# FIXME: add the split node
nodesplit.relative.effect.matrix <- function(model) {
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
