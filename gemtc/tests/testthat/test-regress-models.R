context("Run models for all data sets and call the appropriate summaries / plots")

test_that("cipriani-efficacy", {
  network <- depression

  # TODO: test node-splitting comparisons

  test.regress(network, likelihood="binom", link="logit", t1="escitalopram", t2="sertraline")
})

test_that("luades-smoking", {
  network <- smoking

  # node-splitting comparisons
  expect_that(mtc.nodesplit.comparisons(network), equals(
    data.frame(t1=c("A", "A", "A", "B", "B", "C"), t2=c("B", "C", "D", "C", "D", "D"), stringsAsFactors=FALSE)))

  test.regress(network, likelihood="binom", link="logit", t1="B", t2="D")
})

test_that("luades-thrombolytic", {
  network <- thrombolytic

  # node-splitting comparisons
  expect_that(mtc.nodesplit.comparisons(network), equals(
    data.frame(t1=c("ASPAC", "ASPAC", "ASPAC", "AtPA", "AtPA", "AtPA", "AtPA", "Ret", "SK", "SK", "tPA"),
               t2=c("AtPA", "SK", "tPA", "Ret", "SK", "SKtPA", "UK", "SK", "tPA", "UK", "UK"), stringsAsFactors=FALSE)))

  test.regress(network, likelihood="binom", link="logit", t1="SK", t2="UK")
})

test_that("tsd2-1 (event data, pair-wise)", {
  data.ab <- dget("../data/studyrow/tsd2-1.out.txt")
  network <- mtc.network(data.ab=data.ab)

  expect_that(nrow(mtc.nodesplit.comparisons(network)), equals(0))

  test.regress(network, likelihood="binom", link="logit")
})


test_that("tsd2-2 (fat survival, rate data)", {
  data.ab <- dget("../data/studyrow/tsd2-2.out.txt")
  data.ab <- data.ab[-5,] # Remove duplicated arm, so the model can be estimated
  network <- mtc.network(data.ab=data.ab)

  expect_that(nrow(mtc.nodesplit.comparisons(network)), equals(0))

  test.regress(network, likelihood="poisson", link="log")
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
  network <- parkinson

  expect_that(mtc.nodesplit.comparisons(network), equals(
    data.frame(t1=c("A", "A", "B", "C"),
               t2=c("C", "D", "D", "D"), stringsAsFactors=FALSE)))

  test.regress(network, likelihood="normal", link="identity", t1="B", t2="D")
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
  data.re <- dget("../data/studyrow/tsd2-7.out.txt")
  data.re$std.err[is.na(data.re$std.err)] <- sqrt(data.re$var[is.na(data.re$std.err)])
  network <- mtc.network(data.re=data.re)

  expect_that(mtc.nodesplit.comparisons(network), equals(
    data.frame(t1=c("1", "1", "2", "3"),
               t2=c("3", "4", "4", "4"), stringsAsFactors=FALSE)))

  test.regress(network, likelihood="normal", link="identity", t1="4", t2="1")
})

test_that("tsd2-8 (parkinson mixed data)", {
  data.ab <- dget("../data/studyrow/tsd2-8.out1.txt")
  data.re <- dget("../data/studyrow/tsd2-8.out2.txt")
  network <- mtc.network(data.ab=data.ab, data.re=data.re)
  
  expect_that(mtc.nodesplit.comparisons(network), equals(
    data.frame(t1=c("1", "1", "2", "3"), t2=c("3", "4", "4", "4"), stringsAsFactors=FALSE)))
  
  test.regress(network, likelihood="normal", link="identity", t1="2", t2="4")
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
