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
  bugs <- c('BRugs', 'R2WinBUGS')
  jags <- c('rjags')
  available <- if (is.na(sampler)) {
    c(jags, bugs)
  } else if (sampler == 'BUGS') {
    bugs
  } else if (sampler == 'JAGS') {
    jags
  } else {
    c(sampler)
  }

  have.package <- function(name) {
    suppressWarnings(do.call(library, list(name, logical.return=TRUE, quietly=TRUE)))
  }

  found <- NA
  i <- 1
  while (is.na(found) && i <= length(available)) {
    if (have.package(available[i])) {
      found <- available[i]
    }
    i <- i + 1
  }
  if (is.na(found)) {
    stop(paste("Could not find a suitable sampler for", sampler))
  }
  sampler <- found

  result <- mtc.sample(model, package=sampler, n.adapt=n.adapt, n.iter=n.iter, thin=thin)

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

mtc.sample <- function(model, package, n.adapt=n.adapt, n.iter=n.iter, thin=thin) {
  if (is.na(package) || !(package %in% c("rjags", "BRugs", "R2WinBUGS"))) {
    stop(paste("Package", package, "not supported"))
  }

  # generate BUGS model
  syntax <- mtc.build.syntaxModel(model)

  # compile & run BUGS model
  file.model <- tempfile()
  cat(paste(syntax[['model']], "\n", collapse=""), file=file.model)
  data <- if (identical(package, 'rjags')) {
    # Note: n.iter must be specified *excluding* the n.adapt
    load.module('dic')
    jags <- jags.model(file.model, data=syntax[['data']],
      inits=syntax[['inits']], n.chains=model[['n.chain']],
      n.adapt=n.adapt)
    samples <- jags.samples(jags, variable.names=c(syntax[['vars']], 'deviance', 'pD'),
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
    list(samples=as.mcmc.list(samples),
         dic=c('Mean deviance'=Dbar, 'Penalty (pD)'=pD, 'DIC'=Dbar+pD))
  } else if (identical(package, 'BRugs')) {
    # Note: n.iter must be specified *excluding* the n.adapt
    samples <- BRugsFit(file.model, data=syntax[['data']],
      inits=syntax[['inits']], numChains=model[['n.chain']],
      parametersToSave=c(syntax[['vars']]), coda=TRUE, DIC=TRUE,
      nBurnin=n.adapt, nIter=n.iter, nThin=thin)
    list(samples=samples, dic=NULL)
  } else if (identical(package, 'R2WinBUGS')) {
    # Note: codaPkg=TRUE does *not* return CODA objects, but rather
    # the names of written CODA output files.
    # Note: n.iter must be specified *including* the n.adapt
    samples <- as.mcmc.list(bugs(model.file=file.model, data=syntax[['data']],
      inits=syntax[['inits']], n.chains=model[['n.chain']],
      parameters.to.save=syntax[['vars']], codaPkg=FALSE, DIC=TRUE,
      n.burnin=n.adapt, n.iter=n.adapt+n.iter, n.thin=thin))
    # Note: does not always work on Unix systems due to a problem
    # with Wine not being able to access the R temporary path.
    # Can be fixed by creating a temporary directory in the Wine
    # C: drive:
    #   mkdir ~/.wine/drive_c/bugstmp
    # And then adding these arguments to the BUGS call:
    #   working.directory='~/.wine/drive_c/bugstmp', clearWD=TRUE
    # Or alternatively invoke R as:
    #   TMPDIR=~/.wine/drive_c/bugstmp R
    list(samples=samples, DIC=NULL)
  }
  unlink(file.model)

  # return
  data
}
