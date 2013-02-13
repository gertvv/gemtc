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

	# Switch on sampler
	samples <- if (sampler %in% bugs) {
		mtc.run.bugs(model, package=sampler, n.adapt=n.adapt, n.iter=n.iter, thin=thin)
	} else if (sampler %in% jags) {
		mtc.run.jags(model, package=sampler, n.adapt=n.adapt, n.iter=n.iter, thin=thin)
	}

	result <- list(
		samples=samples,
		model=model,
		sampler=sampler)
	class(result) <- "mtc.result"
	result
}

mtc.build.syntaxModel <- function(model, is.jags) {
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

mtc.run.bugs <- function(model, package, n.adapt=n.adapt, n.iter=n.iter, thin=thin) {
	if (is.na(package) || !(package %in% c("BRugs", "R2WinBUGS"))) {
		stop(paste("Package", package, "not supported"))
	}

	# generate BUGS model
	syntax1 <- mtc.build.syntaxModel(model, is.jags=FALSE)
	syntax2 <- mtc.build.syntaxModel(model, is.jags=TRUE)

	# compile & run BUGS model
	file.model <- tempfile()
	cat(paste(syntax1$model, "\n", collapse=""), file=file.model)
	data <- if (package == 'BRugs') {
		# Note: n.iter must be specified *excluding* the burn-in
		BRugsFit(file.model, data=syntax2$data,
			inits=syntax2$inits, numChains=model$n.chain,
			parametersToSave=syntax1$vars, coda=TRUE,
			nBurnin=n.adapt, nIter=n.iter, nThin=thin)
	} else if (package == 'R2WinBUGS') {
		# Note: codaPkg=TRUE does *not* return CODA objects, but rather
		# the names of written CODA output files.
		# Note: n.iter must be specified *including* the burn-in
		as.mcmc.list(bugs(model.file=file.model, data=syntax2$data,
			inits=syntax2$inits, n.chains=model$n.chain,
			parameters.to.save=syntax1$vars, codaPkg=FALSE,
			n.burnin=n.adapt, n.iter=n.adapt+n.iter, n.thin=thin))
		# Note: does not always work on Unix systems due to a problem
		# with Wine not being able to access the R temporary path.
		# Can be fixed by creating a temporary directory in the Wine
		# C: drive:
		#		mkdir ~/.wine/drive_c/bugstmp
		# And then adding these arguments to the BUGS call:
		#		working.directory='~/.wine/drive_c/bugstmp', clearWD=TRUE
	}
	unlink(file.model)

	# return
	data
}

mtc.run.jags <- function (model, package, n.adapt=n.adapt, n.iter=n.iter, thin=thin) {
	# generate JAGS model
	syntax <- mtc.build.syntaxModel(model, is.jags=TRUE)

	# compile JAGS model
	file.model <- tempfile()
	cat(paste(syntax$model, "\n", collapse=""), file=file.model)
	jags <- jags.model(file.model, data=syntax$data, inits=syntax$inits, n.chains=model$n.chain, n.adapt=n.adapt)
	unlink(file.model)

	# run JAGS model
	coda.samples(jags, variable.names=syntax$vars, n.iter=n.iter, thin=thin)
}

# Semi-internal utility for loading samples from previous simulations
# Samples that can be loaded were saved using dput
read.mtc.result.samples <- function(file, model, sampler=NULL) {
	samples <- dget(file)
	result <- list(
		samples=samples, 
		model=model, 
		sampler=sampler)
	class(result) <- "mtc.result"
	result
}

mtc.parameters <- function(object) { 
	UseMethod('mtc.parameters', object)
}

mtc.parameters.mtc.model <- function(model) {
	mtc.basic.parameters(model)
}

mtc.parameters.mtc.result <- function(result) {
	colnames(result$samples[[1]])
}
