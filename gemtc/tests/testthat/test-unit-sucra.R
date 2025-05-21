context("sucra")

test_that("SUCRA is implemented correctly", {
  ranks <- matrix(c(1/4, 1/4, 1/4, 1/4, 1/2, 1/4, 1/4, 0, 1/4, 1/2, 1/4, 0, 0, 0, 1/4, 3/4), nrow=4, byrow=TRUE, dimnames=list(c("A", "B", "C", "D"), NULL))
  expect_equal(sucra(ranks), c(A=1/2, B=3/4, C=2/3, D=1/12))
})
