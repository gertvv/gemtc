test.regress <- function(network, likelihood, link, t1=NULL, t2=NULL) {
  n.trt <- nrow(network$treatments)

  # consistency model, random effects
  model <- mtc.model(network, likelihood=likelihood, link=link)
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500, sampler=get.sampler()))
  plot(result)
  forest(result)
  s <- summary(result)
  expect_true("sd.d" %in% rownames(s$summaries$quantiles))
  summary(relative.effect(result, t1=network$treatments$id[2]))
  expect_equal(dim(rank.probability(result)), c(n.trt, n.trt))

  # consistency model, fixed effect
  model <- mtc.model(network, likelihood=likelihood, link=link, linearModel="fixed")
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500, sampler=get.sampler()))
  plot(result)
  forest(result)
  s <- summary(result)
  expect_false("sd.d" %in% rownames(s$summaries$quantiles))
  summary(relative.effect(result, t1=network$treatments$id[2]))
  expect_equal(dim(rank.probability(result)), c(n.trt, n.trt))

  if (!is.null(t1) && !is.null(t2)) {
    # node-splitting, random effects
    model <- mtc.model(network, likelihood=likelihood, link=link, type="nodesplit", t1=t1, t2=t2)
    capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500, sampler=get.sampler()))
    plot(result)
    expect_error(forest(result))
    s <- summary(result)
    expect_true("sd.d" %in% rownames(s$summaries$quantiles))
    expect_true("d.direct" %in% rownames(s$summaries$quantiles))
    expect_true("d.indirect" %in% rownames(s$summaries$quantiles))
    expect_error(relative.effect(result, t1="C"))
    expect_error(rank.probability(result))

    # node-splitting, fixed effect
    model <- mtc.model(network, likelihood=likelihood, link=link, type="nodesplit", t1=t2, t2=t1, linearModel="fixed")
    capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500, sampler=get.sampler()))
    plot(result)
    s <- summary(result)
    expect_false("sd.d" %in% rownames(s$summaries$quantiles))
    expect_true("d.direct" %in% rownames(s$summaries$quantiles))
    expect_true("d.indirect" %in% rownames(s$summaries$quantiles))
  }

  # ume, random effects
  suppressWarnings(model <- mtc.model(network, likelihood=likelihood, link=link, type="ume"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500, sampler=get.sampler()))
  plot(result)
  s <- summary(result)

  # ume, fixed effect
  suppressWarnings(model <- mtc.model(network, likelihood=likelihood, link=link, type="ume", linearModel="fixed"))
  capture.output(result <- mtc.run(model, n.adapt=100, n.iter=500, sampler=get.sampler()))
  plot(result)
  s <- summary(result)

  # anohe, random effects
  capture.output(anohe <- mtc.anohe(network, likelihood=likelihood, link=link, n.adapt=100, n.iter=500, sampler=get.sampler()))
  capture.output(plot(anohe))
  anohe.s <- summary(anohe)
  plot(anohe.s)
  capture.output(print(anohe.s))

  # anohe, fixed effect
  capture.output(anohe <- mtc.anohe(network, likelihood=likelihood, link=link, linearModel="fixed", n.adapt=100, n.iter=500, sampler=get.sampler()))
  capture.output(plot(anohe))
  anohe.s <- summary(anohe)
  plot(anohe.s)
  capture.output(print(anohe.s))
}
