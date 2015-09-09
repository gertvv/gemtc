context("[validate] Smoking cessation data from Lu & Ades, J Am Stat Assoc 2006;101(474):447-459, Table 1")

test_that("The summaries match", {
  result <- replicate.example("luades-smoking", smoking)
  compare.summaries(result$s1, result$s2)
})
