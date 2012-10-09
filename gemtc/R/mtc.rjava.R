# Get a list of included treatments from an org.drugis.mtc.model.Network
mtc.treatments <- function(network) {
	asTreatment <- function(jobj) { .jcast(jobj, "org/drugis/mtc/model/Treatment") }
	getId <- function(treatment) { .jcall(treatment, "S", "getId") }
	getDesc <- function(treatment) { .jcall(treatment, "S", "getDescription") }

	lst <- as.list(.jcall(network, "Lcom/jgoodies/binding/list/ObservableList;", "getTreatments"))
	lst <- lapply(lst, asTreatment)
	ids <- sapply(lst, getId)
	as.data.frame(list(
		id = ids,
		description = sapply(lst, getDesc)
	), row.names = ids)
}

# Get the included data from an org.drugis.mtc.model.Network
mtc.data <- function(network) {
	as <- function(type, jobj) { 
		class <- paste("org/drugis/mtc/model", type, sep="/")
		.jcast(jobj, class)
	}
	getId <- function(jobj) { .jcall(jobj, "S", "getId") }
	getBoxedInt <- function(jobj, method) {
		.jcall(.jcall(jobj, "Ljava/lang/Integer;", method), "I", "intValue")
	}
	getBoxedDouble <- function(jobj, method) {
		.jcall(.jcall(jobj, "Ljava/lang/Double;", method), "D", "doubleValue")
	}

	convertNone <- function(m) {
		t <- .jcall(m$measurement, "Lorg/drugis/mtc/model/Treatment;", "getTreatment")
		s <- m$study
		list(study=getId(as("Study", s)), treatment=getId(as("Treatment", t)))
	}
	convertDichotomous <- function(m) {
		measurement <- convertNone(m)
		measurement$responders <- getBoxedInt(m$measurement, "getResponders")
		measurement$sampleSize <- getBoxedInt(m$measurement, "getSampleSize")
		measurement
	}
	convertContinuous <- function(m) {
		measurement <- convertNone(m)
		measurement$mean <- getBoxedDouble(m$measurement, "getMean")
		measurement$sd <- getBoxedDouble(m$measurement, "getStdDev")
		measurement$sampleSize <- getBoxedInt(m$measurement, "getSampleSize")
		measurement
	}

	dataType <- .jcall(.jcall(network, "Lorg/drugis/mtc/data/DataType;", "getType"), "S", "value")
	convert <- if (dataType == "rate") {
		convertDichotomous
	} else if (dataType == "continuous") {
		convertContinuous
	} else {
		convertNone
	}

	getStudyMeasurements <- function(study) {
		lst <- as.list(.jcall(study, "Lcom/jgoodies/binding/list/ObservableList;", "getMeasurements"))
		lst <- lapply(lst, function(x) { convert(list(study=study, measurement=as("Measurement", x))) })
		do.call(function(...) { mapply(c, ..., SIMPLIFY=FALSE) }, lst)
	}

	lst <- as.list(.jcall(network, "Lcom/jgoodies/binding/list/ObservableList;", "getStudies"))
	lst <- lapply(lst, function(x) { getStudyMeasurements(as("Study", x)) })
	as.data.frame(do.call(function(...) { mapply(c, ..., SIMPLIFY=FALSE) }, lst))
}

# Read an org.drugis.mtc.model.Network from file and convert it to the S3 class 'mtc.network'
read.mtc.network <- function(file) {
	is <- .jcast(.jnew("java/io/FileInputStream", file), "java/io/InputStream")
	j.network <- .jcall("org/drugis/mtc/model/JAXBHandler", "Lorg/drugis/mtc/model/Network;", "readNetwork", is)

	network <- list(
		description=.jcall(j.network, "S", "getDescription"),
		treatments=mtc.treatments(j.network),
		data=mtc.data(j.network)
	)
	class(network) <- "mtc.network"
	network
}

mtc.network.as.java <- function(network) {
	treatment <- function(row) {
		treatment <- .jnew("org/drugis/mtc/model/Treatment", row['id'], row['description'])
		.jcast(treatment, "java/lang/Object")
	}
	treatments <- apply(network$treatments, 1, treatment)

	appendNone <- function(builder, measurement) {
		.jcall(builder, "V", "add",
			measurement['study'], treatments[[measurement['treatment']]])
	}
	appendDichotomous <- function(builder, measurement) {
		.jcall(builder, "V", "add",
			measurement['study'], treatments[[measurement['treatment']]],
			as.integer(measurement['responders']), as.integer(measurement['sampleSize']))
	}
	appendContinuous <- function(builder, measurement) {
		.jcall(builder, "V", "add",
			measurement['study'], treatments[[measurement['treatment']]],
			as.numeric(measurement['mean']), as.numeric(measurement['sd']), as.integer(measurement['sampleSize']))
	}

	createBuilder <- function(type) {
		class <- paste("org/drugis/mtc/model/", type, "NetworkBuilder", sep="")
		jni <- paste("L", class, ";", sep="")
		.jcall(class, jni, "createSimple")
	}

	builder <- if ("responders" %in% colnames(network$data)) {
		list(append=appendDichotomous, builder=createBuilder("Dichotomous"))
	} else if ("mean" %in% colnames(network$data)) {
		list(append=appendContinuous, builder=createBuilder("Continuous"))
	} else {
		list(append=appendNone, builder=createBuilder("None"))
	}

	# create network
	apply(network$data, 1, function(row) { builder$append(builder$builder, row) })
	j.network <- .jcall(builder$builder, "Lorg/drugis/mtc/model/Network;", "buildNetwork")
	.jcall(j.network, "V", "setDescription", network$description)

	j.network
}

