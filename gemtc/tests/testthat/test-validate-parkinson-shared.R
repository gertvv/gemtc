context("[validate] NICE TSD2 program 8a")

test_that("The summaries match", {
  result <- replicate.example("parkinson-shared", parkinson_shared, likelihood="normal", link="identity")
  compare.summaries(result$s1, result$s2)
})
