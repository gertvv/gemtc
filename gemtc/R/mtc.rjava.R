## mtc.network class methods
print.mtc.network <- function(x, ...) {
	cat("MTC dataset: ", x$description, "\n", sep="")
}

summary.mtc.network <- function(x, ...) {
	x
}

plot.mtc.network <- function(x, ...) {
	# FIXME: graph of treatments w/ comparisons as edges?
}

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

# Convert the S3 class 'mtc.network' to an org.drugis.mtc.model.Network and write it to file
write.mtc.network <- function(network, file="") {
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
	#.jcall(j.network, "V", "setDescription", network$description)

	# write to file
	bos <- .jnew("java/io/ByteArrayOutputStream")
	os <- .jcast(bos, "java/io/OutputStream")
	.jcall("org/drugis/mtc/model/JAXBHandler", "V",
		"writeNetwork", j.network, os)
	cat(.jcall(bos, "S", "toString"))
}


## mtc.model class methods

print.mtc.model <- function(x, ...) {
	cat(x$type, "model\n")
	p <- .jcall(x$model, "Lorg/drugis/mtc/Parametrization;", "parametrization")
	b <- .jcall(p, "Lorg/drugis/mtc/FundamentalGraphBasis;", "basis")
	cat(.jcall(b, "S", "dotString"))
	cat("\n")
}

# Get a list of studies including t1 and t2
mtc.network.supportingStudies <- function(network, t1, t2) {
	comparison <- .newTuple2(.jcast(mtc.network.treatment(network, t1), "java/lang/Object"), .jcast(mtc.network.treatment(network, t2), "java/lang/Object"))
	set <- .jcall(network$jobj, "Lscala/collection/immutable/Set;", "supportingStudies", comparison)
	.setToArray(set)
}

# Read JAGS/R input string format to an environment
jagsFormatToList <- function(str) {
	tmpFile <- tempfile()
	cat(paste(str, "\n", collapse=""), file=tmpFile)
	env <- new.env()
	sys.source(tmpFile, env)
	unlink(tmpFile)
	as.list(env)
}

# Extract monitored vars from JAGS script (HACK)
extractVars <- function(script) {
	lines <- unlist(strsplit(script, "\n"))
	monitors <- lines[grepl("^monitor ", lines)]
	sub("monitor ", "", monitors)
}

# Create JAGS model, generate required texts
generateJags <- function(jagsModel, generator, nchain) {
	modelTxt <- .jcall(jagsModel, "S", "modelText")
	data <- jagsFormatToList(.jcall(jagsModel, "S", "dataText"))
	inits <- lapply(1:nchain, function(i) {jagsFormatToList(.jcall(jagsModel, "S", "initialValuesText", generator))})
	vars <- extractVars(.jcall(jagsModel, "S", "scriptText", "baseName", integer(1), integer(1), integer(1)))
	analysis <- jagsFormatToList(.jcall(jagsModel, "S", "analysisText", "baseName"))
	list(model=modelTxt, data=data, inits=inits, vars=vars, analysis=analysis)
}

# Create the specific model (consistency/inconsistency/nodesplit)
# FIXME: support nodesplit
mtc.model <- function(network, type="Consistency", t1=NULL, t2=NULL, factor=2.5, nchain=4) {
	class <- paste("org/drugis/mtc/", type, "NetworkModel", sep="")
	model <- .jcall(class, "Lorg/drugis/mtc/NetworkModel;", "apply", network$jobj)
	if (is.jnull(model)) {
		stop("Error: failed to initialize NetworkModel")
	}
	rng <- .jnew("org/apache/commons/math/random/JDKRandomGenerator")
	generator <- .jcall("org/drugis/mtc/RandomizedStartingValueGenerator", "Lorg/drugis/mtc/StartingValueGenerator;", "apply", model, .jcast(rng, "org/apache/commons/math/random/RandomGenerator"), factor)
	if (is.jnull(generator)) {
		stop("Error: failed to initialize initial values generator")
	}
	jagsModel <- .jnew("org/drugis/mtc/jags/JagsSyntaxModel", model)
	if (is.jnull(jagsModel)) {
		stop("Error: failed to initialize JagsSyntaxModel")
	}
	rval <- list(model=model, jagsModel=jagsModel, jags=generateJags(jagsModel, generator, nchain), type=type)
	class(rval) <- "mtc.model"
	rval
}


# Run the model using JAGS
mtcJags <- function(mtc.model, nadapt=30000, nsamples=20000) {
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
