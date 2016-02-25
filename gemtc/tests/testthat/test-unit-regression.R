context("regression")

test_that("regressionParams is correct", {
  expect_equal(regressionParams(list('coefficient'='shared', 'control'=1), 4), c('B'))
  expect_equal(regressionParams(list('coefficient'='shared', 'control'=3), 5), c('B'))

  expect_equal(regressionParams(list('coefficient'='unrelated', 'control'=1), 4), c('beta[2]', 'beta[3]', 'beta[4]'))
  expect_equal(regressionParams(list('coefficient'='unrelated', 'control'=3), 5), c('beta[1]', 'beta[2]', 'beta[4]', 'beta[5]'))
  expect_equal(regressionParams(list('coefficient'='unrelated', 'control'=4), 4), c('beta[1]', 'beta[2]', 'beta[3]'))

  expect_equal(regressionParams(list('coefficient'='exchangeable', 'control'=3), 4), c('beta[1]', 'beta[2]', 'beta[4]', 'B'))

  expect_equal(regressionParams(list('coefficient'='unrelated'), 8, nc=3), c('B[2]', 'B[3]'))
})

test_that("regressionAdjustMatrix is correct", {
  expect_equal(regressionAdjustMatrix(c(1,1,2), c(1,2,3), list('coefficient'='shared', 'control'=1), 4),
               cbind(0,1,0))
  expect_equal(regressionAdjustMatrix(c(1,1,2), c(1,2,3), list('coefficient'='shared', 'control'=2), 4),
               cbind(0,-1,1))

  expect_equal(regressionAdjustMatrix(c(1,1,2), c(1,2,3), list('coefficient'='unrelated', 'control'=1), 4),
               cbind(c(0,0,0), c(1,0,0), c(-1,1,0)))
  expect_equal(regressionAdjustMatrix(c(1,1,2), c(1,2,3), list('coefficient'='unrelated', 'control'=2), 4),
               cbind(c(0,0,0), c(-1,0,0), c(0,1,0)))
  expect_equal(regressionAdjustMatrix(c(1), c(3), list('coefficient'='unrelated', 'control'=3), 4),
               cbind(c(-1,0,0)))

  expect_equal(regressionAdjustMatrix(c(1,1,2), c(1,2,3), list('coefficient'='exchangeable', 'control'=1), 4),
               cbind(c(0,0,0,0), c(1,0,0,0), c(-1,1,0,0)))

  expect_equal(regressionAdjustMatrix(c(1,1,2,2), c(1,2,3,4), list('coefficient'='shared', 'classes'=list('C'=1, 'X'=c(2,3), 'Y'=4)), 4),
               cbind(c(0, 0), c(1,0), c(0, 0), c(-1, 1)))
})
