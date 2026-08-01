test_that("rtruncnorm can sample across the mean", {
  x <- replicate(1000, rtruncnorm(mean = 0, sd = 1, a = -1, b = 1))
  expect_true(all(x >= -1))
  expect_true(all(x <= 1))
  expect_equal(mean(x), 0, tolerance = 0.1)
})

test_that("rtruncnorm can sample from a non-standard normal", {
  x <- replicate(1000, rtruncnorm(mean = 2, sd = 2, a = -2, b = 6))
  expect_true(all(x >= -2))
  expect_true(all(x <= 6))
  expect_equal(mean(x), 2, tolerance = 0.2)
})

test_that("rtruncnorm can sample the left tail", {
  x <- replicate(1000, rtruncnorm(mean = 0, sd = 1, a = -5, b = -3))
  expect_true(all(x >= -5))
  expect_true(all(x <= -3))
})

test_that("rtruncnorm can sample the right tail", {
  x <- replicate(1000, rtruncnorm(mean = 0, sd = 1, a = 4, b = 8))
  expect_true(all(x >= 4))
  expect_true(all(x <= 8))
})
