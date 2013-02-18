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

    samples <- mtc.sample(model, package=sampler, n.adapt=n.adapt, n.iter=n.iter, thin=thin)

    result <- list(
        samples=samples,
        model=model,
        sampler=sampler)
    class(result) <- "mtc.result"
    result
}

mtc.build.syntaxModel <- function(model) {
    if (model$type == 'Consistency') {
        list(
            model = model$code,
            data = model$data,
            inits = model$inits,
            vars = c(mtc.basic.parameters(model), "sd.d")
        )
    } else {
        stop(paste("Model type", model$type, "unknown."))
    }
}

mtc.sample <- function(model, package, n.adapt=n.adapt, n.iter=n.iter, thin=thin) {
    if (is.na(package) || !(package %in% c("rjags", "BRugs", "R2WinBUGS"))) {
        stop(paste("Package", package, "not supported"))
    }

    # generate BUGS model
    syntax <- mtc.build.syntaxModel(model)

    # compile & run BUGS model
    file.model <- tempfile()
    cat(paste(syntax$model, "\n", collapse=""), file=file.model)
    data <- if (identical(package, 'rjags')) {
        # Note: n.iter must be specified *excluding* the n.adapt
        jags <- jags.model(file.model, data=syntax$data,
            inits=syntax$inits, n.chains=model$n.chain,
            n.adapt=n.adapt)
        coda.samples(jags, variable.names=syntax$vars,
            n.iter=n.iter, thin=thin)
    } else if (identical(package, 'BRugs')) {
        # Note: n.iter must be specified *excluding* the n.adapt
        BRugsFit(file.model, data=syntax$data,
            inits=syntax$inits, numChains=model$n.chain,
            parametersToSave=syntax$vars, coda=TRUE,
            nBurnin=n.adapt, nIter=n.iter, nThin=thin)
    } else if (identical(package, 'R2WinBUGS')) {
        # Note: codaPkg=TRUE does *not* return CODA objects, but rather
        # the names of written CODA output files.
        # Note: n.iter must be specified *including* the n.adapt
        as.mcmc.list(bugs(model.file=file.model, data=syntax$data,
            inits=syntax$inits, n.chains=model$n.chain,
            parameters.to.save=syntax$vars, codaPkg=FALSE, DIC=FALSE,
            n.burnin=n.adapt, n.iter=n.adapt+n.iter, n.thin=thin))
        # Note: does not always work on Unix systems due to a problem
        # with Wine not being able to access the R temporary path.
        # Can be fixed by creating a temporary directory in the Wine
        # C: drive:
        #       mkdir ~/.wine/drive_c/bugstmp
        # And then adding these arguments to the BUGS call:
        #       working.directory='~/.wine/drive_c/bugstmp', clearWD=TRUE
        # Or alternatively invoke R as:
        #       TMPDIR=~/.wine/drive_c/bugstmp R 
    }
    unlink(file.model)

    # return
    data
}
