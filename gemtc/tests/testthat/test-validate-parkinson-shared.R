context("[validate] NICE TSD2 program 8a")

test_that("The summaries match", {
  network <- mtc.network(data.ab=read.table('../data/parkinson-shared.data-ab.txt', header=TRUE),
                         data.re=read.table('../data/parkinson-shared.data-re.txt', header=TRUE))
  result <- replicate.example("parkinson-shared", network, likelihood="normal", link="identity")
  compare.summaries(result$s1, result$s2)
})
