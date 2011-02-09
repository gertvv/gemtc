source('mtc.rjava.R')
network <- mtcNetwork("../example/luades-smoking.xml")
model <- mtcModel(network)
data <- mtcJags(model, 3000, 2000)

source('mtc.R')
data <- append.derived(data, model$jags$analysis$deriv)
summary(data)
