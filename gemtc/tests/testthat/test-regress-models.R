context("Run models for all data sets and call the appropriate summaries / plots")

test_that("luades-smoking", {
  network <- read.mtc.network(system.file('extdata/luades-smoking.gemtc', package='gemtc'))

  # node-splitting comparisons
  expect_that(mtc.nodesplit.comparisons(network), equals(
    data.frame(t1=c("A", "A", "A", "B", "B", "C"), t2=c("B", "C", "D", "C", "D", "D"), stringsAsFactors=FALSE)))

  # all the models run
  test.regress(network, likelihood="binom", link="logit", t1="B", t2="D")
})

test_that("luades-thrombolytic", {
  network <- read.mtc.network(system.file('extdata/luades-thrombolytic.gemtc', package='gemtc'))

  # node-splitting comparisons
  expect_that(mtc.nodesplit.comparisons(network), equals(
    data.frame(t1=c("ASPAC", "ASPAC", "ASPAC", "AtPA", "AtPA", "AtPA", "AtPA", "Ret", "SK", "SK", "tPA"),
               t2=c("AtPA", "SK", "tPA", "Ret", "SK", "SKtPA", "UK", "SK", "tPA", "UK", "UK"), stringsAsFactors=FALSE)))

  # all the models run
  test.regress(network, likelihood="binom", link="logit", t1="SK", t2="UK")
})

test_that("tsd2-8", {
  data.ab <- dget("../data/studyrow/tsd2-8.out1.txt")
  data.re <- dget("../data/studyrow/tsd2-8.out2.txt")
  data.re$diff <- data.re$mean
  data.re$mean <- NULL
  network <- mtc.network(data.ab=data.ab, data.re=data.re)
  
  expect_that(mtc.nodesplit.comparisons(network), equals(
    data.frame(t1=c("1", "1", "2", "3"), t2=c("3", "4", "4", "4"), stringsAsFactors=FALSE)))
  
  test.regress(network, likelihood="normal", link="identity", t1="2", t2="4")
})
