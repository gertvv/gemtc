# Initialize rJava
library(rJava)
.jinit()

# Add MTC to classpath (FIXME)
.jaddClassPath("/home/gert/Documents/repositories/mtc/mtc-0.8/mtc-0.8.jar")

# Load XML & parse it
xml <- .jcall("scala/xml/XML", "Lscala/xml/Node;", "loadFile", "smoking.xml")
network <- .jcall("org/drugis/mtc/Network", "Lorg/drugis/mtc/Network;", "fromXML", xml)

# Create consistency model (FIXME)
networkModel <- .jcall("org/drugis/mtc/ConsistencyNetworkModel", "Lorg/drugis/mtc/NetworkModel;", "apply", network)

# Create starting value generator (FIXME)
rng <- .jnew("org/apache/commons/math/random/JDKRandomGenerator")
generator <- .jcall("org/drugis/mtc/RandomizedStartingValueGenerator", "Lorg/drugis/mtc/StartingValueGenerator;", "apply", networkModel, .jcast(rng, "org/apache/commons/math/random/RandomGenerator"), 2.5)

# Create JAGS model, generate required texts
jagsModel <- .jnew("org/drugis/mtc/jags/JagsSyntaxModel", networkModel)
dataTxt <- .jcall(jagsModel, "S", "dataText")
modelTxt <- .jcall(jagsModel, "S", "modelText")
initTxt <- .jcall(jagsModel, "S", "initialValuesText", generator) # for each chain

# Let rJags parse the texts (FIXME)
modelFile <- file()
cat(modelTxt, file=modelFile)
jags.model(modelFile) # FIXME: file connections not supported by JAGS 1.0.2?
close(modelFile)

