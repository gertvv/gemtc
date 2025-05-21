context("[validate] Welton et al., Am J Epidemiol 2009;169:1158-1165 Systolic BP")

test_that("The summaries match", {
  network <- dget('../data/welton-systolic.txt')
  result <- replicate.example("welton-systolic", network, likelihood="normal", link="identity")
  compare.summaries(result$s1, result$s2)
})
