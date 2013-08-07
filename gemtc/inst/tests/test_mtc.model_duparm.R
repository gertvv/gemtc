context("mtc.model with duplicated arms")

test_that("mtc.model refuses duplicated arms", {
  network <- mtc.network(read.table(textConnection("
    study   treatment  mean std.dev sampleSize
    s01     A          2.0  0.5     20
    s01     B          1.8  0.5     20
    s01     B          1.5  0.5     20"), header=T))
  expect_warning(expect_error(mtc.model(network)))
})
