library(gemtc)

generate.summaries <- function(result) {
	data <- as.matrix(result$samples)
	data <- data[, grep("^d\\.", colnames(data))]
	list(
		effectiveSize = effectiveSize(result$samples),
		summary       = summary(result$samples),
		cov           = cov(as.matrix(data)),
		ranks         = rank.probability(result)
	)
}

compare.summaries <- function(s1, s2) {
	stopifnot(names(s1$effectiveSize) == names(s2$effectiveSize))
	d.idx <- grep("^d\\.", names(s1$effectiveSize))
	sd.idx <- grep("^sd\\.", names(s1$effectiveSize))

	# Test equality of means
	mu1 <- s1$summary$statistics[, 'Mean']
	mu2 <- s2$summary$statistics[, 'Mean']
	se1 <- s1$summary$statistics[, 'Time-series SE']
	se2 <- s2$summary$statistics[, 'Time-series SE']
	test <- pnorm(mu1 - mu2, 0, sqrt(se1^2 + se2^2))
	cat("Test equality of means: \n")
	print(test)
	if(!all(test > 0.025)) {
		print("!!! TEST FAILED")
	}

	# Test equality of variance
	sd1 <- s1$summary$statistics[d.idx, 'SD']
	sd2 <- s2$summary$statistics[d.idx, 'SD']
	en1 <- s1$effectiveSize[d.idx]
	en2 <- s2$effectiveSize[d.idx]
	test <- pf(sd1^2 / sd2^2, en1, en2)
	cat("Test equality of variances: \n")
	print(test)
	if (!all(test > 0.025)) {
		print("!!! TEST FAILED")
	}

	# TODO: multivariate test for equality of means
	# TODO: compare covariance matrices

	# TODO: compare quantiles for the standard deviation using http://www.jstor.org/stable/2673594

	# Test equality of rank probabilities
	thin <- s2$summary$thin
	n.adapt <- s2$summary$start - thin
	n.iter <- s2$summary$end - n.adapt
	test <- sapply(rownames(s2$ranks), function(alt) {
			x <- round(s2$ranks[alt, ] * min(s2$effectiveSize[d.idx]))
			p <- s1$ranks[alt, ]
			test <- chisq.test(x, p=p, rescale.p=TRUE, simulate.p.value=TRUE)
			c('statistic'=unname(test$statistic), 'p.value'=test$p.value)
  })
	cat("Test equality of rank probabilities (Chi-squared based on effective sample size): \n")
	print(test)
	if (!all(test['p.value', ] > 0.025)) {
		print("!!! TEST FAILED")
	}
}

replicate.example <- function(name, sampler) {
	s1 <- dget(paste(name, 'summaries.txt', sep='.'))

	n.chain <- s1$summary$nchain
	thin <- s1$summary$thin
	n.adapt <- s1$summary$start - thin
	n.iter <- s1$summary$end - n.adapt

	network <- read.mtc.network(paste(name, 'gemtc', sep='.'))
	model <- mtc.model(network, n.chain=4)
	result <- mtc.run(model, sampler=sampler, n.adapt=n.adapt, n.iter=n.iter, thin=thin)
	s2 <- generate.summaries(result)
	list(s1=s1, s2=s2)
}

verify.example <- function(name, sampler) {
	cat(paste("=== Verifying", name, "===\n"))
	x <- replicate.example(name, sampler)
	compare.summaries(x$s1, x$s2)
}

verify.example.jags <- function(name) {
	verify.example(name, "JAGS")
}

verify.example.winbugs <- function(name) {
	verify.example(name, "R2WinBUGS")
}

verify.example.openbugs <- function(name) {
	verify.example(name, "BRugs")
}

examples <- c('cipriani-efficacy', 'luades-smoking', 'luades-thrombolytic', 'parkinson', 'welton-cholesterol', 'welton-diastolic', 'welton-systolic')

# lapply(examples, function(name) { x <- replicate.example(name, "rjags")$s2; dput(x, paste(name, "summaries.txt", sep=".")) })
