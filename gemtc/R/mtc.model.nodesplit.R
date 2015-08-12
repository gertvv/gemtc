# Node-split model
mtc.model.nodesplit <- function(model, t1, t2) {
  # Make sure t1 and t2 are 'character' and t1 < t2
  network <- model[['network']]
  ts <- factor(c(t1, t2), levels=levels(network[['treatments']][['id']]))
  if (as.numeric(ts[1]) > as.numeric(ts[2])) {
    ts <- rev(ts)
  }
  t1 <- as.character(ts[1])
  t2 <- as.character(ts[2])

  stopifnot(has.indirect.evidence(network, t1, t2))

  # Rewrite the dataset
  data.ab <- nodesplit.rewrite.data.ab(network[['data.ab']], t1, t2)
  data.re <- nodesplit.rewrite.data.re(network[['data.re']], t1, t2)
  network <- mtc.network(data.ab=data.ab,
                         data.re=data.re,
                         treatments=model[['network']][['treatments']])
  model[['network']] <- network

  # Construct indirect network
  has.both <- sapply(mtc.studies.list(network)$values,
         function(study) {
           all(c(t1, t2) %in% mtc.study.design(network, study))
         })
  network.indirect <- filter.network(network,
                                     function(row) {
                                       !has.both[row['study']]
                                     })

  # Generate tree for indirect evidence
  style.tree <- function(tree) {
    tree <- set.edge.attribute(tree, 'arrow.mode', value=2)
    tree <- set.edge.attribute(tree, 'color', value='black')
    tree <- set.edge.attribute(tree, 'lty', value=1)
    tree
  }
  tree.indirect <- connect.mds.forest(mtc.network.graph(network.indirect))
  model[['tree.indirect']] <- style.tree(tree.indirect)
  model[['graph']] <- model[['tree.indirect']] + edge(c(t1, t2))
  model[['graph']] <- set.edge.attribute(model[['graph']], 'arrow.mode', index=length(E(model[['graph']])), value=2)
  model[['graph']] <- set.edge.attribute(model[['graph']], 'color', index=length(E(model[['graph']])), value='black')
  model[['graph']] <- set.edge.attribute(model[['graph']], 'lty', index=length(E(model[['graph']])), value=2)

  model[['data']] <- mtc.model.data(model)
  model[['data']][['split']] <- as.numeric(ts)
  model[['split']] <- ts
  model[['inits']] <- mtc.init(model)

  model[['code']] <- mtc.model.code(model, mtc.basic.parameters(model), nodesplit.relative.effect.matrix(model, tree.indirect))

  monitors <- c(inits.to.monitors(model[['inits']][[1]]), 'd.direct', 'd.indirect')
  model[['monitors']] <- list(
    available=monitors,
    enabled=c(monitors[grep('^d\\.', monitors)], monitors[grep('^sd.d$', monitors)])
  )

  class(model) <- "mtc.model"

  model
}

mtc.model.name.nodesplit <- function(model) {
  paste("node-split (", model[['t1']], " / ", model[['t2']], ")", sep="")
}

func.param.matrix.nodesplit <- function(model, t1, t2) {
  base <- tree.relative.effect(model[['tree.indirect']], t1, t2)
  m <- rbind(base, rep(0, ncol(base)))
  param.pos <- paste("d", model[['split']][1], model[['split']][2], sep=".")
  param.neg <- paste("d", model[['split']][2], model[['split']][1], sep=".")
  if (param.pos %in% colnames(base)) {
    m[, param.pos] <- c(rep(0, nrow(base)), 1)
  }
  if (param.neg %in% colnames(base)) {
    m[, param.neg] <- c(rep(0, nrow(base)), -1)
  }
  m
}

nodesplit.relative.effect.matrix <- function(model, tree) {
  # Generate list of linear expressions
  params <- mtc.basic.parameters(model)
  re <- tree.relative.effect(tree, V(tree)[1], t2=NULL)
  expr <- apply(re, 2, function(col) { paste(sapply(which(col != 0), function(i) {
    paste(if (col[i] == -1) "-" else "", params[i], sep="")
  }), collapse = " + ") })
  expr <- sapply(1:length(expr), function(i) { paste('d1[', i + 1, '] <- ', expr[i], sep='') })
  expr <- c('d1[1] <- 0', expr, 'for (i in 1:nt) {\n\tfor (j in 1:nt) {\n\t\tis.split[i, j] <- equals(i, split[1]) * equals(j, split[2]) + equals(i, split[2]) * equals(j, split[1])\n\t\td[i, j] <- (1 - is.split[i, j]) * (d1[j] - d1[i]) + is.split[i, j] * (2 * equals(i, split[1]) - 1) * d.direct\n\t}\n}')
  expr <- c(expr, paste('d.direct <- d', model[['split']][1], model[['split']][2], sep='.'))
  expr <- c(expr, 'd.indirect <- d1[split[2]] - d1[split[1]]')
  paste(expr, collapse="\n")
}
