# Arm-level effect estimate (given a one-row data frame)
# Returns mean, standard deviation.
mtc.arm.mle.binom.log <- function(data) {
  s <- unname(data['responders'] + 0.5)
  f <- unname(data['sampleSize'] + 1)
  c('mean'=log(s/f), 'sd'=sqrt(1/s - 1/f))
}

# Relative effect estimate (given a two-row data frame)
mtc.rel.mle.binom.log <- function(data) {
  e1 <- mtc.arm.mle.binom.log(data[1,])
  e2 <- mtc.arm.mle.binom.log(data[2,])
  c(e2['mean'] - e1['mean'], sqrt(e1['sd']^2 + e2['sd']^2))
}

mtc.code.likelihood.binom.log <- function() {
"r[i, k] ~ dbin(p[i, k], n[i, k])
log(p[i, k]) <- mu[i] + delta[i, k]"
}

scale.log.binom.log <- function() { TRUE }
scale.name.binom.log <- function() { "Risk Ratio" }

# Initial values outside this range result in probability 0 or 1 for the
# binomial, which may lead to BUGS/JAGS rejecting the data
scale.limit.inits.binom.log <- function() {
  c(-745, -1E-7)
}

required.columns.ab.binom.log <- function() {
  c('r'='responders', 'n'='sampleSize')
}

validate.data.binom.log <- function(data.ab) {
  stopifnot(all(data.ab[['sampleSize']] >= data.ab[['responders']]))
  stopifnot(all(data.ab[['sampleSize']] > 0))
  stopifnot(all(data.ab[['responders']] >= 0))
}
