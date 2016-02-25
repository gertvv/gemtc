test.regress <- function(network, likelihood, link, t1=NULL, t2=NULL) {
  n.trt <- nrow(network$treatments)
  devianceNames <- c("Dbar", "pD", "DIC", "data points", "dev.ab", "dev.re", "fit.ab", "fit.re")

  # consistency model, random effects
  model <- mtc.model(network, likelihood=likelihood, link=link)
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))
  plot(result)
  forest(result)
  s <- summary(result)
  expect_true("sd.d" %in% rownames(s$summaries$quantiles))
  summary(relative.effect(result, t1=network$treatments$id[2]))
  expect_equal(dim(rank.probability(result)), c(n.trt, n.trt))
  expect_equal(names(result$deviance), devianceNames)
  expect_true(!all(is.na(unlist(result$deviance[c("Dbar", "pD", "DIC")]))))

  # consistency model, fixed effect
  model <- mtc.model(network, likelihood=likelihood, link=link, linearModel="fixed")
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))
  plot(result)
  forest(result)
  s <- summary(result)
  expect_false("sd.d" %in% rownames(s$summaries$quantiles))
  summary(relative.effect(result, t1=network$treatments$id[2]))
  expect_equal(dim(rank.probability(result)), c(n.trt, n.trt))
  expect_equal(names(result$deviance), devianceNames)
  expect_true(!all(is.na(unlist(result$deviance[c("Dbar", "pD", "DIC")]))))

  if (!is.null(t1) && !is.null(t2)) {
    # node-splitting, random effects
    model <- mtc.model(network, likelihood=likelihood, link=link, type="nodesplit", t1=t1, t2=t2)
    capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))
    plot(result)
    expect_error(forest(result))
    s <- summary(result)
    expect_true("sd.d" %in% rownames(s$summaries$quantiles))
    expect_true("d.direct" %in% rownames(s$summaries$quantiles))
    expect_true("d.indirect" %in% rownames(s$summaries$quantiles))
    expect_error(relative.effect(result, t1="C"))
    expect_error(rank.probability(result))
    expect_equal(names(result$deviance), devianceNames)
    expect_true(!all(is.na(unlist(result$deviance[c("Dbar", "pD", "DIC")]))))

    # node-splitting, fixed effect
    model <- mtc.model(network, likelihood=likelihood, link=link, type="nodesplit", t1=t2, t2=t1, linearModel="fixed")
    capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))
    plot(result)
    s <- summary(result)
    expect_false("sd.d" %in% rownames(s$summaries$quantiles))
    expect_true("d.direct" %in% rownames(s$summaries$quantiles))
    expect_true("d.indirect" %in% rownames(s$summaries$quantiles))
    expect_equal(names(result$deviance), devianceNames)
    expect_true(!all(is.na(unlist(result$deviance[c("Dbar", "pD", "DIC")]))))
  }

  # ume, random effects
  suppressWarnings(model <- mtc.model(network, likelihood=likelihood, link=link, type="ume"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))
  plot(result)
  s <- summary(result)
  expect_equal(names(result$deviance), devianceNames)
  expect_true(!all(is.na(unlist(result$deviance[c("Dbar", "pD", "DIC")]))))

  # ume, fixed effect
  suppressWarnings(model <- mtc.model(network, likelihood=likelihood, link=link, type="ume", linearModel="fixed"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500))
  plot(result)
  s <- summary(result)
  expect_equal(names(result$deviance), devianceNames)
  expect_true(!all(is.na(unlist(result$deviance[c("Dbar", "pD", "DIC")]))))

  # anohe, random effects
  capture.output(anohe <- mtc.anohe(network, likelihood=likelihood, link=link, n.adapt=100, n.iter=500))
  capture.output(plot(anohe))
  anohe.s <- summary(anohe)
  plot(anohe.s)
  capture.output(print(anohe.s))
  expect_equal(names(result$deviance), devianceNames)
  expect_true(!all(is.na(unlist(result$deviance[c("Dbar", "pD", "DIC")]))))

  # anohe, fixed effect
  capture.output(anohe <- mtc.anohe(network, likelihood=likelihood, link=link, linearModel="fixed", n.adapt=100, n.iter=500))
  capture.output(plot(anohe))
  anohe.s <- summary(anohe)
  plot(anohe.s)
  capture.output(print(anohe.s))
  expect_equal(names(result$deviance), devianceNames)
  expect_true(!all(is.na(unlist(result$deviance[c("Dbar", "pD", "DIC")]))))
}
