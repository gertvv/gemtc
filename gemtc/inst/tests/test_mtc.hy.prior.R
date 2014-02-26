context("mtc.hy.prior")

test_that("the standard uniform prior on standard deviation is generated correctly", {
  expect_that(as.character(mtc.hy.prior("std.dev", "dunif", 0, "om.scale")), equals("sd.d ~ dunif(0, om.scale)\ntau.d <- pow(sd.d, -2)"))
  expect_that(as.character(mtc.hy.prior("std.dev", "dunif", 0, 5)), equals("sd.d ~ dunif(0, 5)\ntau.d <- pow(sd.d, -2)"))
  expect_that(as.character(mtc.hy.prior("std.dev", "dunif", 0.1, 2)), equals("sd.d ~ dunif(0.1, 2)\ntau.d <- pow(sd.d, -2)"))
})

test_that("other priors can be specified by name", {
  expect_that(as.character(mtc.hy.prior("std.dev", "dgamma", 0.01, 0.01)), equals("sd.d ~ dgamma(0.01, 0.01)\ntau.d <- pow(sd.d, -2)"))
  expect_that(as.character(mtc.hy.prior("std.dev", "dlnorm", -1.3, 0.49)), equals("sd.d ~ dlnorm(-1.3, 0.49)\ntau.d <- pow(sd.d, -2)"))
})

test_that("priors can have more or less than two parameters", {
  expect_that(as.character(mtc.hy.prior("std.dev", "dgamma", 0.01, 0.02, 0.03)), equals("sd.d ~ dgamma(0.01, 0.02, 0.03)\ntau.d <- pow(sd.d, -2)"))
  expect_that(as.character(mtc.hy.prior("std.dev", "dgamma", 0.01)), equals("sd.d ~ dgamma(0.01)\ntau.d <- pow(sd.d, -2)"))
})

test_that("the prior can be specified on the variance", {
  expect_that(as.character(mtc.hy.prior("var", "dlnorm", -1.3, 0.49)), equals("var.d ~ dlnorm(-1.3, 0.49)\nsd.d <- sqrt(var.d)\ntau.d <- 1 / var.d"))
})

test_that("the prior can be specified on the precision", {
  expect_that(as.character(mtc.hy.prior("prec", "dlnorm", -1.3, 0.49)), equals("tau.d ~ dlnorm(-1.3, 0.49)\nsd.d <- sqrt(1 / tau.d)"))
})

test_that("LOR empirical priors have correct values", {
  expect_that(as.character(mtc.hy.empirical.lor("mortality", "pharma-control")), equals("var.d ~ dlnorm(-4.06, 0.476)\nsd.d <- sqrt(var.d)\ntau.d <- 1 / var.d"))
  expect_that(as.character(mtc.hy.empirical.lor("mortality", "pharma-pharma")), equals("var.d ~ dlnorm(-4.27, 0.457)\nsd.d <- sqrt(var.d)\ntau.d <- 1 / var.d"))
  expect_that(as.character(mtc.hy.empirical.lor("subjective", "pharma-pharma")), equals("var.d ~ dlnorm(-2.34, 0.381)\nsd.d <- sqrt(var.d)\ntau.d <- 1 / var.d"))
})
