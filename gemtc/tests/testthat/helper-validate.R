generate.summaries <- function(result) {
  sel <- colnames(result$samples[[1]]) != 'deviance'
  samples <- as.mcmc.list(lapply(result$samples, function(chain) {
    chain[ , sel]
  }))

  data <- as.matrix(samples)
  data <- data[, grep("^d\\.", colnames(data))]
  if (result$model$type == "consistency") {
    list(
      effectiveSize = effectiveSize(samples),
      summary       = summary(samples),
      cov           = cov(as.matrix(data)),
      ranks         = rank.probability(result)
    )
  } else {
    list(
      effectiveSize = effectiveSize(samples),
      summary       = summary(samples),
      cov           = cov(as.matrix(data))
    )
  }
}

formatError <- function(name, s1, s2, test) {
  str.s1 <- paste(capture.output(print(s1)), collapse="\n")
  str.s2 <- paste(capture.output(print(s2)), collapse="\n")
  str.test <- paste(capture.output(print(test)), collapse="\n")

  paste(name,
        "=========== Expected:       ===========", str.s1,
        "=========== Actual:         ===========", str.s2,
        "=========== Test statistic: ===========", str.test, sep="\n\n")
}

compare.summaries <- function(s1, s2) {
  stopifnot(names(s1$effectiveSize) == names(s2$effectiveSize))
  d.idx <- grep("^d\\.", names(s1$effectiveSize))
  sd.idx <- grep("^sd\\.", names(s1$effectiveSize))

  # Force statistics to be matrix
  if (is.vector(s1$summary$statistics)) s1$summary$statistics <- t(s1$summary$statistics)
  if (is.vector(s2$summary$statistics)) s2$summary$statistics <- t(s2$summary$statistics)

  # Test equality of means
  mu1 <- s1$summary$statistics[, 'Mean']
  mu2 <- s2$summary$statistics[, 'Mean']
  se1 <- s1$summary$statistics[, 'Time-series SE']
  se2 <- s2$summary$statistics[, 'Time-series SE']
  test <- pnorm(mu1 - mu2, 0, sqrt(se1^2 + se2^2))
  get_reporter()$add_result(
    expectation(all(test > 0.025), formatError("Means were not equal", mu1, mu2, test)))

  # Test equality of variance
  sd1 <- s1$summary$statistics[d.idx, 'SD']
  sd2 <- s2$summary$statistics[d.idx, 'SD']
  en1 <- s1$effectiveSize[d.idx]
  en2 <- s2$effectiveSize[d.idx]
  test <- pf(sd1^2 / sd2^2, en1, en2)
  get_reporter()$add_result(
    expectation(all(test > 0.025), formatError("Variances were not equal", sd1^2, sd2^2, test)))

  # TODO: multivariate test for equality of means
  # TODO: compare covariance matrices

  # TODO: compare quantiles for the standard deviation using http://www.jstor.org/stable/2673594

  # Test equality of rank probabilities
  if (!is.null(s1$ranks)) {
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
    get_reporter()$add_result(
      expectation(all(test['p.value',] > 0.025), formatError("Rank probabilities were not equal", s1$ranks, s2$ranks, test)))
  }
}

replicate.example <- function(name, network, type="consistency", linearModel="random", likelihood="binom", link="logit") {
  s1 <- dget(paste0("../data/", name, '.summaries.txt'))

  n.chain <- s1$summary$nchain
  thin <- s1$summary$thin
  n.adapt <- s1$summary$start - thin
  n.iter <- s1$summary$end - n.adapt

  model <- mtc.model(network, type=type,
    likelihood=likelihood, link=link,
    linearModel=linearModel,
    n.chain=4)
  capture.output(
    result <- mtc.run(model, n.adapt=n.adapt, n.iter=n.iter, thin=thin)
  )
  s2 <- generate.summaries(result)
  list(s1=s1, s2=s2)
}

verify.example <- function(example, sampler) {
  x <- replicate.example(example, sampler)
  compare.summaries(x$s1, x$s2)
}
