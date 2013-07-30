# Arm-level effect estimate (given a one-row data frame)
# Returns mean, standard deviation.
mtc.arm.mle.poisson.log <- function(data) {
  r <- data['responders'] + 0.5
  E <- data['exposure']
  mu <- as.numeric(log(r/E))
  sigma <- as.numeric(sqrt(1/E))
  c('mean'=mu, 'sd'=sigma)
}

# Relative effect estimate (given a two-row data frame)
mtc.rel.mle.poisson.log <- function(data) {
  e1 <- mtc.arm.mle.poisson.log(data[1,])
  e2 <- mtc.arm.mle.poisson.log(data[2,])
  c(e2['mean'] - e1['mean'], sqrt(e1['sd']^2 + e2['sd']^2))
}

mtc.code.likelihood.poisson.log <- function() {
"r[i, k] ~ dpois(theta[i, k])
theta[i, k] <- E[i, k] * lambda[i, k]
log(lambda[i, k]) <- mu[i] + delta[i, k]"
}

scale.log.poisson.log <- function() { TRUE }
scale.name.poisson.log <- function() { "Hazard Ratio" }

# Initial values outside this range result in rate 0 for the
# poisson, which may lead to BUGS/JAGS rejecting the data
scale.limit.inits.poisson.log <- function() {
  c(-745, +Inf)
}

required.columns.ab.poisson.log <- function() {
  c('r'='responders', 'E'='exposure')
}
