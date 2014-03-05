context("[validate] Thrombolytic drugs data from Lu & Ades, J Am Stat Assoc 2006;101(474):447-459, Table 3")

test_that("The summaries match", {
  network <- read.mtc.network(system.file('extdata/luades-thrombolytic.gemtc', package='gemtc'))
  result <- replicate.example("luades-thrombolytic", network)
  compare.summaries(result$s1, result$s2)
})
