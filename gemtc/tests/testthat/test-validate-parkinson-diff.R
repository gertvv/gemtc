context("[validate] NICE TSD2 program 7a")

test_that("The summaries match", {
  result <- replicate.example("parkinson-diff", parkinson_diff, likelihood="normal", link="identity")
  compare.summaries(result$s1, result$s2)
})
