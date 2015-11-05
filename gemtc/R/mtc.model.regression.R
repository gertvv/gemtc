#' @include mtc.model.consistency.R

mtc.model.regression <- function(model, regressor) {
  style.tree <- function(tree) {
    tree <- set.edge.attribute(tree, 'arrow.mode', value=2)
    tree <- set.edge.attribute(tree, 'color', value='black')
    tree <- set.edge.attribute(tree, 'lty', value=1)
    tree
  }
  model[['tree']] <-
    style.tree(minimum.diameter.spanning.tree(mtc.network.graph(model[['network']])))

  regressor[['control']] <- as.treatment.factor(regressor[['control']], model[['network']])
  control <- regressor[['control']]
  x <- regressorData(model[['network']], regressor)
  if (all(x %in% 0:1)) { # binary covariate
    model[['regressor']] <- c(regressor, type='binary')
  } else if (is.numeric(x)) { # continuous covariate
    model[['regressor']] <- c(regressor, type='continuous', mu=mean(x), sd=sd(x))
    x <- (x - mean(x)) / sd(x)
  } else { # unsupported covariate
    stop("The covariate must be either binary (0/1) or numeric")
  }
  model[['data']] <- mtc.model.data(model)
  model[['data']][['x']] <- x
  model[['data']][['reg.control']] <- control
  model[['inits']] <- mtc.init(model)

  priors <- list(
    'shared'='\n# Regression priors\nfor (k in 1:(reg.control-1)) {\n  beta[k] <- B\n}\nbeta[reg.control] <- 0\nfor (k in (reg.control+1):nt) {\n  beta[k] <- B\n}\nB ~ dnorm(0, prior.prec)\n',
    'unrelated'='\n# Regression priors\nfor (k in 1:(reg.control-1)) {\n  beta[k] ~ dnorm(0, prior.prec)\n}\nbeta[reg.control] <- 0\nfor (k in (reg.control+1):nt) {\n  beta[k] ~ dnorm(0, prior.prec)\n}\n',
    'exchangeable'='\n# Regression priors\nfor (k in 1:(reg.control-1)) {\n  beta[k] ~ dnorm(B, reg.tau)\n}\nbeta[reg.control] <- 0\nfor (k in (reg.control+1):nt) {\n  beta[k] ~ dnorm(B, reg.tau)\n}\nB ~ dnorm(0, prior.prec)\nreg.sd ~ dunif(0, om.scale)\nreg.tau <- pow(reg.sd, -2)')

  nt <- model[['data']][['nt']]
  reg.monitors <- regressionParams(regressor, nt)

  model[['code']] <- mtc.model.code(model, mtc.basic.parameters(model), consistency.relative.effect.matrix(model),
                                    linearModel='delta[i, k] + (beta[t[i, k]] - beta[t[i, 1]]) * x[i]',
                                    regressionPriors=priors[[regressor[['coefficient']]]])

  monitors <- inits.to.monitors(model[['inits']][[1]])
  model[['monitors']] <- list(
    available=c(monitors, reg.monitors),
    enabled=c(monitors[grep('^d\\.', monitors)], monitors[grep('^sd.d$', monitors)], reg.monitors)
  )

  class(model) <- "mtc.model"

  model
}

regressorData <- function(network, regressor) {
  var <- regressor[['variable']]
  data <- mtc.merge.data(network)
  studies <- unique(data[['study']])
  unname(sapply(studies, function(study) {
    sel <- network[['data.ab']][['study']] == study
    if (any(sel)) {
      network[['data.ab']][[var]][sel][1]
    } else {
      sel <- network[['data.re']][['study']] == study
      network[['data.re']][[var]][sel][1]
    }
  }))
}

mtc.model.name.regression <- function(model) {
  "meta-regression"
}

func.param.matrix.regression <- function(model, t1, t2) {
  tree.relative.effect(model[['tree']], t1, t2)
}
