#' @include stopIfNotConsistent.R
#' @include regression.R

treatment.pairs <- function(t1, t2, ts) {
  if((is.null(t2) || length(t2) == 0) && length(t1) == 1) {
    t2 <- ts[ts != as.numeric(t1)]
  }
  if(length(t1) > length(t2)) t2 <- rep(t2, length.out=length(t1))
  if(length(t2) > length(t1)) t1 <- rep(t1, length.out=length(t2))
  matrix(c(t1, t2), ncol=2)
}

# basic parameters are rows, derived parameters (i.e. the relative effects) are columns
tree.relative.effect <- function(g, t1, t2) {
  pairs <- treatment.pairs(t1, t2, V(g))
  paths <- apply(pairs, 1, function(rel) {
    p <- suppressWarnings(get.shortest.paths(g, rel[1], rel[2], mode='all'))
    if (is.list(p) && "vpath" %in% names(p)) { p <- p$vpath }
    p <- unlist(p)
    if (length(p) == 0) {
      stop(paste("The requested comparison ",
             V(g)[rel[2]]$name, " vs ", V(g)[rel[1]]$name,
             " can not be expressed as a linear combination of the ",
             "available parameters", sep=""))
    }
    p <- matrix(c(p[1:length(p)-1], p[-1]), ncol=2)

    sapply(E(g), function(e) {
      v <- ends(g, e, names=FALSE)
      if (sum(p[,1] == v[1] & p[,2] == v[2])) 1
      else if (sum(p[,1] == v[2] & p[,2] == v[1])) -1
      else 0
    })
  })

  # Ensure paths is a matrix, since apply() will simplify to a vector if
  # either ncol==1 or nrow==1
  paths <- matrix(as.numeric(paths), nrow=length(E(g)))

  colnames(paths) <- apply(pairs, 1, function(pair) {
    pair <- V(g)[pair]$name
    paste('d', pair[1], pair[2], sep='.')
  })
  paths
}

# Given a vector of parameters names (d.A.B, sd.d, etc.), extract all basic
# parameters and generate a two-column matrix in which each row is a comparison
extract.comparisons <- function(parameters) {
  x <- parameters[grep("^d\\.", parameters)]
  matrix(unlist(strsplit(x, "\\.")), ncol=3, byrow=TRUE)[ , -1, drop=FALSE]
}

spanning.tree.mtc.result <- function(result) {
  network <- result[['model']][['network']]
  pairs <- extract.comparisons(colnames(result[['samples']][[1]]))
  pairs <- matrix(pairs, ncol=2)
  parameters <- data.frame(
    t1=as.treatment.factor(pairs[,1], network),
    t2=as.treatment.factor(pairs[,2], network)
  )
  graph.create(network[['treatments']][['id']], parameters, arrow.mode=2, color="black", lty=1)
}

relative.effect <- function(result, t1, t2 = c(), preserve.extra=TRUE, covariate=NA) {
  stopIfNotConsistent(result, 'relative.effect')

  # Build relative effect transformation matrix
  network <- result[['model']][['network']]
  g <- spanning.tree.mtc.result(result)
  if (is.character(t1)) {
    t1 <- as.treatment.factor(t1, network)
  }
  if (is.character(t2)) {
    t2 <- as.treatment.factor(t2, network)
  }
  effects <- tree.relative.effect(g, t1, t2)

  parnames <- colnames(result[['samples']][[1]])
  parameters <- grep("^d\\.", parnames)

  if (result[['model']][['type']] == 'regression' && !is.na(covariate)) {
    model <- result[['model']]
    regressor <- model[['regressor']]

    if (regressor[['type']] == 'binary' && !(covariate %in% 0:1)) {
      stop("Invalid value for binary covariate")
    }

    nt <- nrow(model[['network']][['treatments']])
    nc <- length(model[['regressor']][['classes']])
    regression.parameters <- sapply(regressionParams(regressor, nt, nc), function(p) { which(parnames == p) })
    parameters <- c(parameters, regression.parameters)

    transform <- regressionAdjustMatrix(t1, t2, regressor, nt)
    transform <- transform * (covariate - regressor[['center']]) / regressor[['scale']]
    effects <- rbind(effects, transform)
  }

  # Apply tranformation to each chain
  samples <- as.mcmc.list(lapply(result[['samples']], function(chain) {
    samples <- chain[,parameters] %*% effects
    if (preserve.extra) {
      samples <- cbind(samples, chain[,-parameters,drop=FALSE])
    }
    mcmc(samples, start=start(chain), end=end(chain), thin=thin(chain))
  }))
  effects <- list(
    samples=samples,
    model=result[['model']],
    sampler=result[['sampler']],
    covariate=covariate)

  class(effects) <- "mtc.result"
  effects
}
