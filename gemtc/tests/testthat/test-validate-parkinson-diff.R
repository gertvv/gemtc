context("[validate] NICE TSD2 program 7a")

test_that("The summaries match", {
  network <- mtc.network(data.re=read.table('../data/parkinson-diff.data.txt', header=TRUE))
  result <- replicate.example("parkinson-diff", network, likelihood="normal", link="identity")
  compare.summaries(result$s1, result$s2)
})
