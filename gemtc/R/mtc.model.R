## mtc.model class methods

mtc.model.call <- function(fn, model, ...) {
  fn <- paste(fn, model[['type']], sep='.')
  do.call(fn, c(list(model), list(...)))
}

mtc.model.defined <- function(model) {
  fns <- c('mtc.model', 'mtc.model.name')
  fns <- paste(fns, model[['type']], sep='.')
  all(exists(fns, mode='function'))
}

mtc.model <- function(network, type="consistency",
    factor=2.5, n.chain=4,
    likelihood=NULL, link=NULL,
    linearModel="random", ...) {
  if (!inherits(network, "mtc.network")) {
    stop('Given network is not an mtc.network')
  }
  if (!(linearModel %in% c("random", "fixed"))) {
    stop('linearModel must be either "random" or "fixed"')
  }

  network <- fix.network(network)

  if (check.duplicated.treatments(network, warn=TRUE)) {
    stop("Studies with duplicated treatments are not supported.")
  }

  add.std.err <- function(data) {
    if (!is.null(data[['std.dev']]) &&
        !is.null(data[['sampleSize']]) &&
        is.null(data[['std.err']])) {
      data[['std.err']] <- data[['std.dev']] / sqrt(data[['sampleSize']])
    }
    data
  }

  # calculate std.err for "legacy" data sets:
  if (!is.null(network[['data.ab']])) {
    network[['data.ab']] <- add.std.err(network[['data.ab']])
  }
  if (!is.null(network[['data.re']])) {
    network[['data.re']] <- add.std.err(network[['data.re']])
  }

  model <- list(
    type = type,
    linearModel = linearModel,
    network = network,
    n.chain = n.chain,
    var.scale = factor)

  if (!mtc.model.defined(model)) {
    stop(paste(type, 'is not an MTC model type.'))
  }

  model[['likelihood']] <- likelihood
  model[['link']] <- link
  if (!is.null(network[['data.ab']]) && 'responders' %in% colnames(network[['data.ab']])) {
    if (is.null(likelihood)) {
      model[['likelihood']] = 'binom'
    }
    if (is.null(link)) {
      model[['link']] = 'logit'
    }
  } else if (!is.null(network[['data.ab']]) && 'mean' %in% colnames(network[['data.ab']])) {
    if (is.null(likelihood)) {
      model[['likelihood']] = 'normal'
    }
    if (is.null(link)) {
      model[['link']] = 'identity'
    }
  } else {
    if (is.null(likelihood)) {
      warning('Likelihood can not be inferred. Defaulting to normal.')
      model[['likelihood']] = 'normal'
    }
    if (is.null(link)) {
      warning('Link can not be inferred. Defaulting to identity.')
      model[['link']] = 'identity'
    }
  }
  if (!ll.defined(model)) {
    stop(paste('likelihood = ', model[['likelihood']],
      ', link = ', model[['link']], ' not found!', sep=''))
  }

  model[['om.scale']] <- guess.scale(model)

  mtc.model.call('mtc.model', model, ...)
}

print.mtc.model <- function(x, ...) {
  cat("MTC ", x[['type']], " model: ", x[['description']], "\n", sep="")
}

summary.mtc.model <- function(object, ...) {
  list("Description"=paste("MTC ", object[['type']], " model: ", object[['description']], sep=""),
     "Parameters"=mtc.basic.parameters(object))
}

plot.mtc.model <- function(x, layout=igraph::layout.circle, ...) {
  igraph::plot.igraph(mtc.model.graph(x), layout=layout, ...)
}

mtc.model.graph <- function(model) {
  comparisons <- mtc.comparisons(model[['network']])
  g <- if (!is.null(model[['tree']])) model[['tree']] else model[['graph']]
  g <- g + edges(as.vector(unlist(non.edges(g, comparisons))), arrow.mode=0, lty=1, color="grey")
  g
}

# filters list of comparison by edges that are not yet present in graph g
non.edges <- function(g, comparisons) {
  sapply(1:nrow(comparisons), function(i) {
    x <- c(comparisons[['t1']][i], comparisons[['t2']][i])
    if (are.connected(g, x[1], x[2]) || are.connected(g, x[2], x[1])) c() else x
  })
}

mtc.basic.parameters <- function(model) {
  graph <- if (!is.null(model[['tree']])) model[['tree']] else model[['graph']]
  sapply(E(graph), function(e) {
    v <- get.edge(graph, e)
    paste("d", V(graph)[v[1]]$name, V(graph)[v[2]]$name, sep=".")
  })
}
