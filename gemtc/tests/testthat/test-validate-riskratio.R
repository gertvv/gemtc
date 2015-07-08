context("[validate] Risk ratio example")

# Example from http://www.statsdirect.com/help/default.htm#meta_analysis/relative_risk.htms
# Original source http://dx.doi.org/10.1016/0895-4356(91)90261-7

# Results of the Bayesian FE model closely match the frequentist RR of 0.913608 (0.8657, 0.964168)

test_that("The summaries match", {
  network <- mtc.network(data.ab=read.table("../data/riskratio.data.txt", header=TRUE))
  result <- replicate.example("riskratio", network, likelihood="binom", link="log", linearModel="fixed")
  compare.summaries(result$s1, result$s2)
})
