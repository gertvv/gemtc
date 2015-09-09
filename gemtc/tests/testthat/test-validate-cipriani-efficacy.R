context("[validate] Efficacy data from Cipriani et al. Lancet 2009;373:746-758")

test_that("The summaries match", {
  result <- replicate.example("cipriani-efficacy", depression)
  compare.summaries(result$s1, result$s2)
})
