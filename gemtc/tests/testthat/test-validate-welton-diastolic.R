context("[validate] Welton et al., Am J Epidemiol 2009;169:1158-1165 Diastolic BP")

test_that("The summaries match", {
  network <- read.mtc.network('../data/welton-diastolic.gemtc')
  result <- replicate.example("welton-diastolic", network, likelihood="normal", link="identity")
  compare.summaries(result$s1, result$s2)
})
