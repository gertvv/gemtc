#' @include solveLP.R
#' @include arrayize.R

#'Generate a list of arms with a likelihood contribution
#'@param baseline Include study baseline arms
likelihood.arm.list <- function(network, baseline=TRUE) {
  data <- mtc.merge.data(network)
  all.studies <- as.character(data[['study']])
  studies <- rle(all.studies)[['values']]
  do.call(rbind, lapply(1:length(studies), function(i) {
    study <- studies[i]
    sel <- which(all.studies == study)
    ts <- as.character(data[['treatment']][sel])
    t1 <- rep(ts[1], length(sel) - 1)
    t2 <- ts[-1]
    idx <- 2:length(ts)
    n.ab <- if (is.null(network[['data.ab']])) 0 else nrow(network[['data.ab']])
    if (sel[1] <= n.ab && baseline) {
      t1 <- c(NA, t1)
      t2 <- c(NA, t2)
      idx <- c(1, idx)
    }
    data.frame(study=study, studyIndex=i, armIndex=idx, t1=t1, t2=t2, stringsAsFactors=FALSE)
  }))
}

# MLE estimates for the study baselines
mtc.init.mle.baseline <- function(model) {
  s.mat <- arm.index.matrix(model[['network']])
  data.ab <- model[['network']][['data.ab']]
  studies.ab <- rle(as.character(data.ab[['study']]))[['values']]
  rval <- if (length(studies.ab) > 0) {
    do.call(rbind, lapply(1:length(studies.ab), function(i) {
      study <- studies.ab[i]
      data.ab <- model[['network']][['data.ab']]
      treatment <- data.ab[['treatment']][s.mat[study, 1, drop=TRUE]]
      data <- data.ab[data.ab[['study']] == study & data.ab[['treatment']] == treatment, , drop=TRUE]
      data <- unlist(data[ll.call("required.columns.ab", model)])
      mle <- ll.call("mtc.arm.mle", model, data)
      data.frame(parameter=paste0("mu[", i, "]"), type="baseline", mean=mle['mean'], 'std.err'=mle['sd'], stringsAsFactors=FALSE)
    }))
  } else {
    data.frame(parameter=character(), type=character(), mean=numeric(), std.err=numeric())
  }
  rownames(rval) <- NULL
  rval
}

# MLE estimates for the study-level relative effects
mtc.init.mle.relative <- function(model) {
  data.ab <- model[['network']][['data.ab']]
  data.re <- model[['network']][['data.re']]

  columns <- ll.call("required.columns.ab", model)
  processArm <- function(study, studyIndex, armIndex, t1, t2) {
    if (!is.null(data.ab) && study %in% data.ab[['study']]) {
      data <- data.ab[data.ab[['study']] == study & (data.ab[['treatment']] == t1 | data.ab[['treatment']] == t2), columns, drop=FALSE]
      mle <- ll.call("mtc.rel.mle", model, as.matrix(data))
    } else { # data.re -- assumes baseline is unaltered
      data <- data.re[data.re[['study']] == study & data.re[['treatment']] == t2, , drop=TRUE]
      mle <- c('mean'=data[['diff']], 'sd'=data[['std.err']])
    }
    data.frame(parameter=paste0("delta[", studyIndex, ",", armIndex, "]"), type="relative", mean=mle['mean'], 'std.err'=mle['sd'], stringsAsFactors=FALSE)
  }

  arms <- likelihood.arm.list(model[['network']], baseline=FALSE)
  rval <- do.call(rbind, mapply(processArm, arms$study, arms$studyIndex, arms$armIndex, arms$t1, arms$t2, SIMPLIFY=FALSE))
  rownames(rval) <- NULL
  rval
}

