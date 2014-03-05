context("[validate] Modified from NICE TSD4 smoking example")

test_that("The summaries match", {
  network <- mtc.network(data.re=read.table('../data/smoking-ume.data.txt', header=TRUE))
  result <- replicate.example("smoking-ume", network, likelihood="binom", link="cloglog", type="ume")
  compare.summaries(result$s1, result$s2)
})
