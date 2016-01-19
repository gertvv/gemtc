#' @include likelihoods.R

# Arm-level effect estimate (given a one-row data frame)
# Returns mean, standard deviation.
mtc.arm.mle.poisson.log <- function(data, k=0.5) {
  r <- data['responders'] + k
  E <- data['exposure']
  mu <- as.numeric(log(r/E))
  sigma <- as.numeric(sqrt(1/E))
  c('mean'=mu, 'sd'=sigma)
}

# Relative effect estimate (given a two-row data frame)
mtc.rel.mle.poisson.log <- function(data, correction.force=TRUE, correction.type="constant", correction.magnitude=1) {
  correction.need <- data[1,"responders"] == 0 || data[2,"responders"] == 0

  groupRatio <- if (correction.type == "reciprocal") {
    data[1,'exposure'] / data[2,'exposure']
  } else {
    1
  }

  correction <- if (correction.force || correction.need) {
    correction.magnitude * c(groupRatio/(groupRatio+1), 1/(groupRatio+1))
  } else {
    c(0, 0)
  }

  e1 <- mtc.arm.mle.poisson.log(data[1,], correction[1])
  e2 <- mtc.arm.mle.poisson.log(data[2,], correction[2])
  c(e2['mean'] - e1['mean'], sqrt(e1['sd']^2 + e2['sd']^2))
}

mtc.code.likelihood.poisson.log <- function(powerAdjust) {
  paste("log(lambda[i, k]) <- $armLinearModel$",
        "theta[i, k] <- E[i, k] * lambda[i, k]",
        likelihood.code.poisson[powerAdjust + 1], sep="\n")
}

fitted.values.parameter.poisson.log <- fitted.values.parameter.poisson
deviance.poisson.log <- deviance.poisson

scale.log.poisson.log <- function() { TRUE }
scale.name.poisson.log <- function() { "Hazard Ratio" }

# Initial values outside this range result in rate 0 for the
# poisson, which may lead to BUGS/JAGS rejecting the data
scale.limit.inits.poisson.log <- function() { c(-745, +Inf) }
inits.info.poisson.log <- function() {
  list(
    limits=c(-745, +Inf),
    param='lambda.base',
    transform=exp)
}

required.columns.ab.poisson.log <- function() {
  c('r'='responders', 'E'='exposure')
}

validate.data.poisson.log <- function(data.ab) {
  stopifnot(all(data.ab[['responders']] >= 0))
  stopifnot(all(data.ab[['exposure']] >= 0))
}

study.baseline.priors.poisson.log <- function() {
"for (i in studies.a) {
  mu[i] <- log(lambda.base[i])
  lambda.base[i] ~ dgamma(0.001, 0.001)
}
"
}
