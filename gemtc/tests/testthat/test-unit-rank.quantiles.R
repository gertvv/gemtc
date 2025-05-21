context("rank.quantiles")

test_that("rank.quantiles is implemented correctly", {
  ranks <- matrix(c(1/4, 1/4, 1/4, 1/4, 1/2, 1/4, 1/4, 0, 1/4, 1/2, 1/4, 0, 0, 0, 1/4, 3/4), nrow=4, byrow=TRUE, dimnames=list(c("A", "B", "C", "D"), NULL))
  expect_equal(rank.quantiles(ranks), matrix(c(1,2,4,1,1,3,1,2,3,3,4,4), nrow=4, byrow=TRUE, dimnames=list(c("A", "B", "C", "D"), c("2.5%", "50%", "97.5%"))))
})
