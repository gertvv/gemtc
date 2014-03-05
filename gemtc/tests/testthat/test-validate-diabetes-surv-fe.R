context("[validate] NICE TSD2 program 3b")

test_that("The summaries match", {
  network <- mtc.network(data.ab=read.table('../data/diabetes-surv.data.txt', header=TRUE))
  result <- replicate.example("diabetes-surv.fe", network, likelihood="binom", link="cloglog", linearModel="fixed")
  compare.summaries(result$s1, result$s2)
})
