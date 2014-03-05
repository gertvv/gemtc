context("mtc.model (column handling)")

test_that("std.dev + sampleSize is rewritten to std.err", {
  data <- read.table(textConnection('study treatment mean std.dev sampleSize
s01 A 0.0 1.5 12
s01 B 1.0 2.0 31'), header=T)
  network <- mtc.network(data)
  model <- mtc.model(network, likelihood='normal', link='identity')
  expect_that(model$network[['data.ab']]$std.err, equals(c(1.5/sqrt(12), 2.0/sqrt(31))))
})

test_that("normal.identity requires the right columns", {
  expect_that(required.columns.ab.normal.identity(), equals(c('m'='mean', 'e'='std.err')))
})

test_that("data.ab missing sampleSize throws error", {
  data <- read.table(textConnection('study treatment mean std.dev
s01 A 0.0 1.5
s01 B 1.0 2.0'), header=T)
  network <- mtc.network(data)
  expect_error(mtc.model(network, likelihood='normal', link='identity'))
})

test_that("data.ab missing std.dev throws error", {
  data <- read.table(textConnection('study treatment mean sampleSize 
s01 A 0.0 12
s01 B 1.0 31'), header=T)
  network <- mtc.network(data)
  expect_error(mtc.model(network, likelihood='normal', link='identity'))
})

test_that("data.ab missing mean throws error", {
  data <- read.table(textConnection('study treatment std.dev sampleSize 
s01 A 0.0 12
s01 B 1.0 31'), header=T)
  network <- mtc.network(data)
  expect_error(mtc.model(network, likelihood='normal', link='identity'))
})

test_that("data.ab with std.err does not throw error", {
  data <- read.table(textConnection('study treatment mean std.err 
s01 A 0.0 0.5 
s01 B 1.0 0.7'), header=T)
  network <- mtc.network(data)
  model <- mtc.model(network, likelihood='normal', link='identity')
})

test_that("data.re missing diff throws error", {
  data <- read.table(textConnection('study treatment diff std.err
s01 A NA 0.5
s01 B 1.0 0.7'), header=T)
  network <- mtc.network(data.re=data)
  network$data.re$diff <- NULL
  expect_error(mtc.model(network, likelihood='normal', link='identity'))
})

test_that("data.re missing std.err throws error", {
  data <- read.table(textConnection('study treatment diff std.err
s01 A NA 0.5
s01 B 1.0 0.7'), header=T)
  network <- mtc.network(data.re=data)
  network$data.re$std.err <- NULL
  expect_error(mtc.model(network, likelihood='normal', link='identity'))
})
