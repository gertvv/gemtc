context("[validate] Smoking cessation data from Lu & Ades, J Am Stat Assoc 2006;101(474):447-459, Table 1")

test_that("The summaries match", {
  network <- read.mtc.network(system.file('extdata/luades-smoking.gemtc', package='gemtc'))
  result <- replicate.example("luades-smoking", network)
  compare.summaries(result$s1, result$s2)
})
