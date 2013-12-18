# Regression test for treatments getting mixed up in ANOHE.
# This example used to break the summary().

library(gemtc)
network <- read.mtc.network(system.file("extdata/luades-thrombolytic.gemtc", package="gemtc"))
treatments <- network$treatments
treatments$id <- factor(rev(as.character(treatments$id)), levels=rev(as.character(treatments$id)))
network <- mtc.network(data=network$data.ab, treatments=treatments)
anohe <- mtc.anohe(network, n.adapt=1000, n.iter=5000)
x <- summary(anohe)
stopifnot('studyEffects' %in% names(x))