mle.pooled.effect <- function(model, t1, t2, om.scale) {
  t1 <- as.treatment.factor(t1, model[['network']])
  t2 <- as.treatment.factor(t2, model[['network']])
  pair <- data.frame(t1=t1, t2=t2)

  calc <- function(data, fun) {
    sel1 <- data[['treatment']] == t1
    sel2 <- data[['treatment']] == t2
    studies <- intersect(unique(data[['study']][sel1]), unique(data[['study']][sel2]))

    study.mle <- sapply(studies, function(study) {
      fun(data[data[['study']] == study, , drop=FALSE])
    })

    if (!is.matrix(study.mle)) {
      study.mle <- matrix(study.mle, nrow=2)
    }
    rownames(study.mle) <- c('mean', 'sd')

    study.mle
  }

  study.mle <- NULL
  data.ab <- model[['network']][['data.ab']]
  if (!is.null(data.ab)) {
    study.mle <- calc(data.ab, function(data) {
      rel.mle.ab(data, model, pair)
    })
  }
  data.re <- model[['network']][['data.re']]
  if (!is.null(data.re)) {
    study.mle <- cbind(study.mle, calc(data.re, function(data) {
      rel.mle.re(data, pair)
    }))
  }
  meta <- 
    if (ncol(study.mle) != 0) {
      meta::metagen(unlist(study.mle['mean', ]), unlist(study.mle['sd', ]))
    } else {
      list('TE.random'=0, seTE.random=om.scale)
    }
  data.frame(parameter=paste('d', t1, t2, sep='.'), type='basic', mean=meta[['TE.random']], std.err=meta[['seTE.random']], stringsAsFactors=FALSE)
}

# MLE estimates for the basic parameters
# based on inverse-variance random effects meta-analysis (package meta)
mtc.init.mle.basic <- function(model) {
  graph <- if(!is.null(model[['tree']])) model[['tree']] else model[['graph']]
  if (!is.null(graph)) {
    params <- mtc.basic.parameters(model)
    rval <- do.call(rbind, lapply(E(graph), function(e) {
      v <- ends(graph, e, names=FALSE)
      mle.pooled.effect(model, V(graph)[v[1]]$name, V(graph)[v[2]]$name, model[['om.scale']])
    }))
    rownames(rval) <- NULL
    rval
  } else {
    NULL
  }
}

mtc.init.mle.regression <- function(model) {
  nc <- length(model[['regressor']][['classes']])
  params <- regressionParams(model[['regressor']], model[['data']][['nt']], nc)
  params <- params[params != "reg.sd"]
  data.frame(parameter=params, type='coefficient',
             mean=0.0, std.err=model[['om.scale']],
             stringsAsFactors=FALSE)
}

# Matrix representing the linear model level of the BHM
mtc.linearModel.matrix <- function(model, parameters) {
  basic <- mtc.basic.parameters(model)
  if (model[['type']] == 'regression') {
    regr <- regressionParams(model[['regressor']], model[['data']][['nt']], length(model[['regressor']][['classes']]))
  }
  processArm <- function(study, studyIndex, armIndex, t1, t2) {
    x <- rep(0, length(parameters))
    names(x) <- parameters
    x[parameters == paste0("mu[", studyIndex, "]")] <- 1
    if (!is.na(t1) && !is.na(t2)) {
      if (model[['linearModel']] == 'random') {
        x[parameters == paste0("delta[", studyIndex, ",", armIndex, "]")] <- 1
      } else {
        x[basic] <- as.vector(mtc.model.call('func.param.matrix', model, t1=t1, t2=t2))
      }
      if (model[['type']] == 'regression') {
        t1 <- as.treatment.factor(t1, model[['network']])
        t2 <- as.treatment.factor(t2, model[['network']])
        x[regr] <- regressionAdjustMatrix(t1, t2, model[['regressor']], model[['data']][['nt']]) * model[['data']][['x']][studyIndex]
      }
    }
    x
  }

  # loop over arms that have a likelihood contribution, generate a row (column?) for each
  arms <- likelihood.arm.list(model[['network']], baseline=TRUE)
  rval <- t(mapply(processArm, arms$study, arms$studyIndex, arms$armIndex, arms$t1, arms$t2, USE.NAMES=FALSE))
  dimnames(rval) <- NULL
  rval
}

# Initial values for heterogeneity from prior
mtc.init.hy <- function(hy.prior, om.scale, n.chain) {
  fn <- hy.prior[['distr']]
  substr(fn, 1, 1) <- "r"
  args <- c(n.chain, hy.prior[['args']])
  args[args == 'om.scale'] <- om.scale
  values <- do.call(fn, args)
  if (hy.prior$type == "prec") {
    pmax(values, 1E-232) # prevent underflow in JAGS/BUGS (precision 0 is variance \infty)
  } else {
    values
  }
}

