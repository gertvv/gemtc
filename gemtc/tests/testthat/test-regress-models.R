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

test_that("certolizumab regression (binomial data, continuous covariate)", {
  model <- mtc.model(certolizumab, type="regression", linearModel="fixed", 
                     regressor=list(control="Placebo", coefficient="unrelated", variable="diseaseDuration"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))

  model <- mtc.model(certolizumab, type="regression", linearModel="fixed", 
                     regressor=list(control="Placebo", coefficient="shared", variable="diseaseDuration"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))

  model <- mtc.model(certolizumab, type="regression", linearModel="fixed", 
                     regressor=list(control="Placebo", coefficient="exchangeable", variable="diseaseDuration"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))

  model <- mtc.model(certolizumab, type="regression", linearModel="random", 
                     regressor=list(control="Placebo", coefficient="unrelated", variable="diseaseDuration"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))

  model <- mtc.model(certolizumab, type="regression", linearModel="random", 
                     regressor=list(control="Placebo", coefficient="shared", variable="diseaseDuration"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))

  model <- mtc.model(certolizumab, type="regression", linearModel="random", 
                     regressor=list(control="Placebo", coefficient="exchangeable", variable="diseaseDuration"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))
})

test_that("atrial fibrillation regression (classes, binomial data, continuous covariate)", {
  classes <- list("control"=c(1),
                  "anti-coagulant"=c(2,3,4,9),
                  "anti-platelet"=c(5,6,7,8,10,11,12,16,17),
                  "mixed"=c(13,14,15))

  model <- mtc.model(atrialFibrillation, type="regression", linearModel="fixed", 
                     regressor=list(classes=classes, coefficient="shared", variable="stroke"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))

  model <- mtc.model(atrialFibrillation, type="regression", linearModel="random", 
                     regressor=list(classes=classes, coefficient="shared", variable="stroke"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))
})

test_that("heart failure prevention (binomial data, binary covariate)", {
  model <- mtc.model(hfPrevention, type="regression", linearModel="fixed", 
                     regressor=list(control="control", coefficient="unrelated", variable="secondary"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))

  model <- mtc.model(hfPrevention, type="regression", linearModel="fixed", 
                     regressor=list(control="control", coefficient="shared", variable="secondary"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))

  model <- mtc.model(hfPrevention, type="regression", linearModel="fixed", 
                     regressor=list(control="control", coefficient="exchangeable", variable="secondary"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))

  model <- mtc.model(hfPrevention, type="regression", linearModel="random", 
                     regressor=list(control="control", coefficient="unrelated", variable="secondary"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))

  model <- mtc.model(hfPrevention, type="regression", linearModel="random", 
                     regressor=list(control="control", coefficient="shared", variable="secondary"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))

  model <- mtc.model(hfPrevention, type="regression", linearModel="random", 
                     regressor=list(control="control", coefficient="exchangeable", variable="secondary"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))
})
