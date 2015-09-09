context("[validate] Thrombolytic drugs data from Lu & Ades, J Am Stat Assoc 2006;101(474):447-459, Table 3")

test_that("The summaries match", {
  result <- replicate.example("luades-thrombolytic", thrombolytic)
  compare.summaries(result$s1, result$s2)
})
