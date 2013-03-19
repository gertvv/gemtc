library(gemtc)
library(rjags)
library(coda)

code <- "model {
	for (i in 1:ns) {
		for (k in 1:na[i]) {
			$likelihood$
		}
		mu[i] ~ dnorm(0, prior.prec)
		delta[i, 1] <- 0
		for (k in 2:na[i]) {
			delta[i, k] ~ dnorm(0, prior.prec)
		}
	}

	prior.prec <- pow(15 * om.scale, -2)
}"

source('gemtc/R/ll.binom.logit.R')
source('gemtc/R/ll.call.R')

network <- read.mtc.network(system.file('extdata/luades-smoking.gemtc', package='gemtc'))
model <- mtc.model(network)

i <- 1
data <- model$data
na <- data$na[i]
prior.prec <- 1/(15 * model$om.scale)^2
prior.mu <- 0
study.data <- list(
	ns=1,
	na=na,
	om.scale=data$om.scale,
	r=matrix(data$r[i, ], ncol=na),
	n=matrix(data$n[i, ], ncol=na))
study.inits <- lapply(model$inits, function(inits) {
	list(
		mu=inits$mu[i],
		delta=matrix(inits$delta[i,], ncol=na))
})
study.model <- sub(
	'$likelihood$',
	ll.call("mtc.code.likelihood", model),
	code, fixed=TRUE)


jmodel <- jags.model(textConnection(study.model), data=study.data, inits=study.inits, n.chains=4)
study.samples <- coda.samples(jmodel, variable.names=c("delta"), n.iter=1E4)

samples <- as.matrix(study.samples)
mu <- sapply(1:na, function(i) {
	sapply(1:na, function(j) {
		mean(samples[ ,i] - samples[ ,j])
	})
})
# Effective variances
V <- sapply(1:na, function(i) {
    sapply(1:na, function(j) {
        var(samples[ ,i] - samples[ ,j])
    })
})

J <- matrix(1/na, ncol=na, nrow=na)
# Pseudo-inverse Laplacian
Lt <- -0.5 * (V - (1 / na) * (V %*% J + J %*% V) + (1 / na^2) * (J %*% V %*% J))
# Laplacian
L <- solve(Lt - J) + J

# Factor out the priors
# (mu1, prec1): posterior parameters
prec1 <- -L
diag(prec1) <- 0
mu1 <- mu
prec <- prec1 - prior.prec
decomp.mu <- (mu1 * prec1 - prior.mu * prior.prec) * 1/prec
diag(prec) <- 0
decomp.sigma <- sqrt(1/prec)


# Now what
decomp.model <- "model {
    for (i in 1:ns) {
        for (k in 1:(na-1)) {
			m[i, k] ~ dnorm(md[i, k], prec[i, k])
			md[i, k] <- d[t[i, k + 1]] - d[t[i, 1]]
			prec[i, k] <- pow(e[i, k], -2)
        }
    }
	d[1] <- 0
	for (k in 2:nt) {
		d[k] ~ dnorm(0, prior.prec)
	}
        
    prior.prec <- pow(15 * om.scale, -2)
}"

all.pair.matrix <- function(m) {
    do.call(rbind, lapply(1:(m - 1), function(i) {
        do.call(rbind, lapply((i + 1):m, function(j) {
			c(i, j)
        }))
    }))
}

ts <- all.pair.matrix(na)
m <- matrix(
	apply(ts, 1, function(comp) { decomp.mu[comp[1], comp[2]] }),
	nrow=nrow(ts))
e <- matrix(
	apply(ts, 1, function(comp) { decomp.sigma[comp[1], comp[2]] }),
	nrow=nrow(ts))

decomp.data <- list(ns = na * (na - 1) / 2, na = 2,nt = na, t = ts, m = m, e = e, om.scale = model$om.scale)

jmodel <- jags.model(textConnection(decomp.model), data=decomp.data)
decomp.samples <- coda.samples(jmodel, variable.names=c("d"), n.iter=1E4)
