context("likelihood/link");

# This file contains a single test for each implemented likelihood.
# Since ll.defined() checks whether all required methods are implemented,
# this is a simple guard against forgetting to add or correctly name new
# methods.

test_that("normal.identity is defined", {
  expect_true(ll.defined(list(likelihood='normal', link='identity')))
})

test_that("binom.logit is defined", {
  expect_true(ll.defined(list(likelihood='binom', link='logit')))
})

test_that("binom.log is defined", {
  expect_true(ll.defined(list(likelihood='binom', link='log')))
})

test_that("binom.cloglog is defined", {
  expect_true(ll.defined(list(likelihood='binom', link='cloglog')))
})

test_that("poisson.log is defined", {
  expect_true(ll.defined(list(likelihood='poisson', link='log')))
})
