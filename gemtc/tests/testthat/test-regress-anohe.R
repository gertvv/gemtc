context("Regressions previously observed for ANOHE")

test_that("anohe-breaking.gemtc does not break summary.mtc.anohe", {
  network <- read.mtc.network("../data/anohe-breaking.gemtc")
  capture.output(anohe <- mtc.anohe(network, n.adapt=200, n.iter=500, sampler=get.sampler()))
  x <- summary(anohe)
  expect_true('studyEffects' %in% names(x))
})

test_that("Mixing up the order of treatments does not break summary.mtc.anohe", {
  network <- read.mtc.network(system.file("extdata/luades-thrombolytic.gemtc", package="gemtc"))
  treatments <- network$treatments
  treatments$id <- factor(rev(as.character(treatments$id)), levels=rev(as.character(treatments$id)))
  network <- mtc.network(data=network$data.ab, treatments=treatments)
  capture.output(anohe <- mtc.anohe(network, n.adapt=200, n.iter=500, sampler=get.sampler()))
  x <- summary(anohe)
  expect_true('studyEffects' %in% names(x))
})
