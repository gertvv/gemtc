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
mtc.run <- function(model, sampler=NA, n.adapt=5000, n.iter=20000, thin=1) {
  if (!is.na(sampler)) {
    if (sampler %in% c("JAGS", "rjags")) {
      warning("Setting the sampler is deprecated.")
    } else {
      stop("Setting the sampler is deprecated, only JAGS is supported.")
    }
  }

  result <- mtc.sample(model, n.adapt=n.adapt, n.iter=n.iter, thin=thin)

  result <- list(
    samples=result$samples,
    dic=result$dic,
    model=model,
    sampler=sampler)
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

mtc.sample <- function(model, n.adapt=n.adapt, n.iter=n.iter, thin=thin) {
  # generate JAGS model
  syntax <- mtc.build.syntaxModel(model)

  # compile & run JAGS model
  file.model <- tempfile()
  cat(paste(syntax[['model']], "\n", collapse=""), file=file.model)

  # Note: n.iter must be specified *excluding* the n.adapt
  rjags::load.module('dic')
  jags <- rjags::jags.model(file.model, data=syntax[['data']],
    inits=syntax[['inits']], n.chains=model[['n.chain']],
    n.adapt=n.adapt)
  samples <- rjags::jags.samples(jags, variable.names=c(syntax[['vars']], 'deviance', 'pD'),
    n.iter=n.iter, thin=thin)

  # Calculate DIC
  Dbar <- mean(as.numeric(samples$deviance))
  pD <- mean(as.numeric(samples$pD))

  # Convert samples to mcmc.list
  samples <- lapply(samples, as.mcmc.list)
  samples$pD <- NULL
  varNames <- names(samples)
  samples <- lapply(1:model[['n.chain']], function(i) {
    chain <- lapply(samples, function(x) { x[[i]] })
    chain <- do.call(cbind, chain)
    colnames(chain) <- varNames
    mcmc(chain, start=n.adapt+1, thin=thin)
  })
  data <- list(samples=as.mcmc.list(samples),
        dic=c('Mean deviance'=Dbar, 'Penalty (pD)'=pD, 'DIC'=Dbar+pD))

  unlink(file.model)

  # return
  data
}
