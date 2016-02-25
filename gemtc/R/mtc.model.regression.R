#' @include mtc.model.consistency.R
#' @include template.R

mtc.model.regression <- function(model, regressor) {
  style.tree <- function(tree) {
    tree <- set.edge.attribute(tree, 'arrow.mode', value=2)
    tree <- set.edge.attribute(tree, 'color', value='black')
    tree <- set.edge.attribute(tree, 'lty', value=1)
    tree
  }
  model[['tree']] <-
    style.tree(minimum.diameter.spanning.tree(mtc.network.graph(model[['network']])))

  if (is.vector(regressor)) {
    regressor <- as.list(regressor)
  }

  if (is.null(regressor[['variable']]) || is.null(regressor[['coefficient']]) ||
      (is.null(regressor[['control']]) && is.null(regressor[['classes']]))) {
    stop("Regressor specification incomplete")
  }

  if (!is.null(regressor[['control']]) && !is.null(regressor[['classes']])) {
    stop("Can not specify both 'control' and 'classes'")
  }

  by.class <- !is.null(regressor[['classes']])

  if (!by.class) {
    control <- as.treatment.factor(regressor[['control']], model[['network']])
    if (is.na(control)) {
      stop(paste0("Control treatment \"", regressor[['control']], "\" not found"))
    }
    regressor[['control']] <- control
  }

  if (!(regressor[['coefficient']] %in% c('shared', 'unrelated', 'exchangeable'))) {
    stop(paste0("Coefficient type \"", regressor[['coefficient']], "\" not supported"))
  }
  if (by.class && regressor[['coefficient']] == 'unrelated') {
    stop("Unrelated coefficients not supported for regression by class")
  }
  if (by.class && regressor[['coefficient']] == 'exchangeable') {
    stop("Exchangeable coefficients not yet implemented for regression by class")
  }

  if (by.class) {
    classes <- regressor[['classes']]
    stopifnot(is.list(classes))
    if (is.null(names(classes)) || anyDuplicated(names(classes))) {
      names(classes) <- 1:length(classes)
    }
    classes <- lapply(classes, as.treatment.factor, model[['network']])
    class.trts <- do.call(c, regressor[['classes']])
    all.trts <- model[['network']][['treatments']][['id']]
    stopifnot(!anyDuplicated(class.trts), setequal(class.trts, all.trts))
    trt.to.class <- regressionClassMap(classes)
  }

  studyData <- model[['network']][['studies']]
  if (is.null(studyData) || is.null(studyData[[regressor[['variable']]]])) {
    stop(paste0("Regressor variable \"", regressor[['variable']], "\" not found"))
  }

  # make sure the sort order of x is correct
  x <- studyData[[regressor[['variable']]]] 
  names(x) <- studyData[['study']]
  studies <- mtc.studies.list(model[['network']])[['values']]
  x <- x[studies]

  if (any(is.na(x))) {
    stop("NA values for regressor variable not supported")
  } else if (all(x %in% 0:1)) { # binary covariate
    model[['regressor']] <- c(regressor, type='binary', center=mean(x), scale=1)
  } else if (is.numeric(x)) { # continuous covariate
    model[['regressor']] <- c(regressor, type='continuous', center=mean(x), scale=2*sd(x))
  } else { # unsupported covariate
    stop("The covariate must be either binary (0/1) or numeric")
  }
  x <- (x - model[['regressor']][['center']]) / model[['regressor']][['scale']]
  model[['data']] <- mtc.model.data(model)
  model[['data']][['x']] <- x
  if (!by.class) {
    model[['data']][['reg.control']] <- control
  } else {
    model[['data']][['reg.classes']] <- trt.to.class
    model[['data']][['reg.nclasses']] <- length(classes)
  }
  model[['inits']] <- mtc.init(model)

  reg.prior.tpl <- '\n# Regression priors\nreg.prior.prec <- pow(om.scale, -2)\nfor (k in c(1:(reg.control-1), (reg.control+1):nt)) {\n  $regPrior$\n}\nbeta[reg.control] <- 0\n'
  priors <- list(
    'shared'=paste0(template.block.sub(reg.prior.tpl, 'regPrior', 'beta[k] <- B'), 'B ~ dt(0, reg.prior.prec, 1)\n'),
    'unrelated'=template.block.sub(reg.prior.tpl, 'regPrior', 'beta[k] ~ dt(0, reg.prior.prec, 1)'),
    'exchangeable'=paste0(template.block.sub(reg.prior.tpl, 'regPrior', 'beta[k] ~ dnorm(B, reg.tau)'), 'B ~ dt(0, reg.prior.prec, 1)\nreg.sd ~ dunif(0, om.scale)\nreg.tau <- pow(reg.sd, -2)'))

  priors.classes <- list(
    'shared'='\n# Regression priors\nreg.prior.prec <- pow(om.scale, -2)\nfor (k in 1:nt) {\n  beta[k] <- B[reg.classes[k]]\n}\nB[1] <- 0\nfor (k in 2:reg.nclasses) {\n  B[k] ~ dt(0, reg.prior.prec, 1)\n}')

  nt <- model[['data']][['nt']]
  reg.monitors <- c(regressionParams(regressor, nt, length(classes)), if(regressor[['coefficient']] == 'exchangeable') 'reg.sd')

  model[['code']] <- mtc.model.code(model, mtc.basic.parameters(model), consistency.relative.effect.matrix(model),
                                    linearModel='delta[i, k] + (beta[t[i, k]] - beta[t[i, 1]]) * x[i]',
                                    regressionPriors=if (by.class) priors.classes[[regressor[['coefficient']]]] else priors[[regressor[['coefficient']]]])

  monitors <- inits.to.monitors(model[['inits']][[1]])
  model[['monitors']] <- list(
    available=c(monitors, reg.monitors),
    enabled=c(monitors[grep('^d\\.', monitors)], monitors[grep('^sd.d$', monitors)], reg.monitors)
  )

  class(model) <- "mtc.model"

  model
}

mtc.model.name.regression <- function(model) {
  "meta-regression"
}

func.param.matrix.regression <- function(model, t1, t2) {
  tree.relative.effect(model[['tree']], t1, t2)
}
