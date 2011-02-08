## mtcNetwork class methods
print.mtcNetwork <- function(x, ...) {
	cat(asXML.mtcNetwork(x))
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

# Initialize rJava
library(rJava)
.jinit()

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

# Create the specific model (consistency/inconsistency/nodesplit)
# FIXME: support nodesplit
mtcModel <- function(network, type="Consistency", t1=NULL, t2=NULL, factor=2.5) {
	class <- paste("org/drugis/mtc/", type, "NetworkModel", sep="")
	model <- .jcall(class, "Lorg/drugis/mtc/NetworkModel;", "apply", network$value)
	rng <- .jnew("org/apache/commons/math/random/JDKRandomGenerator")
	generator <- .jcall("org/drugis/mtc/RandomizedStartingValueGenerator", "Lorg/drugis/mtc/StartingValueGenerator;", "apply", model, .jcast(rng, "org/apache/commons/math/random/RandomGenerator"), factor)
	jagsModel <- .jnew("org/drugis/mtc/jags/JagsSyntaxModel", model)
	rval <- list(model=model, generator=generator, jags=jagsModel, type=type)
	class(rval) <- "mtcModel"
	rval
}



# Create JAGS model, generate required texts
dataTxt <- .jcall(jagsModel, "S", "dataText")
modelTxt <- .jcall(jagsModel, "S", "modelText")
initTxt <- .jcall(jagsModel, "S", "initialValuesText", generator) # for each chain

# Let rJags parse the texts (FIXME)
modelFile <- file()
cat(modelTxt, file=modelFile)
jags.model(modelFile) # FIXME: file connections not supported by JAGS 1.0.2?
close(modelFile)

