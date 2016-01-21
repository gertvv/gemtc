context("[validate] NICE TSD2 program 2b")

test_that("The summaries match", {
  result <- replicate.example("dietfat.fe", dietfat, likelihood="poisson", link="log", linearModel="fixed")
  compare.summaries(result$s1, result$s2)
})
