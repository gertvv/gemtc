context("inits.to.monitors")

test_that("inits.to.monitors maps scalars", {
  expect_that(inits.to.monitors(list(sd.d=3)), equals(c("sd.d")))
})

test_that("inits.to.monitors maps vectors", {
  expect_that(inits.to.monitors(list(mu=c(1, -1, 3))), equals(c("mu[1]", "mu[2]", "mu[3]")))
})

test_that("inits.to.monitors removes NAs from vectors", {
  expect_that(inits.to.monitors(list(mu=c(1, NA, 3))), equals(c("mu[1]", "mu[3]")))
})

test_that("inits.to.monitors maps matrices", {
  expect_that(inits.to.monitors(list(delta=matrix(c(5, 3, -1, 2, 4, 5), ncol=3, byrow=TRUE))),
    equals(c("delta[1,1]", "delta[1,2]", "delta[1,3]", "delta[2,1]", "delta[2,2]", "delta[2,3]")))
})

test_that("inits.to.monitors removes NAs from matrices", {
  expect_that(inits.to.monitors(list(delta=matrix(c(NA, 3, NA, NA, 4, 5), ncol=3, byrow=TRUE))),
    equals(c("delta[1,2]", "delta[2,2]", "delta[2,3]")))
})

test_that("inits.to.monitors adds sd.d if var.d is present", {
  expect_that(inits.to.monitors(list(var.d=2)),
    equals(c("var.d", "sd.d")))
})

test_that("inits.to.monitors adds sd.d if tau.d is present", {
  expect_that(inits.to.monitors(list(tau.d=2)),
    equals(c("tau.d", "sd.d")))
})
