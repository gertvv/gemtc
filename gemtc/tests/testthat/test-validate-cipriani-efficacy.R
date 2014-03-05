context("[validate] Efficacy data from Cipriani et al. Lancet 2009;373:746-758")

test_that("The summaries match", {
  network <- read.mtc.network(system.file('extdata/cipriani-efficacy.gemtc', package='gemtc'))
  result <- replicate.example("cipriani-efficacy", network)
  compare.summaries(result$s1, result$s2)
})
