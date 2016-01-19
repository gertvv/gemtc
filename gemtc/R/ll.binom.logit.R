#' @include ll-helper.counts.R
#' @include likelihoods.R

# Arm-level effect estimate (given a one-row data frame)
# Returns mean, standard deviation.
mtc.arm.mle.binom.logit <- function(data, k=0.5) {
  s <- unname(data['responders'] + k)
  f <- unname(data['sampleSize'] - s + 2 * k)
  c('mean'=log(s/f), 'sd'=sqrt(1/s + 1/f))
}

# Relative effect estimate (given a two-row data frame)
mtc.rel.mle.binom.logit <- function(data, correction.force=TRUE, correction.type="constant", correction.magnitude=1) {
  correction <- correction.counts(data, correction.force, correction.type, correction.magnitude)

  e1 <- mtc.arm.mle.binom.logit(data[1,], k=correction[1])
  e2 <- mtc.arm.mle.binom.logit(data[2,], k=correction[2])

  c(e2['mean'] - e1['mean'], sqrt(e1['sd']^2 + e2['sd']^2))
}

mtc.code.likelihood.binom.logit <- function(powerAdjust) {
  paste("logit(p[i, k]) <- $armLinearModel$", likelihood.code.binom[powerAdjust + 1], sep="\n")
}

fitted.values.parameter.binom.logit <- fitted.values.parameter.binom
deviance.binom.logit <- deviance.binom

scale.log.binom.logit <- function() { TRUE }
scale.name.binom.logit <- function() { "Odds Ratio" }

# Initial values outside this range result in probability 0 or 1 for the
# binomial, which may lead to BUGS/JAGS rejecting the data
inits.info.binom.logit <- function() {
  list(
    limits=c(-745, 36.8),
    param='mu',
    transform=identity)
}

required.columns.ab.binom.logit <- required.columns.counts
validate.data.binom.logit <- validate.data.counts

study.baseline.priors.binom.logit <- function() {
"for (i in studies.a) {
  mu[i] ~ dnorm(0, prior.prec)
}
"
}
