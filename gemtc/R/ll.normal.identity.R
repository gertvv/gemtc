#' @include likelihoods.R

# Arm-level effect estimate (given a one-row data frame)
mtc.arm.mle.normal.identity <- function(data, k=0.5) {
  c('mean'=as.numeric(data['mean']), 'sd'=as.numeric(data['std.err']))
}

# Relative effect estimate (given a two-row data frame
mtc.rel.mle.normal.identity <- function(data, correction.force=TRUE, correction.type="constant", correction.magnitude=1) {
  e1 <- mtc.arm.mle.normal.identity(data[1,])
  e2 <- mtc.arm.mle.normal.identity(data[2,])
  c(e2['mean'] - e1['mean'], sqrt(e1['sd']^2 + e2['sd']^2))
}

mtc.code.likelihood.normal.identity <- function(powerAdjust) {
  paste("theta[i, k] <- $armLinearModel$", likelihood.code.normal[powerAdjust + 1], sep="\n")
}

fitted.values.parameter.normal.identity <- fitted.values.parameter.normal
deviance.normal.identity <- deviance.normal

scale.log.normal.identity <- function() { FALSE }
scale.name.normal.identity <- function() { "Mean Difference" }

inits.info.normal.identity <- function() {
  list(
    limits=c(-Inf, +Inf),
    param='mu',
    transform=identity)
}

required.columns.ab.normal.identity <- function() {
  c('m'='mean', 'e'='std.err')
}

validate.data.normal.identity <- function(data.ab) {
  stopifnot(all(data.ab[['std.err']] > 0))
}

study.baseline.priors.normal.identity <- function() {
"for (i in studies.a) {
  mu[i] ~ dnorm(0, prior.prec)
}
"
}
