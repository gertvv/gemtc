context("[validate] NICE TSD2 program 3a")

test_that("The summaries match", {
  network <- mtc.network(data.ab=read.table('../data/diabetes-surv.data.txt', header=TRUE))
  result <- replicate.example("diabetes-surv", network, likelihood="binom", link="cloglog")
  compare.summaries(result$s1, result$s2)
})
