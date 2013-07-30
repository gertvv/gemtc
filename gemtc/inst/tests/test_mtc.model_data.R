context("mtc.model.data");

test_that("mtc.model.data runs on simple data", {
  data <- read.table(textConnection('
study treatment responders sampleSize
1     A         3          10
1     B         4          12'), header=T)
  network <- mtc.network(data)
  model <- list(network=network, likelihood='binom', link='logit', om.scale=2.5)
  model <- mtc.model.data(model)
  expect_that(model$r, equals(t(c(3, 4))))
  expect_that(model$n, equals(t(c(10, 12))))
  expect_that(model$t, equals(t(as.numeric(network$data$treatment))))
})

test_that("mtc.model.data complains about NAs in data.ab", {
  data <- read.table(textConnection('
study treatment responders sampleSize
1     A         3          10
1     B         NA         12'), header=T)
  network <- mtc.network(data)
  model <- list(network=network, likelihood='binom', link='logit', om.scale=2.5)
  expect_error(mtc.model.data(model), 'data.ab contains NAs in column "responders"')

  data <- read.table(textConnection('
study treatment responders sampleSize
1     A         3          10
1     B         4          NA'), header=T)
  network <- mtc.network(data)
  model <- list(network=network, likelihood='binom', link='logit', om.scale=2.5)
  expect_error(mtc.model.data(model), 'data.ab contains NAs in column "sampleSize"')
})

test_that("mtc.model.data includes correct fields for data.ab, binom.logit", {
  data.ab <- read.table(textConnection('
study treatment responders sampleSize exposure
1     A         3          10         20
1     B         4          12         24'), header=T)
  network <- mtc.network(data.ab)
  model <- list(network=network, likelihood='binom', link='logit', om.scale=2.5)
  data <- mtc.model.data(model)
  expect_that(sort(names(data)), equals(
    sort(c(
      'ns.a', 'ns.r2', 'ns.rm', 'ns', 'nt', 'om.scale',
      'na', 't', 'r', 'n'))
  ))
})

test_that("mtc.model.data includes correct fields for data.ab, binom.cloglog", {
  data.ab <- read.table(textConnection('
study treatment responders sampleSize exposure
1     A         3          10         20
1     B         4          12         24'), header=T)
  network <- mtc.network(data.ab)
  model <- list(network=network, likelihood='binom', link='cloglog', om.scale=2.5)
  data <- mtc.model.data(model)
  expect_that(sort(names(data)), equals(
    sort(c(
      'ns.a', 'ns.r2', 'ns.rm', 'ns', 'nt', 'om.scale',
      'na', 't', 'r', 'n'))
  ))
})

test_that("mtc.model.data includes correct fields for data.ab, poisson.log", {
  data.ab <- read.table(textConnection('
study treatment responders sampleSize exposure
1     A         3          10         20
1     B         4          12         24'), header=T)
  network <- mtc.network(data.ab)
  model <- list(network=network, likelihood='poisson', link='log', om.scale=2.5)
  data <- mtc.model.data(model)
  expect_that(sort(names(data)), equals(
    sort(c(
      'ns.a', 'ns.r2', 'ns.rm', 'ns', 'nt', 'om.scale',
      'na', 't', 'r', 'E'))
  ))
})

test_that("mtc.model.data includes correct fields for data.ab, normal.identity", {
  data.ab <- read.table(textConnection('
study treatment mean std.err
1     A         3.4  0.21
1     B         4.0  0.19'), header=T)
  network <- mtc.network(data.ab)
  model <- list(network=network, likelihood='normal', link='identity', om.scale=2.5)
  data <- mtc.model.data(model)
  expect_that(sort(names(data)), equals(
    sort(c(
      'ns.a', 'ns.r2', 'ns.rm', 'ns', 'nt', 'om.scale',
      'na', 't', 'm', 'e'))
  ))
})

test_that("mtc.model.data includes correct fields for data.re", {
  data.re <- read.table(textConnection('
study treatment diff std.err
1     A         NA   0.21
1     B         4.0  0.19'), header=T)
  network <- mtc.network(data.re=data.re)
  model <- list(network=network, likelihood='normal', link='identity', om.scale=2.5)
  data <- mtc.model.data(model)
  expect_that(sort(names(data)), equals(
    sort(c(
      'ns.a', 'ns.r2', 'ns.rm', 'ns', 'nt', 'om.scale',
      'na', 't', 'm', 'e'))
  ))
})

test_that("mtc.model.data includes correct fields for data.ab, normal.identity + data.re", {
  data.ab <- read.table(textConnection('
study treatment mean std.err
1     A         3.4  0.21
1     B         4.0  0.19'), header=T)
  data.re <- read.table(textConnection('
study treatment diff std.err
2     A         NA   0.21
2     B         4.0  0.19'), header=T)
  network <- mtc.network(data.ab, data.re=data.re)
  model <- list(network=network, likelihood='normal', link='identity', om.scale=2.5)
  data <- mtc.model.data(model)
  expect_that(sort(names(data)), equals(
    sort(c(
      'ns.a', 'ns.r2', 'ns.rm', 'ns', 'nt', 'om.scale',
      'na', 't', 'm', 'e'))
  ))
})

test_that("mtc.model.data includes correct fields for data.ab, binom.logit + data.re", {
  data.ab <- read.table(textConnection('
study treatment responders sampleSize
1     A         3          10
1     B         4          12'), header=T)
  data.re <- read.table(textConnection('
study treatment diff std.err
2     A         NA   0.21
2     B         4.0  0.19'), header=T)
  network <- mtc.network(data.ab, data.re=data.re)
  model <- list(network=network, likelihood='binom', link='logit', om.scale=2.5)
  data <- mtc.model.data(model)
  expect_that(sort(names(data)), equals(
    sort(c(
      'ns.a', 'ns.r2', 'ns.rm', 'ns', 'nt', 'om.scale',
      'na', 't', 'r', 'n', 'm', 'e'))
  ))
})
