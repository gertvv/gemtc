library(rjags)

dataset <- 'longterm'
burnInIter <- 50000
convIter <- 100000

data <- read.data(paste(dataset, '.cons.data', sep=""), format=c("jags", "bugs"))
inits <- list(list(d.iPCI.mPCI=0, d.mPCI.sPCI=0, sd.d=sqrt(0.5)), list(d.iPCI.mPCI=log(2), d.mPCI.sPCI=log(0.5), sd.d=sqrt(0.1)), list(d.iPCI.mPCI=log(0.5), d.mPCI.sPCI=log(2), sd.d=sqrt(0.8)))
model <- jags.model(file=paste(dataset, '.cons.model', sep=""), data=data, inits=inits, n.adapt=burnInIter, n.chains=3)
convSample <- coda.samples(model=model, variable.names=c("d.iPCI.mPCI", "d.mPCI.sPCI", "var.d"), n.iter=convIter, thin=10)

write.table(convSample[[1]], file="samples.txt", sep=",")
write.table(convSample[[2]], file="samples.txt", append=TRUE, col.names=FALSE, sep=",")
write.table(convSample[[3]], file="samples.txt", append=TRUE, col.names=FALSE, sep=",")

write.table(gelman.diag(convSample)$psrf, file="results-10k.txt", sep=",")
write.table(gelman.diag(lapply(convSample, function(x) { as.mcmc(x[1:2000,]) }))$psrf, file="results-2k.txt", sep=",")
write.table(gelman.diag(lapply(convSample, function(x) { as.mcmc(x[1:500,]) }))$psrf, file="results-0.5k.txt", sep=",")
