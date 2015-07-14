# Arm-level effect estimate (given a one-row data frame)
# Returns mean, standard deviation.
mtc.arm.mle.binom.logit <- function(data, k=0.5) {
  s <- unname(data['responders'] + k)
  f <- unname(data['sampleSize'] - s + 2 * k)
  c('mean'=log(s/f), 'sd'=sqrt(1/s + 1/f))
}

# Relative effect estimate (given a two-row data frame)
mtc.rel.mle.binom.logit <- function(data, correction.force=TRUE, correction.type="constant", correction.magnitude=1) {
  correction.need <-
    data[1,'responders'] == 0 || data[1,'responders'] == data[1,'sampleSize'] ||
    data[2,'responders'] == 0 || data[2,'responders'] == data[2,'sampleSize']

  groupRatio <- if (correction.type == "reciprocal") {
    data[1,'sampleSize'] / data[2,'sampleSize']
  } else {
    1
  }

  correction <- if (correction.force || correction.need) {
    correction.magnitude * c(groupRatio/(groupRatio+1), 1/(groupRatio+1))
  } else {
    c(0, 0)
  }

  e1 <- mtc.arm.mle.binom.logit(data[1,], k=correction[1])
  e2 <- mtc.arm.mle.binom.logit(data[2,], k=correction[2])

  c(e2['mean'] - e1['mean'], sqrt(e1['sd']^2 + e2['sd']^2))
}

mtc.code.likelihood.binom.logit <- function() {
"r[i, k] ~ dbin(p[i, k], n[i, k])
logit(p[i, k]) <- mu[i] + delta[i, k]"
}

scale.log.binom.logit <- function() { TRUE }
scale.name.binom.logit <- function() { "Odds Ratio" }

# Initial values outside this range result in probability 0 or 1 for the
# binomial, which may lead to BUGS/JAGS rejecting the data
scale.limit.inits.binom.logit <- function() {
  c(-745, 36.8)
}

required.columns.ab.binom.logit <- function() {
  c('r'='responders', 'n'='sampleSize')
}

validate.data.binom.logit <- function(data.ab) {
  stopifnot(all(data.ab[['sampleSize']] >= data.ab[['responders']]))
  stopifnot(all(data.ab[['sampleSize']] > 0))
  stopifnot(all(data.ab[['responders']] >= 0))
}
