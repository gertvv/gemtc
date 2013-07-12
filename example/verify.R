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
			n.sample <- min(s2$effectiveSize[d.idx])
			x <- round(s2$ranks[alt, ] * n.sample)
			p <- s1$ranks[alt, ]
			
			# Continuity correction because too many zero cells cause the test to fail
			x <- x + 1
			p <- (p + 1/n.sample) / (1 + nrow(s2$ranks)/n.sample)

			test <- chisq.test(x, p=p, rescale.p=TRUE, simulate.p.value=TRUE)
			c('statistic'=unname(test$statistic), 'p.value'=test$p.value)
  })
	cat("Test equality of rank probabilities (Chi-squared based on effective sample size): \n")
	print(test)
	if (!all(test['p.value', ] > 0.025)) {
		print("!!! TEST FAILED")
	}
}

replicate.example <- function(example, sampler) {
	s1 <- dget(paste(example$name, 'summaries.txt', sep='.'))

	n.chain <- s1$summary$nchain
	thin <- s1$summary$thin
	n.adapt <- s1$summary$start - thin
	n.iter <- s1$summary$end - n.adapt

	model <- mtc.model(example$network, likelihood=example$likelihood, link=example$link, n.chain=4)
	result <- mtc.run(model, sampler=sampler, n.adapt=n.adapt, n.iter=n.iter, thin=thin)
	s2 <- generate.summaries(result)
	list(s1=s1, s2=s2)
}

verify.example <- function(example, sampler) {
	cat(paste("=== Verifying", example$name, "===\n"))
	x <- replicate.example(example, sampler)
	compare.summaries(x$s1, x$s2)
}

verify.example.jags <- function(example) {
	verify.example(example, "JAGS")
}

verify.example.winbugs <- function(example) {
	verify.example(example, "R2WinBUGS")
}

verify.example.openbugs <- function(example) {
	verify.example(example, "BRugs")
}

# Examples manually verified against reported summaries from the literature.
# Validation summaries subsequently generated from verified results.
examples <- list(
	'cipriani-efficacy' = list( # Efficacy data from Cipriani et al. Lancet 2009;373:746-758.
		likelihood='binom',
		link='logit'
	),
	'luades-smoking' = list( # Smoking cessation data from Lu & Ades, J Am Stat Assoc 2006;101(474):447-459, Table 1.
		likelihood='binom',
		link='logit'
	),
	'luades-thrombolytic' = list( # Thrombolytic drugs data from Lu & Ades, J Am Stat Assoc 2006;101(474):447-459, Table 3.
		likelihood='binom',
		link='logit'
	),
	'parkinson' = list( # NICE TSD2 program 5a
		likelihood='normal',
		link='identity'
	),
	'welton-cholesterol' = list( # Welton et al., Am J Epidemiol 2009;169:1158-1165
		likelihood='normal',
		link='identity'
	),
	'welton-diastolic' = list( # Welton et al., Am J Epidemiol 2009;169:1158-1165
		likelihood='normal',
		link='identity'
	),
	'welton-systolic' = list( # Welton et al., Am J Epidemiol 2009;169:1158-1165
		likelihood='normal',
		link='identity'
	),
	'diabetes-surv' = list( # NICE TSD2 program 3a
		network = mtc.network(read.table('diabetes-surv.data.txt', header=T)),
		likelihood='binom',
		link='cloglog'
	),
	'parkinson-shared' = list( # NICE TSD2 program 8a
		network = mtc.network(
			data=read.table('parkinson-shared.data-ab.txt', header=T),
			data.re=read.table('parkinson-shared.data-re.txt', header=T)
		),
		likelihood='normal',
		link='identity'
	)
)

for (name in names(examples)) {
	if (is.null(examples[[name]][['network']])) {
		examples[[name]][['network']] <- read.mtc.network(paste(name, 'gemtc', sep='.'))
	}
	examples[[name]][['name']] <- name
}

# lapply(examples, function(name) { x <- replicate.example(name, "rjags")$s2; dput(x, paste(name, "summaries.txt", sep=".")) })
