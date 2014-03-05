context("[validate] NICE TSD2 program 5a")

test_that("The summaries match", {
  network <- read.mtc.network(system.file('extdata/parkinson.gemtc', package='gemtc'))
  result <- replicate.example("parkinson", network, likelihood="normal", link="identity")
  compare.summaries(result$s1, result$s2)
})
