library(rcppbugs)
library(coda)

data <- read.table('data.txt', header=T)
nt <- max(c(data$t1, data$t2)) # number of treatments
ns <- nrow(data) # number of studies

# Random effect std. dev. prior
sd.d <- mcmc.uniform(0.3, lower=0, upper=3)
prec.d <- deterministic(function(sd.d) { 1 / sd.d^2 }, sd.d)

# Study baseline effect priors
mu <- mcmc.normal(rnorm(ns), mu=0, tau=1E-3)

# Relative effect priors
d <- mcmc.normal(rnorm(nt), mu=0, tau=1E-3)

# Study-level random effects
d.s <- deterministic(function(d) {
	d <- c(0, d)
	d[data$t2] - d[data$t1]
}, d)
delta <- mcmc.normal(rnorm(ns), mu=d.s, tau=prec.d)

# Arm-level effects
theta1 <- deterministic(identity, mu)
theta2 <- deterministic(function(mu, delta) { mu + delta }, mu, delta)
#theta2 <- deterministic(function(mu, delta) { mu + delta }, mu, d.s)

# Inverse logit link
logit <- function(p) { log(p) - log(1 - p) }
ilogit <- function(x) { exp(x) / (1 + exp(x)) }
p1 <- deterministic(ilogit, theta1)
p2 <- deterministic(ilogit, theta2)

# Likelihood
lik1 <- mcmc.binomial(as.double(data$r1), n=as.double(data$n1), p=p1, observed=TRUE)
lik2 <- mcmc.binomial(as.double(data$r2), n=as.double(data$n2), p=p2, observed=TRUE)

m <- create.model(sd.d, prec.d, mu, d, d.s, delta, theta1, theta2, p1, p2, lik1, lik2)

ans <- run.model(m, iterations=1e5L, burn=1e4L, adapt=1e3L, thin=10L)

as.mcmc.rcppbugs <- function(samples, extract=names(samples)) {
	col.names <- lapply(extract, function(name) {
		var <- samples[[name]]
		if (is.matrix(var)) {
			paste(name, "[", 1:ncol(var), "]", sep="")
		} else if (is.null(var)) {
			character()
		} else {
			name
		}
	})
	data <- do.call(cbind, samples[extract])
	colnames(data) <- do.call(c, col.names)
	as.mcmc(data)
}
result <- as.mcmc.rcppbugs(ans)

