#' @include deviance.R

mtc.model.run <- function(network, type, ...) {
  runNames <- names(formals(mtc.run))
  runNames <- runNames[runNames != 'model']
  args <- list(...)

  # Call mtc.model with any arguments not intended for mtc.run
  modelArgs <- args[!(names(args) %in% runNames)]
  modelArgs$network <- network
  modelArgs$type <- type
  model <- do.call(mtc.model, modelArgs)

  # Call mtc.run with all arguments intended for mtc.run
  runArgs <- args[names(args) %in% runNames]
  runArgs$model <- model
  do.call(mtc.run, runArgs)
}

# If is.na(sampler), a sampler will be chosen based on availability, in this order:
# JAGS, BUGS. When the sampler is BUGS, BRugs or R2WinBUGS will be used.
mtc.run <- function(model, sampler=NA, n.adapt=5000, n.iter=20000, thin=1, n.burnin=0) {
  
  if (!is.na(sampler)) {
    if (sampler %in% c("JAGS", "rjags")) {
      warning("Setting the sampler is deprecated.")
    } else {
      stop("Setting the sampler is deprecated, only JAGS is supported.")
    }
  }

  result <- mtc.sample(model, n.adapt=n.adapt, n.iter=n.iter, thin=thin, n.burnin=n.burnin)

  result <- c(result, list(model = model))
  class(result) <- "mtc.result"
  result
}

mtc.build.syntaxModel <- function(model) {
  list(
    model = model[['code']],
    data = model[['data']],
    inits = model[['inits']],
    vars = if (!is.null(model[['monitors']][['enabled']])) model[['monitors']][['enabled']] else c(mtc.basic.parameters(model), "sd.d")
  )
}

mtc.sample <- function(model, n.adapt, n.iter, thin, n.burnin) {
  # generate JAGS model
  syntax <- mtc.build.syntaxModel(model)

  # compile & run JAGS model
  file.model <- tempfile()
  cat(paste(syntax[['model']], "\n", collapse=""), file=file.model)

  vars <- syntax[['vars']]
  if (model$dic) {
    dic.vars <- deviance_monitors(model)
    dic.vars <- dic.vars[!(dic.vars %in% vars)] # jags complains about redundant variables
    vars <- c(vars, dic.vars)
  }

  # Note: n.iter must be specified *excluding* the n.adapt
  jags <- rjags::jags.model(file.model, data=syntax[['data']],
    inits=syntax[['inits']],
    n.chains=model[['n.chain']],
    n.adapt=n.adapt)
  
  if (n.burnin > 0) update(jags, n.iter=n.burnin)
  
  samples <- rjags::coda.samples(jags, variable.names=vars,
                                 n.iter=n.iter, thin=thin)
  unlink(file.model)

  deviance.stats <- if (model[['dic']]) {
    apply(as.matrix(samples[, deviance_monitors(model), drop=FALSE]), 2, mean)
  }
  samples <- samples[, syntax[['vars']], drop=FALSE]

  # return
  list(samples=samples, deviance=computeDeviance(model, deviance.stats))
}
