#' @include likelihoods.R

# Arm-level effect estimate (given a one-row data frame)
mtc.arm.mle.normal.smd <- function(data, k=0.5) {
  c('mean'=as.numeric(data['mean']), 'sd'=as.numeric(data['std.dev'])/sqrt(as.numeric(data['sampleSize'])))
}

# Relative effect estimate (given a two-row data frame)
mtc.rel.mle.normal.smd <- function(data, correction.force=TRUE, correction.type="constant", correction.magnitude=1) {
  e1 <- mtc.arm.mle.normal.smd(data[1,])
  e2 <- mtc.arm.mle.normal.smd(data[2,])
  c(e2['mean'] - e1['mean'], sqrt(e1['sd']^2 + e2['sd']^2))
}

likelihood.code.normal.smd <- list(
  read.template("gemtc.likelihood.normal.smd.txt"),
  read.template("gemtc.likelihood.normal.smd.power.txt"))

mtc.code.likelihood.normal.smd <- function(powerAdjust) {
  paste("theta[i, k] <- ($armLinearModel$) * sqrt(sum((n[i,1:na[i]]-1)*pow(s[i,1:na[i]], 2))/(sum(n[i,1:na[i]])-na[i]))/(1-(3/((4*(sum(n[i,1:na[i]])-na[i]))-1)))", likelihood.code.normal.smd[powerAdjust + 1], sep="\n")
}

fitted.values.parameter.normal.smd <- fitted.values.parameter.normal
deviance.normal.smd <- deviance.normal

scale.log.normal.smd <- function() { FALSE }
scale.name.normal.smd <- function() { "Standardized Mean Difference" }

inits.info.normal.smd <- function() {
  list(
    limits=c(-Inf, +Inf),
    param='mu',
    transform=identity)
}

required.columns.ab.normal.smd <- function() {
  c('m'='mean', 's'='std.dev', 'n'='sampleSize')
}

validate.data.normal.smd <- function(data.ab) {
  stopifnot(all(data.ab[['std.dev']] > 0))
  stopifnot(all(data.ab[['sampleSize']] > 0))
}

study.baseline.priors.normal.smd <- function() {
"for (i in studies.a) {
  mu[i] ~ dnorm(0, prior.prec)
}
"
}
