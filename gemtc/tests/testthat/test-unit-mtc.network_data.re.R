context("mtc.network(data.re)")

data.re <- read.table(textConnection("
study  treatment  diff  std.err
s01    A          NA    NA
s01    C          -0.31 0.67
s02    A          NA    NA
s02    B          -1.7  0.38
s03    C          NA    NA
s03    D          -0.35 0.44
s04    C          NA    NA
s04    D          0.55  0.55
s05    D          NA    NA
s05    E          -0.3  0.27
s06    D          NA    NA
s06    E          -0.3  0.32
s07    A          NA    0.50
s07    B          -2.3  0.72
s07    D          -0.9  0.69"), header=T)


test_that("either data or data.re must be specified", {
  expect_error(mtc.network())
})

test_that("data can be unspecified if data.re is given", {
  mtc.network(data.re=data.re)
})

test_that("data.re is properly stored", {
  network <- mtc.network(data.re=data.re)
  expect_that(network$data.re$study, equals(data.re$study))
  expect_that(network$data.re$treatment, equals(data.re$treatment))
  expect_that(network$data.re$diff, equals(data.re$diff))
  expect_that(network$data.re$std.err, equals(data.re$std.err))
})

data.arm <- read.table(textConnection("
  study  treatment
  s08    A
  s08    C
  s09    A
  s09    D
  s09    F"), header=TRUE)

test_that("treatments for data and data.re are merged", {
  network <- mtc.network(data.ab=data.arm, data.re=data.re)
  expect_that(levels(network$treatments$id), equals(c("A", "B", "C", "D", "E", "F")))
  expect_that(as.character(network$data.ab$treatment), equals(as.character(data.arm$treatment)))
  expect_that(as.character(network$data.re$treatment), equals(as.character(data.re$treatment)))
})

test_that("merged list of arms is correct", {
  network <- mtc.network(data.ab=data.arm, data.re=data.re)
  expect_that(as.character(mtc.merge.data(network)$study), equals(c(as.character(data.arm$study), as.character(data.re$study))))
  expect_that(as.character(mtc.merge.data(network)$treatment), equals(c(as.character(data.arm$treatment), as.character(data.re$treatment))))
})

test_that("duplicate studies raise an error", {
  data <- data.arm
  data$study <- c('s07', 's07', 's09', 's09', 's09')
  expect_error(mtc.network(data.ab=data, data.re=data.re))
})

test_that("data.re column names are checked", {
  expect_error(mtc.network(data.re=data.frame(study=c("s01", "s01"), treatment=c("A", "B"))))
  expect_error(mtc.network(data.re=data.frame(study=c("s01", "s01"), treatment=c("A", "B"), diff=c(NA, NA))))
  expect_error(mtc.network(data.re=data.frame(study=c("s01", "s01"), treatment=c("A", "B"), std.err=c(NA, NA))))
})

test_that("data.re checks that every study has a baseline (diff=NA) arm", {
  expect_error(mtc.network(data.re=data.frame(study=c("s01", "s01"), treatment=c('A', 'B'), diff=c(1, 2), std.err=c(0.5, 0.5))))
})

test_that("data.re checks that non-baseline arms have std.err specified", {
  mtc.network(data.re=data.frame(study=c("s01", "s01"), treatment=c('A', 'B'), diff=c(NA, 2), std.err=c(NA, 1)))
  expect_error(mtc.network(data.re=data.frame(study=c("s01", "s01"), treatment=c('A', 'B'), diff=c(NA, 2), std.err=c(NA, NA))))
})

test_that("data.re checks that multi-arm trials must have std.err specified for all arms", {
  expect_error(mtc.network(data.re=data.frame(study=c("s01", "s01", "s01"), treatment=c('A', 'B', 'C'), diff=c(NA, 2, 1), std.err=c(NA, 1, 1))))
})

test_that("data.re is ordered by number of arms", {
  data <- read.table(textConnection("
study  treatment  diff  std.err
s01    A          NA    0.43
s01    C          -0.31 0.67
s01    D          -0.31 0.67
s02    A          NA    NA
s02    B          -1.7  0.38"), header=T)
  expected <- read.table(textConnection("
study  treatment  diff  std.err
s02    A          NA    NA
s02    B          -1.7  0.38
s01    A          NA    0.43
s01    C          -0.31 0.67
s01    D          -0.31 0.67"), header=T)
  expect_that(mtc.network(data.re=data)$data.re, equals(expected))
})

test_that("data.re has the baseline arm of every study first", {
  data <- read.table(textConnection("
study  treatment  diff  std.err
s01    C          -0.31 0.67
s01    A          -0.31 0.67
s01    D          NA    0.43
s02    A          NA    NA
s02    B          -1.7  0.38"), header=T)
  expected <- read.table(textConnection("
study  treatment  diff  std.err
s02    A          NA    NA
s02    B          -1.7  0.38
s01    D          NA    0.43
s01    A          -0.31 0.67
s01    C          -0.31 0.67"), header=T)
  expect_that(mtc.network(data.re=data)$data.re, equals(expected))
})
