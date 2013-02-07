library(gemtc)

generate.summaries <- function(result) {
	data <- as.matrix(result$samples)
	data <- data[, grep("^d\\.", colnames(data))]
	list(
		effectiveSize = effectiveSize(result$samples),
		summary       = summary(result$samples),
		cov           = cov(as.matrix(data))
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
}

verify.example <- function(name) {
	cat(paste("=== Verifying", name, "==="))
	s1 <- dget(paste(name, 'summaries.txt', sep='.'))

	n.chain <- s1$summary$nchain
	thin <- s1$summary$thin
	n.adapt <- s1$summary$start - thin
	n.iter <- s1$summary$end - n.adapt

	network <- read.mtc.network(paste(name, 'gemtc', sep='.'))
	model <- mtc.model(network, n.chain=4)
	result <- mtc.run(model, sampler="JAGS", n.adapt=n.adapt, n.iter=n.iter, thin=thin)

	s2 <- generate.summaries(result)
	compare.summaries(s1, s2)
}

examples <- c('cipriani-efficacy', 'luades-smoking', 'luades-thrombolytic', 'parkinson', 'welton-cholesterol', 'welton-diastolic', 'welton-systolic')

lapply(examples, verify.example)
