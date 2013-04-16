# Arm-level effect estimate (given a one-row data frame)
mtc.arm.mle.normal.identity <- function(data) {
    c('mean'=as.numeric(data['mean']), 'sd'=as.numeric(data['std.dev'] / sqrt(data['sampleSize'])))
}

# Relative effect estimate (given a two-row data frame)
mtc.rel.mle.normal.identity <- function(data) {
    e1 <- mtc.arm.mle.normal.identity(data[1,])
    e2 <- mtc.arm.mle.normal.identity(data[2,])
    c(e2['mean'] - e1['mean'], sqrt(e1['sd']^2 + e2['sd']^2))
}

mtc.code.likelihood.normal.identity <- function() {
"m[i, k] ~ dnorm(theta[i, k], prec[i, k])
theta[i, k] <- mu[i] + delta[i, k]
prec[i, k] <- pow(e[i, k], -2)"
}

scale.log.normal.identity <- function() { FALSE }
scale.name.normal.identity <- function() { "Mean Difference" }
