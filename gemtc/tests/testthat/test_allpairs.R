# Test calculation of all-pairs MLE estimates (needed for priors, starting values)
context("rel.mle.[ab|re]")

test_that("a single pair returns a one-row matrix", {
  data <- data.frame(treatment=c("A", "B"), mean=c(1.0, 2.0), std.err=c(0.5/4, 0.5/4))
  model <- list("likelihood"="normal", "link"="identity")
  pairs <- data.frame(t1=data$treatment[1], t2=data$treatment[2])
  expected <- matrix(c('mean'=1.0, 'sd'=sqrt(2*0.125^2)), nrow=1, ncol=2)
  colnames(expected) <- c('mean', 'sd')
  expect_that(rel.mle.ab(data, model, pairs), equals(expected))
})

test_that("two pairs return a two-row matrix", {
  data <- data.frame(treatment=c("A", "B", "C"), mean=c(1.0, 2.0, 2.5), std.err=c(0.5/4, 0.5/4, 1.0/4))
  model <- list("likelihood"="normal", "link"="identity")
  ts <- data$treatment
  pairs <- data.frame(t1=coerce.factor(c(ts[1], ts[1]), ts), t2=coerce.factor(c(ts[2], ts[3]), ts))
  expected <- matrix(c(1.0, sqrt(2*0.125^2), 1.5, sqrt(0.125^2 + 0.25^2)), ncol=2, byrow=TRUE)
  colnames(expected) <- c('mean', 'sd')
  expect_that(rel.mle.ab(data, model, pairs), equals(expected))
})

test_that("calculating pairs for relative effect data transforms the mvnorm", {
  data <- read.table(textConnection("
study  treatment  diff  std.err
s07    A          NA    0.50
s07    B          -2.3  0.72
s07    D          -0.9  0.69"), header=T)
  ts <- data$treatment
  pairs <- data.frame(t1=coerce.factor(c(ts[3], ts[3]), ts), t2=coerce.factor(c(ts[1], ts[2]), ts))
  expected <- matrix(c(0.9, 0.69, -1.4, sqrt(0.72^2+0.69^2-2*0.50^2)), ncol=2, byrow=TRUE)
  colnames(expected) <- c('mean', 'sd')
  expect_that(rel.mle.re(data, pairs), equals(expected))
})

test_that("calculating pairs for relative effect data handles 1-pair case", {
  data <- read.table(textConnection("
study  treatment  diff  std.err
s07    A          NA    0.50
s07    B          -2.3  0.72
s07    D          -0.9  0.69"), header=T)
  ts <- data$treatment
  pairs <- data.frame(t1=coerce.factor(c(ts[3], ts[3]), ts), t2=coerce.factor(c(ts[1], ts[2]), ts))
  expected <- matrix(c(0.9, 0.69, -1.4, sqrt(0.72^2+0.69^2-2*0.50^2)), ncol=2, byrow=TRUE)
  colnames(expected) <- c('mean', 'sd')
  expect_that(rel.mle.re(data, pairs), equals(expected))
})

test_that("calculating pairs for relative effect data handles missing treatments", {
  data <- read.table(textConnection("
study  treatment  diff  std.err
s07    A          NA    0.50
s07    B          -2.3  0.72
s07    D          -0.9  0.69
s08    C          NA    0.3"), header=T)
  ts <- data$treatment
  pairs <- data.frame(t1=coerce.factor(c(ts[3]), ts), t2=coerce.factor(c(ts[2]), ts))
  expected <- matrix(c(-1.4, sqrt(0.72^2+0.69^2-2*0.50^2)), ncol=2, byrow=TRUE)
  colnames(expected) <- c('mean', 'sd')
  expect_that(rel.mle.re(data[data$study=="s07",], pairs), equals(expected))
})

test_that("guess.scale handles relative effect data", {
  data <- read.table(textConnection("
study  treatment  diff  std.err
s07    A          NA    0.50
s07    B          -2.3  0.72
s07    D          -0.9  0.69"), header=T)
  network <- mtc.network(data.re=data)

    model <- list(
        network = network,
    likelihood = 'normal',
    link = 'identity'
    )
  expect_that(guess.scale(model), equals(2.3))
})
