context("[validate] NICE TSD2 program 5a")

test_that("The summaries match", {
  result <- replicate.example("parkinson", parkinson, likelihood="normal", link="identity")
  compare.summaries(result$s1, result$s2)
})
