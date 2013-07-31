# Arm-level effect estimate (given a one-row data frame)
# Returns mean, standard deviation.
mtc.arm.mle.binom.cloglog <- function(data) {
  s <- data['responders'] + 0.5
  n <- data['sampleSize'] + 1
  mu <- unname(log(-log(1 - s/n)))
  sigma <- unname(sqrt(1/n^2)/exp(mu))
  c('mean'=mu, 'sd'=min(1, sigma))
}

# Relative effect estimate (given a two-row data frame)
mtc.rel.mle.binom.cloglog <- function(data) {
  e1 <- mtc.arm.mle.binom.cloglog(data[1,])
  e2 <- mtc.arm.mle.binom.cloglog(data[2,])
  c(e2['mean'] - e1['mean'], sqrt(e1['sd']^2 + e2['sd']^2))
}

mtc.code.likelihood.binom.cloglog <- function() {
"r[i, k] ~ dbin(p[i, k], n[i, k])
cloglog(p[i, k]) <- mu[i] + delta[i, k]"
}

scale.log.binom.cloglog <- function() { TRUE }
scale.name.binom.cloglog <- function() { "Hazard Ratio" }

# Initial values outside this range result in probability 0 or 1 for the
# binomial, which may lead to BUGS/JAGS rejecting the data
scale.limit.inits.binom.cloglog <- function() {
  c(-37.4, 3.6)
}

required.columns.ab.binom.cloglog <- function() {
  c('r'='responders', 'n'='sampleSize')
}
