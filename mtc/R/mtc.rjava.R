## mtcNetwork class methods
print.mtcNetwork <- function(x, ...) {
	cat(mtcNetwork.asXML(x))
	cat("\n")
}

summary.mtcNetwork <- function(x, ...) {
	treatments <- mtcNetwork.treatments(x)
	comps <- sapply(treatments, function(t1) { 
		sapply(treatments, function(t2) {
				if (t1 == t2) NA else length(mtcNetwork.supportingStudies(x, t1, t2))
			})
	})

	cat("Treatment comparison matrix:\n")
	print(comps)
}

plot.mtcNetwork <- function(x, ...) {
	# FIXME: graph of treatments w/ comparisons as edges?
}

print.mtcModel <- function(x, ...) {
	cat(x$type, "model\n")
	p <- .jcall(x$model, "Lorg/drugis/mtc/Parametrization;", "parametrization")
	b <- .jcall(p, "Lorg/drugis/mtc/FundamentalGraphBasis;", "basis")
	cat(.jcall(b, "S", "dotString"))
	cat("\n")
}

# Add MTC to classpath (FIXME)
.jaddClassPath("/home/gert/Documents/repositories/mtc/mtc-0.8/mtc-0.8.jar")

# Load XML & parse it
mtcNetwork <- function(file) {
	xml <- .jcall("scala/xml/XML", "Lscala/xml/Node;", "loadFile", file)
	network <- .jcall("org/drugis/mtc/Network", "Lorg/drugis/mtc/Network;", "fromXML", xml)
	mtcNetwork <- list(value=network)
	class(mtcNetwork) <- "mtcNetwork"
	mtcNetwork
}

# Return the XML string for the network
mtcNetwork.asXML <- function(network) {
	.jcall(network$value, "S", "toPrettyXML")
}

# Convert scala Set to java array
.setToArray <- function(set) {
	list <- .jcall("scala/collection/JavaConversions", "Ljava/util/List;", "asList", .jcall(set, "Lscala/collection/Seq;", "toSeq"))
	.jcall(list, "[Ljava/lang/Object;", "toArray")
}

.castToObject <- function(x) {
	.jcast(x, "java/lang/Object")
}

.newTuple2 <- function(x1, x2) {
	factory <- .jfield("scala/Tuple2$", "Lscala/Tuple2$;", "MODULE$")
	.jcall(factory, "Lscala/Tuple2;", "apply", .castToObject(x1), .castToObject(x2))
}

.size <- function(x) {
	.jcall(x, "I", "size")
}

# Get a list of included treatment IDs
mtcNetwork.treatments <- function(network) {
	set <- .jcall(network$value, "Lscala/collection/immutable/Set;", "treatments")
	sapply(.setToArray(set), function(x) { .jcall(x, "S", "id") })
}

# Get a Treatment based on ID
mtcNetwork.treatment <- function(network, id) {
	.jcall(network$value, "Lorg/drugis/mtc/Treatment;", "treatment", id)
}

# Get a list of studies including t1 and t2
mtcNetwork.supportingStudies <- function(network, t1, t2) {
	comparison <- .newTuple2(.jcast(mtcNetwork.treatment(network, t1), "java/lang/Object"), .jcast(mtcNetwork.treatment(network, t2), "java/lang/Object"))
	set <- .jcall(network$value, "Lscala/collection/immutable/Set;", "supportingStudies", comparison)
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
mtcModel <- function(network, type="Consistency", t1=NULL, t2=NULL, factor=2.5, nchain=4) {
	class <- paste("org/drugis/mtc/", type, "NetworkModel", sep="")
	model <- .jcall(class, "Lorg/drugis/mtc/NetworkModel;", "apply", network$value)
	rng <- .jnew("org/apache/commons/math/random/JDKRandomGenerator")
	generator <- .jcall("org/drugis/mtc/RandomizedStartingValueGenerator", "Lorg/drugis/mtc/StartingValueGenerator;", "apply", model, .jcast(rng, "org/apache/commons/math/random/RandomGenerator"), factor)
	jagsModel <- .jnew("org/drugis/mtc/jags/JagsSyntaxModel", model)
	rval <- list(model=model, jagsModel=jagsModel, jags=generateJags(jagsModel, generator, nchain), type=type)
	class(rval) <- "mtcModel"
	rval
}


# Run the model using JAGS
mtcJags <- function(mtcModel, nadapt=30000, nsamples=20000) {
	modelFile <- tempfile()
	cat(paste(mtcModel$jags$model, "\n", collapse=""), file=modelFile)
	data <- mtcModel$jags$data
	inits <- mtcModel$jags$inits
	jags <- jags.model(modelFile, data=data, inits=inits, nchain=length(inits), n.adapt=nadapt)
	unlink(modelFile)
	data <- coda.samples(jags, variable.names=mtcModel$jags$vars, n.iter=nsamples)
	for(i in 1:length(data)) { 
		colnames(data[[i]]) <- mtcModel$jags$vars
	}
	data
}
