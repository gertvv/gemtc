context("Run models for all data sets and call the appropriate summaries / plots")

test_that("cipriani-efficacy", {
  # TODO: test node-splitting comparisons

  test.regress(depression, likelihood="binom", link="logit", t1="escitalopram", t2="sertraline")
})

test_that("luades-smoking", {
  # node-splitting comparisons
  expect_that(mtc.nodesplit.comparisons(smoking), equals(
    data.frame(t1=c("A", "A", "A", "B", "B", "C"), t2=c("B", "C", "D", "C", "D", "D"), stringsAsFactors=FALSE)))

  test.regress(smoking, likelihood="binom", link="logit", t1="B", t2="D")
})

test_that("luades-thrombolytic", {
  # node-splitting comparisons
  expect_that(mtc.nodesplit.comparisons(thrombolytic), equals(
    data.frame(t1=c("ASPAC", "ASPAC", "ASPAC", "AtPA", "AtPA", "AtPA", "AtPA", "Ret", "SK", "SK", "tPA"),
               t2=c("AtPA", "SK", "tPA", "Ret", "SK", "SKtPA", "UK", "SK", "tPA", "UK", "UK"), stringsAsFactors=FALSE)))

  test.regress(thrombolytic, likelihood="binom", link="logit", t1="SK", t2="UK")
})

test_that("tsd2-1 (event data, pair-wise)", {
  expect_that(nrow(mtc.nodesplit.comparisons(blocker)), equals(0))
  test.regress(blocker, likelihood="binom", link="logit")
})


test_that("tsd2-2 (fat survival, rate data)", {
  expect_that(nrow(mtc.nodesplit.comparisons(dietfat)), equals(0))
  test.regress(dietfat, likelihood="poisson", link="log")
})

test_that("tsd2-3 (diabetes, rate data)", {
  data.ab <- dget("../data/studyrow/tsd2-3.out.txt")
  network <- mtc.network(data.ab=data.ab)
  
  comparisons <- mtc.comparisons(network)
  comparisons$t1 <- as.character(comparisons$t1)
  comparisons$t2 <- as.character(comparisons$t2)
  expect_that(mtc.nodesplit.comparisons(network), equals(comparisons))

  test.regress(network, likelihood="binom", link="cloglog", t1="ARB", t2="BetaB")
})

test_that("parkinson example", {
  expect_that(mtc.nodesplit.comparisons(parkinson), equals(
    data.frame(t1=c("A", "A", "B", "C"),
               t2=c("C", "D", "D", "D"), stringsAsFactors=FALSE)))

  test.regress(parkinson, likelihood="normal", link="identity", t1="B", t2="D")
})

test_that("tsd2-5 (parkinson AB data)", {
  data.ab <- dget("../data/studyrow/tsd2-5.out.txt")
  network <- mtc.network(data.ab=data.ab)

  expect_that(mtc.nodesplit.comparisons(network), equals(
    data.frame(t1=c("1", "1", "2", "3"),
               t2=c("3", "4", "4", "4"), stringsAsFactors=FALSE)))

  test.regress(network, likelihood="normal", link="identity", t1="3", t2="4")
})

test_that("tsd2-7 (parkinson RE data)", {
  expect_that(mtc.nodesplit.comparisons(parkinson_diff), equals(
    data.frame(t1=c("A", "A", "B", "C"),
               t2=c("C", "D", "D", "D"), stringsAsFactors=FALSE)))

  test.regress(parkinson_diff, likelihood="normal", link="identity", t1="D", t2="A")
})

test_that("tsd2-8 (parkinson mixed data)", {
  expect_that(mtc.nodesplit.comparisons(parkinson_shared), equals(
    data.frame(t1=c("A", "A", "B", "C"),
               t2=c("C", "D", "D", "D"), stringsAsFactors=FALSE)))
  
  test.regress(parkinson_shared, likelihood="normal", link="identity", t1="B", t2="D")
})

test_that("ns-complex (numbered studies)", {
  data.re <- read.table("../data/ns-complex.csv", header=TRUE, sep=",")
  data.re$Study <- NULL
  network <- mtc.network(data.re=data.re)

  # Node-split comparisons are tested elsewhere

  test.regress(network, likelihood="normal", link="identity", t1="B", t2="D")
})

test_that("ns-complex (named studies)", {
  data.re <- read.table("../data/ns-complex.csv", header=TRUE, sep=",")
  data.re$study <- data.re$Study
  data.re$Study <- NULL
  network <- mtc.network(data.re=data.re)

  # Node-split comparisons are tested elsewhere

  test.regress(network, likelihood="normal", link="identity", t1="H", t2="D")
})
