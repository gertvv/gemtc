context("mtc.model.use")

test_that("the model generates correctly", {
  data.ab <- read.table(textConnection('
study treatment mean std.err
1     A         10.5 0.18
1     B         15.3 0.17
2     B         15.7 0.12
2     C         18.3 0.15
3     B         13.1 0.19
3     C         14.2 0.20'), header=T)
  network <- mtc.network(data.ab)
  model <- mtc.model(network, type='use')
  
  expect_equal(3, length(model$inits[[1]]$mu))
  expect_equal(3, length(model$inits[[2]]$mu))
  expect_equal(3, length(model$inits[[3]]$mu))
  expect_equal(3, length(model$inits[[4]]$mu))
})
