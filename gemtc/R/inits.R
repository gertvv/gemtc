# Limit initial values to allowed range
mtc.init.limit <- function(model, value, offset=rep(0.0, model[['n.chain']])) {
  limits <- ll.call("scale.limit.inits", model)
  too.small <- (value + offset) < limits[1]
  too.large <- (value + offset) > limits[2]
  value[too.small] <- limits[1] - offset[too.small]
  value[too.large] <- limits[2] - offset[too.large]
  value
}

# Initial values for study-level absolute treatment effects based on (adjusted) MLE
mtc.init.baseline.effect <- function(model, study, treatment) {
  data.ab <- model[['network']][['data.ab']]
  data <- data.ab[data.ab[['study']] == study & data.ab[['treatment']] == treatment, , drop=TRUE]
  data <- unlist(data[ll.call("required.columns.ab", model)])
  mle <- ll.call("mtc.arm.mle", model, data)
  mtc.init.limit(
    model,
    rnorm(model[['n.chain']], mle['mean'], model[['var.scale']] * mle['sd'])
  )
}

# Initial values for study-level relative effects based on (adjusted) MLE
mtc.init.relative.effect <- function(model, study, t1, t2, mu=rep(0.0, model[['n.chain']])) {
  data <- model[['network']][['data.ab']]
  if (!is.null(data) && study %in% data[['study']]) {
    columns <- ll.call("required.columns.ab", model)
    data <- data[data[['study']] == study & (data[['treatment']] == t1 | data[['treatment']] == t2), columns, drop=FALSE]
    mle <- ll.call("mtc.rel.mle", model, as.matrix(data))
  } else { # data.re -- assumes baseline is unaltered
    data <- model[['network']][['data.re']]
    data <- data[data[['study']] == study & data[['treatment']] == t2, , drop=TRUE]
    mle <- c('mean'=data[['diff']], 'sd'=data[['std.err']])
  }
  mtc.init.limit(
    model,
    rnorm(model[['n.chain']], mle['mean'], model[['var.scale']] * mle['sd']),
    mu
  )
}

# Initial values for pooled effect (basic parameter) based on
# inverse-variance random effects meta-analysis (package meta)
mtc.init.pooled.effect <- function(model, t1, t2, om.scale) {
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

  rnorm(model[['n.chain']], meta[['TE.random']], model[['var.scale']] * meta[['seTE.random']])
}

# Initial values for heterogeneity from prior
mtc.init.hy <- function(hy.prior, om.scale, n.chain) {
  fn <- hy.prior[['distr']]
  substr(fn, 1, 1) <- "r"
  args <- c(n.chain, hy.prior[['args']])
  args[args == 'om.scale'] <- om.scale
  do.call(fn, args)
}

# Generate initial values for all relevant parameters
mtc.init <- function(model) {
  data.ab <- model[['network']][['data.ab']]
  data.re <- model[['network']][['data.re']]
  s.mat <- arm.index.matrix(model[['network']])
  studies <- rle(as.character(data.ab[['study']]))[['values']]

  # Generate initial values for each parameter
  mu <- sapply(studies, function(study) {
    mtc.init.baseline.effect(model, study, data.ab[['treatment']][s.mat[study, 1, drop=TRUE]])
  })
  if (!is.matrix(mu)) {
    mu <- matrix(mu, nrow=model[['n.chain']], ncol=length(studies))
  }
  studies <- c(studies, rle(as.character(data.re[['study']]))[['values']])
  ts <- c(as.character(data.ab[['treatment']]), as.character(data.re[['treatment']]))
  delta <- lapply(studies, function(study) {
    sapply(1:ncol(s.mat), function(i) {
      if (i == 1 || is.na(s.mat[study, i, drop=TRUE])) rep(NA, model[['n.chain']])
      else mtc.init.relative.effect(
           model, study,
           ts[s.mat[study, 1, drop=TRUE]],
           ts[s.mat[study, i, drop=TRUE]],
           if (study %in% colnames(mu)) { mu[, study, drop=TRUE] } else { rep(0.0, model[['n.chain']]) })
    })
  })
  graph <- if(!is.null(model[['tree']])) model[['tree']] else model[['graph']]
  if (!is.null(graph)) {
    params <- mtc.basic.parameters(model)
    d <- sapply(E(graph), function(e) {
      v <- get.edge(graph, e)
      mtc.init.pooled.effect(model, V(graph)[v[1]]$name, V(graph)[v[2]]$name, model[['om.scale']])
    })
    hy <- mtc.init.hy(model[['hy.prior']], model[['om.scale']], model[['n.chain']])
  } else {
    params <- c()
    hy <- c()
  }

  # Separate the initial values per chain
  lapply(1:model[['n.chain']], function(chain) {
    c(
      if (!is.null(data.ab)) {
        list(mu = mu[chain, , drop=TRUE])
      } else {
        list()
      },
      if (model[['linearModel']] == 'random') {
        type <- model[['hy.prior']][['type']]
        c(
          list(delta = t(sapply(delta, function(x) { x[chain, , drop=TRUE] }))),
          if (type == 'std.dev') {
            list(sd.d=hy[chain])
          } else if (type == 'var') {
            list(var.d=hy[chain])
          } else if (type == 'prec') {
            list(tau.d=hy[chain])
          } else {
            stop("Invalid heterogeneity prior type")
          }
        )
      } else {
        list()
      },
      sapply(params, function(p) { d[chain, which(params == p), drop=TRUE] })
    )
  })
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
