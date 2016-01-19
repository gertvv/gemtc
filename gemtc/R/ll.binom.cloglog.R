#' @include ll-helper.counts.R
#' @include likelihoods.R

# Arm-level effect estimate (given a one-row data frame)
# Returns mean, standard deviation.
mtc.arm.mle.binom.cloglog <- function(data, k=0.5) {
  s <- data['responders'] + k
  n <- data['sampleSize'] + 2*k
  mu <- unname(log(-log(1 - s/n)))
  sigma <- unname(sqrt(1/n^2)/exp(mu))
  c('mean'=mu, 'sd'=min(1, sigma))
}

# Relative effect estimate (given a two-row data frame)
mtc.rel.mle.binom.cloglog <- function(data, correction.force=TRUE, correction.type="constant", correction.magnitude=1) {
  correction <- correction.counts(data, correction.force, correction.type, correction.magnitude)

  e1 <- mtc.arm.mle.binom.cloglog(data[1,], correction[1])
  e2 <- mtc.arm.mle.binom.cloglog(data[2,], correction[2])
  c(e2['mean'] - e1['mean'], sqrt(e1['sd']^2 + e2['sd']^2))
}

mtc.code.likelihood.binom.cloglog <- function(powerAdjust) {
  paste("cloglog(p[i, k]) <- $armLinearModel$", likelihood.code.binom[powerAdjust + 1], sep="\n")
}

fitted.values.parameter.binom.cloglog <- fitted.values.parameter.binom
deviance.binom.cloglog <- deviance.binom

scale.log.binom.cloglog <- function() { TRUE }
scale.name.binom.cloglog <- function() { "Hazard Ratio" }

# Initial values outside this range result in probability 0 or 1 for the
# binomial, which may lead to BUGS/JAGS rejecting the data
inits.info.binom.cloglog <- function() {
  list(
    limits=c(-37.4, 3.6),
    param='p.base',
    transform=function(theta) { 1 - exp(-exp(theta)) })
}

required.columns.ab.binom.cloglog <- required.columns.counts
validate.data.binom.cloglog <- validate.data.counts
study.baseline.priors.binom.cloglog <- function() {
"for (i in studies.a) {
  mu[i] <- cloglog(p.base[i])
  p.base[i] ~ dunif(0, 1)
}
"
}
