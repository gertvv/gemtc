context("[validate] NICE TSD2 program 2a")

test_that("The summaries match", {
  result <- replicate.example("dietfat.re", dietfat, likelihood="poisson", link="log", linearModel="random")
  compare.summaries(result$s1, result$s2)
})
