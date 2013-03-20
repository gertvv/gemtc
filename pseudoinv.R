library(gemtc)
library(rjags)
library(coda)

source('gemtc/R/ll.binom.logit.R')
source('gemtc/R/ll.normal.identity.R')
source('gemtc/R/ll.call.R')

all.pair.matrix <- function(m) {
    do.call(rbind, lapply(1:(m - 1), function(i) {
        do.call(rbind, lapply((i + 1):m, function(j) {
			c(i, j)
        }))
    }))
}

delta.names <- function(i, na) {
	paste("delta[", i, ",", 2:na, "]", sep="")
}

# Modify the given model to an independent study effects model
independent.study.effects <- function(model) {
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
}\n"

	study.data <- model$data
	study.data$t <- NULL
	study.inits <- lapply(model$inits, function(inits) {
		list(
			mu=inits$mu,
			delta=inits$delta)
	})
	study.code <- sub(
		'$likelihood$',
		ll.call("mtc.code.likelihood", model),
		code, fixed=TRUE)

	model$code <- study.code
	model$data <- study.data
	model$inits <- study.inits
	model$type <- "Independent study effects"

	model
}

# Decompose trials based on a consistency model and study.samples
decompose.trials <- function(study.samples, ts, mu0, prec0) {
	decompose.study <- function(samples, mu0, prec0) {
		na <- ncol(samples) + 1
		samples <- cbind(0, samples)
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

		prec <- -L
		diag(prec) <- 0
		se <- sqrt(1/prec)

		pairs <- all.pair.matrix(na)
		list(
			mu = apply(pairs, 1, function(p) { mu[p[1], p[2]] }),
			se = apply(pairs, 1, function(p) { se[p[1], p[2]] })
		)
	}

	study.samples <- as.matrix(study.samples)

	decomposed <- lapply(1:nrow(ts), function(i) {
		study <- ts[i, ]
		study <- study[!is.na(study)]
		na <- length(study)
		if (na > 2) {
			data <- decompose.study(
				study.samples[, delta.names(i, na)],
				mu0, prec0)
			ts <- matrix(study[all.pair.matrix(na)], ncol=2)
			list(m=data$mu, e=data$se, t=ts)
		} else {
			samples <- study.samples[ , delta.names(i, 2)]
			list(m=mean(samples), e=sd(samples), t=matrix(study, nrow=1))
		}
	})

	# (mu1, prec1): posterior parameters
	mu1 <- do.call(c, lapply(decomposed, function(x) { x$m }))
	sigma1 <- do.call(c, lapply(decomposed, function(x) { x$e }))
	prec1 <- 1/sigma1^2

	# Factor out the priors
	prec <- prec1 - prec0
	mu <- (mu1 * prec1 - mu0 * prec0) * 1/prec
	sigma <- sqrt(1/prec)

	ts <- do.call(rbind, lapply(decomposed, function(x) { x$t }))

	list(t=ts, m=mu, e=sigma)
}

network <- read.mtc.network(system.file('extdata/luades-smoking.gemtc', package='gemtc'))
## disable multi-arm trials
#network$data <- network$data[!(network$data$study %in% c('09', '20')), ]
#network$data <- network$data[!(network$data$study %in% c('01', '02')), ]
#network <- mtc.network(network$data)
## /disable multi-arm trials
## Rewrite to continuous 
if (
#	FALSE) {
	'responders' %in% colnames(network$data)) {
	data <- network$data
	r <- data$responders
	s <- data$sampleSize - r
	sel <- r == 0 | s == 0
	r[sel] <- r[sel] + 0.001
	s[sel] <- s[sel] + 0.999
	data$mean <- log(r/s)
	data$std.dev <- sqrt(1/r+1/s) * sqrt(data$sampleSize)
	data$responders <- NULL
	network <- mtc.network(data, treatments=network$treatments)
}
##
model <- mtc.model(network)
result <- mtc.run(model)

study.model <- independent.study.effects(model)

jmodel <- jags.model(textConnection(study.model$code), data=study.model$data, inits=study.model$inits, n.chains=4)
vars <- do.call(c, lapply(1:study.model$data$ns, function(i) {
	delta.names(i, study.model$data$na[i])
}))
study.samples <- coda.samples(jmodel, variable.names=vars, n.iter=5E4, thin=5)


prior.prec <- 1/(15 * model$om.scale)^2
prior.mu <- 0

decomp.data <- decompose.trials(study.samples, model$data$t, prior.mu, prior.prec)
decomp.data$ns <- length(decomp.data$m)
decomp.data$om.scale <- model$om.scale
decomp.data$nt <- nrow(model$network$treatments)

# Now what
decomp.model <- "model {
    for (i in 1:ns) {
		m[i] ~ dnorm(delta[i], prec[i])
		delta[i] ~ dnorm(md[i], tau.d)
		md[i] <- d[t[i, 2]] - d[t[i, 1]]
		prec[i] <- pow(e[i], -2)
    }
	d[1] <- 0
	for (k in 2:nt) {
		d[k] ~ dnorm(0, prior.prec)
	}
        
    prior.prec <- pow(15 * om.scale, -2)
	sd.d ~ dunif(0, om.scale)
	tau.d <- pow(sd.d, -2)
}\n"

jmodel <- jags.model(textConnection(decomp.model), data=decomp.data, n.chains=4)
decomp.samples <- coda.samples(jmodel, variable.names=c("d", "sd.d"), n.iter=1E4)