# Convert the S3 class 'mtc.network' to an org.drugis.mtc.model.Network and write it to file
write.mtc.network <- function(network, file="") {
	j.network <- mtc.network.as.java(network)

	# write to file
	bos <- .jnew("java/io/ByteArrayOutputStream")
	os <- .jcast(bos, "java/io/OutputStream")
	.jcall("org/drugis/mtc/model/JAXBHandler", "V",
		"writeNetwork", j.network, os)
	cat(.jcall(bos, "S", "toString"))
}

# Create the specific model (consistency/inconsistency/nodesplit)
# FIXME: support nodesplit
mtc.model <- function(network, type="Consistency", t1=NULL, t2=NULL, factor=2.5, n.chain=4) {
	typeMap <- c(
		'Consistency'='Consistency',
		'consistency'='Consistency',
		'cons'='Consistency',
		'NodeSplit'='NodeSplit',
		'nodeSplit'='NodeSplit',
		'nodesplit'='NodeSplit',
		'split'='NodeSplit',
		'Inconsistency'='Inconsistency',
		'inconsistency'='Inconsistency',
		'incons'='Inconsistency')

	if (is.na(typeMap[type])) {
		stop(paste(type, 'is not an MTC model type.'))
	}
	type <- typeMap[type]

	# create java network structure
	j.network <- mtc.network.as.java(network)

	# create parameterization
	class <- paste('org/drugis/mtc/parameterization/', type, 'Parameterization', sep='')
	j.model <- .jcall(class, paste('L', class, ';', sep=''), 'create', j.network)

	# create starting value generator
	rng <- .jcast(.jnew('org/apache/commons/math3/random/JDKRandomGenerator'), 'org/apache/commons/math3/random/RandomGenerator')
	j.cgraph <- .jcall('org/drugis/mtc/parameterization/NetworkModel',
		'Ledu/uci/ics/jung/graph/UndirectedGraph;',
		'createComparisonGraph', j.network)
	j.generator <- .jcall('org/drugis/mtc/parameterization/AbstractDataStartingValueGenerator',
		'Lorg/drugis/mtc/parameterization/StartingValueGenerator;',
		'create', j.network, j.cgraph, rng, factor)

	# create data structure
	model <- list(
		type = type,
		description = network$description,
		j.network = j.network,
		j.model = j.model,
		j.generator = j.generator,
		n.chain = n.chain,
		var.scale = factor)
	class(model) <- "mtc.model"

	model
}

# If is.na(sampler), a sampler will be chosen based on availability, in this order:
# JAGS, BUGS, YADAS. When the sampler is BUGS, BRugs or R2WinBUGS will be used.
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

	if (is.na(sampler) || (!is.na(sampler) && sampler != 'YADAS')) {
		found <- NA
		i <- 1
		while (is.na(found) && i <= length(available)) {
			if (do.call(library, list(available[i], logical.return=TRUE, quietly=TRUE))) {
				found <- available[i]
			}
			i <- i + 1
		}
		if (is.na(found)) {
			stop(paste("Could not find a suitable sampler for", sampler))
		}
		sampler <- found
	}

	# Switch on sampler
	if (sampler == 'YADAS') {
		mtc.run.yadas(model, n.adapt=n.adapt, n.iter=n.iter, thin=thin)
	} else if (sampler %in% bugs) {
		mtc.run.bugs(model, package=sampler, n.adapt=n.adapt, n.iter=n.iter, thin=thin)
	} else if (sampler %in% jags) {
		mtc.run.jags(model, package=sampler, n.adapt=n.adapt, n.iter=n.iter, thin=thin)
	}
}

# Read JAGS/R input string format to an environment
jags.as.list <- function(str) {
	tmpFile <- tempfile()
	cat(paste(str, "\n", collapse=""), file=tmpFile)
	env <- new.env()
	sys.source(tmpFile, env)
	unlink(tmpFile)
	as.list(env)
}

mtc.build.syntaxModel <- function(model, is.jags) {
	j.model <- .jcast(model$j.model, 'org/drugis/mtc/parameterization/Parameterization')
	j.syntaxModel <- .jnew('org/drugis/mtc/jags/JagsSyntaxModel', model$j.network, j.model, is.jags)

	list(
		model = .jcall(j.syntaxModel, "S", "modelText"),
		data = jags.as.list(.jcall(j.syntaxModel, "S", "dataText")),
		inits = lapply(1:model$n.chain, function(i) {jags.as.list(.jcall(j.syntaxModel, "S", "initialValuesText", model$j.generator))}),
		vars = sapply(as.list(.jcall(j.model, 'Ljava/util/List;', 'getParameters')), function(p) { .jcall(p, 'S', 'getName') })
	)
}

