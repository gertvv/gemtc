library(mtc)

network <- mtcNetwork("luades-smoking.xml")
model <- mtcModel(network)
data <- mtcJags(model, 3000, 2000)

data <- append.derived(data, model$jags$analysis$deriv)
summary(data)
