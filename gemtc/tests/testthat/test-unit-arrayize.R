context("arrayize")

test_that("it preserves scalar values", {
  expect_equal(arrayize(c("a"=3)), list("a"=3))
  expect_equal(arrayize(c("a"=3, "b"=2)), list("a"=3, "b"=2))
})

test_that("it parses vectors", {
  expect_equal(arrayize(c("a[1]"=3)), list("a"=3))
  expect_equal(arrayize(c("a[1]"=3, "a[2]"=4)), list("a"=c(3,4)))
  expect_equal(arrayize(c("a[2]"=4)), list("a"=c(NA,4)))
})

test_that("it parses matrices", {
  expect_equal(arrayize(c("m[1,2]"=8,"m[2,1]"=4)), list("m"=rbind(c(NA,8),c(4,NA))))
})

test_that("it handles multiple variables", {
  result <- arrayize(c("m[1,2]"=8,"a"=1,"beta[2]"=3,"m[2,1]"=4))
  expect_equal(result, list("a"=1, "beta"=c(NA,3), "m"=rbind(c(NA,8),c(4,NA))))
})
