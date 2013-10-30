# Regression test for treatments getting mixed up in ANOHE.
# This example used to break the summary().

library(gemtc)
network <- read.mtc.network("anohe-breaking.gemtc")
anohe <- mtc.anohe(network, n.adapt=1000, n.iter=5000)
x <- summary(anohe)
stopifnot('studyEffects' %in% names(x))
