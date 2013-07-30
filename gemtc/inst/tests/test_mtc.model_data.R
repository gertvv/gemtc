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
