# Arm-level effect estimate (given a one-row data frame)
# Returns mean, variance.
mtc.arm.mle.binom.logit <- function(data) {
	s <- data['responders'] + 0.5
	f <- data['sampleSize'] - s + 0.5
	c('mean'=as.numeric(log(s/f)), 'sd'=as.numeric(sqrt(1/s + 1/f)))
}

# Relative effect estimate (given a two-row data frame)
mtc.rel.mle.binom.logit <- function(data) {
	e1 <- mtc.arm.mle.binom.logit(data[1,])
	e2 <- mtc.arm.mle.binom.logit(data[2,])
	c(e2['mean'] - e1['mean'], sqrt(e1['sd']^2 + e2['sd']^2))
}

mtc.code.likelihood.binom.logit <- function() {
"			r[i, k] ~ dbin(p[i, k], n[i, k])
			logit(p[i, k]) <- mu[i] + delta[i, k]"
}
