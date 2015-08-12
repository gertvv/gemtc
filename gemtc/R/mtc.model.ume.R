# Unrelated mean effects model
mtc.model.ume <- function(model) {
  network <- model[['network']]

  studies <- mtc.studies.list(network)
  na <- studies[['lengths']]
  studies <- studies[['values']]
  if (any(na > 2)) {
    warning("The Unrelated Mean Effects model does not handle multi-arm trials correctly.")
  }
  
  # these comparisons may contain duplicates due to the baselines of the RE data
  comparisons <- mtc.comparisons.baseline(network)
  # the basic parameters are the comparisons without duplicates
  basicParameters <- unique(apply(comparisons, 1, function(comparison) {
    t1 <- as.character(comparison['t1'])
    t2 <- as.character(comparison['t2'])
    if (t1 < t2) {
      data.frame('t1'=t1, 't2'=t2, stringsAsFactors=FALSE)
    } else {
      data.frame('t1'=t2, 't2'=t1, stringsAsFactors=FALSE)
    }
  }))
  basicParameters <- do.call(rbind, basicParameters)
  basicParameters[['t1']] <- as.treatment.factor(basicParameters[['t1']], network)
  basicParameters[['t2']] <- as.treatment.factor(basicParameters[['t2']], network)

  model[['graph']] <- graph.create(
    network[['treatments']][['id']],
    basicParameters,
    arrow.mode=2, color='black', lty=1)

  model[['data']] <- mtc.model.data(model)
  model[['data']][['nt']] <- NULL
  model[['inits']] <- mtc.init(model)

  model[['code']] <- mtc.model.code(model, mtc.basic.parameters(model), sparse.relative.effect.matrix(model, comparisons))

  monitors <- inits.to.monitors(model[['inits']][[1]])
  model[['monitors']] <- list(
    available=monitors,
    enabled=c(monitors[grep('^d\\.', monitors)], monitors[grep('^sd.d$', monitors)])
  )

  class(model) <- "mtc.model"

  model
}

mtc.model.name.ume <- function(model) {
  "unrelated mean effects"
}

func.param.matrix.ume <- function(model, t1, t2) {
  g <- model[['graph']]
  if((is.null(t2) || length(t2) == 0) && length(t1) == 1) {
    t2 <- V(g)[V(g) != as.numeric(t1)]
  }
  if(length(t1) > length(t2)) t2 <- rep(t2, length.out=length(t1))
  if(length(t2) > length(t1)) t1 <- rep(t1, length.out=length(t2))
  pairs <- matrix(c(t1, t2), ncol=2)
  paths <- apply(pairs, 1, function(rel) {
    edge_pos <- igraph::get.edge.ids(model[['graph']], rel)
    edge_neg <- igraph::get.edge.ids(model[['graph']], rev(rel))
    edge <- max(edge_pos, edge_neg)
    if (!edge) {
      stop(paste("The requested comparison ",
             V(g)[rel[2]]$name, " vs ", V(g)[rel[1]]$name,
             " is not present in the UME model."))
    }
    expr <- rep(0, length(igraph::E(g)))
    expr[edge] <- if (edge_pos) 1 else -1
    expr})

  # Ensure paths is a matrix, since apply() will simplify to a vector if
  # either ncol==1 or nrow==1
  paths <- matrix(as.numeric(paths), ncol=length(t1), nrow=length(E(g)))

  colnames(paths) <-  apply(pairs, 1, function(pair) {
    pair <- V(g)[pair]$name
    paste('d', pair[1], pair[2], sep='.')
  })
  paths
}

sparse.relative.effect.matrix <- function(model, comparisons) {
  ts <- model[['network']][['treatments']][['id']]
  nt <- length(ts)
  x <- sapply(1:nrow(comparisons), function(k) {
    i <- as.numeric(comparisons[k, 't1'])
    j <- as.numeric(comparisons[k, 't2'])
    if (model[['graph']][i, j, sparse=FALSE, drop=TRUE]) {
      paste("d[", i, ", ", j, "] <- d.", ts[i], ".", ts[j], sep="")
    } else {
      paste("d[", i, ", ", j, "] <- -d.", ts[j], ".", ts[i], sep="")
    }
  })
  paste(x, collapse="\n")
}

mtc.comparisons.baseline <- function(network) {
  baseline.pairs <- function(treatments) {
    n <- length(treatments)
    t1 <- rep(treatments[1], n - 1)
    t2 <- treatments[2:n]
    data.frame(t1=t1, t2=t2)
  }
  data <- mtc.merge.data(network)

  # Identify the unique "designs" (treatment combinations)
  design <- function(study) { mtc.study.design(network, study) }
  designs <- unique(lapply(unique(data[['study']]), design))

  # Generate all pair-wise comparisons from each "design"
  comparisons <- do.call(rbind, lapply(designs, baseline.pairs))

  # Ensure the output comparisons are unique and always in the same order
  comparisons <- unique(comparisons)
  comparisons <- comparisons[order(comparisons[['t1']], comparisons[['t2']]), , drop=FALSE]
  row.names(comparisons) <- NULL
  comparisons[['t1']] <- as.treatment.factor(comparisons[['t1']], network)
  comparisons[['t2']] <- as.treatment.factor(comparisons[['t2']], network)
  comparisons
}
