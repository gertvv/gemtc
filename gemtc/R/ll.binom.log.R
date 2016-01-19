#' @include ll-helper.counts.R
#' @include likelihoods.R

# Arm-level effect estimate (given a one-row data frame)
# Returns mean, standard deviation.
mtc.arm.mle.binom.log <- function(data, k=0.5) {
  s <- unname(data['responders'] + k)
  f <- unname(data['sampleSize'] + 2 * k)
  c('mean'=log(s/f), 'sd'=sqrt(1/s - 1/f))
}

# Relative effect estimate (given a two-row data frame)
mtc.rel.mle.binom.log <- function(data, correction.force=TRUE, correction.type="constant", correction.magnitude=1) {
  correction <- correction.counts(data, correction.force, correction.type, correction.magnitude)

  e1 <- mtc.arm.mle.binom.log(data[1,], correction[1])
  e2 <- mtc.arm.mle.binom.log(data[2,], correction[2])
  c(e2['mean'] - e1['mean'], sqrt(e1['sd']^2 + e2['sd']^2))
}

mtc.code.likelihood.binom.log <- function(powerAdjust) {
  paste("log(p[i, k]) <- min($armLinearModel$, -1E-16)", likelihood.code.binom[powerAdjust + 1], sep="\n")
}

fitted.values.parameter.binom.log <- fitted.values.parameter.binom
deviance.binom.log <- deviance.binom

scale.log.binom.log <- function() { TRUE }
scale.name.binom.log <- function() { "Risk Ratio" }

# Initial values outside this range result in probability 0 or 1 for the
# binomial, which may lead to BUGS/JAGS rejecting the data
inits.info.binom.log <- function() {
  list(
    limits=c(-745, -1E-7),
    param='p.base',
    transform=exp)
}

required.columns.ab.binom.log <- required.columns.counts
validate.data.binom.log <- validate.data.counts

study.baseline.priors.binom.log <- function() {
"for (i in studies.a) {
  mu[i] <- log(p.base[i])
  p.base[i] ~ dunif(0, 1)
}
"
}