# Generate initial values for all relevant parameters
mtc.init <- function(model) {
  hy <- if (model[['linearModel']] == 'random') {
    mtc.init.hy(model[['hy.prior']], model[['om.scale']], model[['n.chain']])
  } else {
    c()
  }

  # Approximate MLE estimates of model parameters
  mle <- mtc.init.mle.baseline(model)
  if (model[['linearModel']] == 'random') {
    mle <- rbind(mle, mtc.init.mle.relative(model))
  }
  mle <- rbind(mle, mtc.init.mle.basic(model))

  if (model[['type']] == 'regression') {
    mle <- rbind(mle, mtc.init.mle.regression(model))
  }

  # Define parameter value constraints
  params <- mle[['parameter']]
  linearModel <- mtc.linearModel.matrix(model, params)
  limits <- ll.call("inits.info", model)[['limits']]
  constr.eq <- NULL
  constr.rhs <- NULL
  constr.mat <- NULL
  if (all(is.finite(limits))) {
    # Ax >= L (-Ax <= -L); Ax <= U
    constr.mat <- rbind(-linearModel, linearModel)
    constr.rhs <- c(rep(-limits[1], nrow(linearModel)), rep(limits[2], nrow(linearModel)))
    constr.eq <- rep(0, 2*nrow(linearModel))
  } else if (any(is.finite(limits))) {
    stop("Likelihood/link constrained on one side not (yet) supported")
  }

  # Generate a random permutation of the parameters
  randomParameterOrder <- function() {
    c(sample(params[mle[['type']] == 'coefficient']),
      sample(params[mle[['type']] == 'basic']),
      sample(params[mle[['type']] == 'relative']),
      sample(params[mle[['type']] == 'baseline']))
  }

  # Find min/max value for the given parameter under the constraints
  findLimit <- function(mat, rhs, eq, idx, max) {
    if (is.null(mat)) {
      if (max) { +Inf } else { -Inf }
    } else {
      obj <- rep(0, ncol(mat))
      obj[idx] <- 1
      solveLP(obj, mat, rhs, eq, max=max)
    }
  }

  # Generate initial values for each chain in turn
  inits <- lapply(1:model[['n.chain']], function(chain) {
    param.order <- randomParameterOrder()
    stopifnot(length(param.order) == length(params)) # sanity check

    # sample initial values for the linear model parameters
    x <- rep(NA, length(params))
    names(x) <- params
    for (param in param.order) {
      param.mle <- mle[params == param, ]
      param.limits <- c(findLimit(constr.mat, constr.rhs, constr.eq, params == param, FALSE),
                        findLimit(constr.mat, constr.rhs, constr.eq, params == param, TRUE))

      x[param] <- truncnorm::rtruncnorm(n=1,
                                        mean=param.mle[['mean']],
                                        sd=model[['var.scale']] * param.mle[['std.err']],
                                        a=param.limits[1], b=param.limits[2])

      where <- rep(0, length(params))
      where[params == param] <- 1
      if (!is.null(constr.mat) && param.mle[['type']] != 'baseline') {
        constr.mat <- rbind(constr.mat, where)
        constr.rhs <- c(constr.rhs, unname(x[param]))
        constr.eq <- c(constr.eq, TRUE)
      }
    }

    # add initial values for the heterogeneity
    if (model[['linearModel']] == 'random') {
      type <- model[['hy.prior']][['type']]
      if (type == 'std.dev') {
        x['sd.d'] <- hy[chain]
      } else if (type == 'var') {
        x['var.d'] <- hy[chain]
      } else if (type == 'prec') {
        x['tau.d'] <- hy[chain]
      } else {
        stop("Invalid heterogeneity prior type")
      }
    }

    # convert flat representation to structured one
    x <- arrayize(x)

    # replace mu to whatever the study baseline prior is on
    if (!is.null(x[['mu']])) {
      mu <- x[['mu']]
      x[['mu']] <- NULL
      info <- ll.call('inits.info', model)
      x[[info[['param']]]] <- info[['transform']](mu)
    }

    x
  })
  
  inits
}

# All non-NA initial values correspond to a variable that can be monitored
inits.to.monitors <- function(inits) {
  unlist(lapply(names(inits), function(var) {
    struct <- inits[[var]]
    if (is.matrix(struct)) {
      lapply(1:(nrow(struct)*ncol(struct)), function(idx) {
        i <- (idx - 1) %/% ncol(struct) + 1
        j <- (idx - 1) %% ncol(struct) + 1
        if (!is.na(struct[i, j, drop=TRUE])) paste(var, "[", i, ",", j, "]", sep="")
      })
    } else if (length(struct) > 1) {
      lapply(1:length(struct), function(i) {
        if (!is.na(struct[i])) paste(var, "[", i, "]", sep="")
      })
    } else if (var == "var.d" || var == "tau.d") {
      c(var, "sd.d")
    } else {
      var
    }
  }))
}