mtc.run.yadas <- function(model, n.adapt, n.iter, thin) {
	# Build the YADAS model
	j.model <- .jcast(model$j.model, 'org/drugis/mtc/parameterization/Parameterization')
	j.settings <- .jnew('org/drugis/mtc/yadas/YadasSettings',
		as.integer(n.adapt), as.integer(n.iter), as.integer(thin),
		as.integer(model$n.chain), model$var.scale)
	j.settings <- .jcast(j.settings, 'org/drugis/mtc/MCMCSettings')

	j.yadas <- .jcall('org/drugis/mtc/yadas/YadasModelFactory',
		'Lorg/drugis/mtc/MixedTreatmentComparison;', 'buildYadasModel', model$j.network, j.model, j.settings)
  .jcall(j.yadas, "V", 'setExtendSimulation', .jcall('org/drugis/mtc/MCMCModel$ExtendSimulation', 'Lorg/drugis/mtc/MCMCModel$ExtendSimulation;', 'valueOf', 'FINISH'))

	# Run the YADAS model
	j.activityTask <- .jcall(j.yadas, 'Lorg/drugis/common/threading/activity/ActivityTask;', 'getActivityTask')
	j.task <- .jcast(j.activityTask, 'org/drugis/common/threading/Task')
	j.progress <- .jnew('org/drugis/common/threading/status/ActivityTaskProgressModel', j.activityTask)

	pb <- txtProgressBar(style=3)
	progress <- function() { 
		val <- .jcall(j.progress, 'Ljava/lang/Double;', 'getProgress')
		if(!is.null(val)) { 
			val <- as.numeric(.jcall(val, 'D', 'doubleValue'))
		} else { 
			val <- NA
		}
		val
	}
	
	.jcall('org/drugis/common/threading/TaskUtil', 'V', 'start', j.task)
	while (.jcall('org/drugis/common/threading/TaskUtil', 'Z', 'isRunning', j.task)) {
		Sys.sleep(0.5)
		setTxtProgressBar(pb, progress())
	}
	close(pb)
	# Generate the results
	j.results <- .jcall(j.yadas, 'Lorg/drugis/mtc/MCMCResults;', 'getResults')
	params <- sapply(as.list(.jcall(j.results, '[Lorg/drugis/mtc/Parameter;', 'getParameters')), function(p) { .jcall(p, 'S', 'getName') })
	get.samples <- function(chain, i) {
    .jcall('org/drugis/mtc/util/ResultsUtil', '[D', 'getSamples', j.results, as.integer(i), as.integer(chain))
	}
	as.coda.chain <- function(chain) {
		samples <- sapply(params, function(p) { get.samples(chain - 1, which(params == p) - 1) })
		mcmc(samples, start=n.adapt + 1, end=n.adapt + n.iter, thin=thin)
	}
	

	as.mcmc.list(lapply(1:model$n.chain, as.coda.chain))
}

mtc.run.bugs <- function(model, package=sampler, n.adapt=n.adapt, n.iter=n.iter, thin=thin) {
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
		#   mkdir ~/.wine/drive_c/bugstmp
		# And then adding these arguments to the BUGS call:
		#   working.directory='~/.wine/drive_c/bugstmp', clearWD=TRUE
	}
	unlink(file.model)

	# return
	data
}

mtc.run.jags <- function (model, package=sampler, n.adapt=n.adapt, n.iter=n.iter, thin=thin) {
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

# Extract monitored vars from JAGS script (HACK)
vars.extract <- function(script) {
	lines <- unlist(strsplit(script, "\n"))
	monitors <- lines[grepl("^monitor ", lines)]
	sub("monitor ", "", monitors)
}

# Create JAGS model, generate required texts
generate.jags <- function(jagsModel, generator, nchain) {
	modelTxt <- .jcall(jagsModel, "S", "modelText")
	data <- jags.as.list(.jcall(jagsModel, "S", "dataText"))
	inits <- lapply(1:nchain, function(i) {jags.as.list(.jcall(jagsModel, "S", "initialValuesText", generator))})
	vars <- vars.extract(.jcall(jagsModel, "S", "scriptText", "baseName", integer(1), integer(1), integer(1)))
	analysis <- jags.as.list(.jcall(jagsModel, "S", "analysisText", "baseName"))
	list(model=modelTxt, data=data, inits=inits, vars=vars, analysis=analysis)
}

# Run the model using JAGS
mtc.jags <- function(mtc.model, nadapt=30000, nsamples=20000) {
	modelFile <- tempfile()
	cat(paste(mtc.model$jags$model, "\n", collapse=""), file=modelFile)
	data <- mtc.model$jags$data
	inits <- mtc.model$jags$inits
	jags <- jags.model(modelFile, data=data, inits=inits, nchain=length(inits), n.adapt=nadapt)
	unlink(modelFile)
	data <- coda.samples(jags, variable.names=mtc.model$jags$vars, n.iter=nsamples)
	for(i in 1:length(data)) { 
		colnames(data[[i]]) <- mtc.model$jags$vars
	}
	data
}
