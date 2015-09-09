context("relative.effect and rank.probability")

test_that("relative.effect outputs the correct parameters", {
  result <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))

  expect_that(colnames(result$samples[[1]]), equals(c("d.A.B", "d.A.C", "d.A.D", "sd.d")))

  out <- relative.effect(result, "A", preserve.extra=TRUE)
  expect_that(colnames(out$samples[[1]]), equals(c("d.A.B", "d.A.C", "d.A.D", "sd.d"))) #1

  out <- relative.effect(result, "A", preserve.extra=FALSE)
  expect_that(colnames(out$samples[[1]]), equals(c("d.A.B", "d.A.C", "d.A.D"))) #2

  out <- relative.effect(result, "B")
  expect_that(colnames(out$samples[[1]]), equals(c("d.B.A", "d.B.C", "d.B.D", "sd.d"))) #3

  out <- relative.effect(result, "B", "C")
  expect_that(colnames(out$samples[[1]]), equals(c("d.B.C", "sd.d")))

  out <- relative.effect(result, "B", c("A", "B", "C"))
  expect_that(colnames(out$samples[[1]]), equals(c("d.B.A", "d.B.B", "d.B.C", "sd.d")))

  out <- relative.effect(result, c("A", "B"), c("C"))
  expect_that(colnames(out$samples[[1]]), equals(c("d.A.C", "d.B.C", "sd.d")))
})

test_that("relative.effect generates the expected statistics", {
  result <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))
  stats <- summary(relative.effect(result, "B"))$summaries

  expected <- textConnection('
           Mean     SD  Naive.SE Time-series.SE
  d.B.A -0.4965 0.4081 0.004563       0.004989
  d.B.C  0.3394 0.4144 0.004634       0.004859
  d.B.D  0.6123 0.4789 0.005354       0.005297
  sd.d   0.8465 0.1913 0.002139       0.002965
  ')
  expected <- as.matrix(read.table(expected, header=TRUE))
  colnames(expected)[3] <- "Naive SE"
  colnames(expected)[4] <- "Time-series SE"
  expect_that(stats$statistics, equals(expected, tolerance=0.0001, scale=1))
})

test_that("tree.relative.effect handles a simple tree", {
  g <- graph.edgelist(t(matrix(c("A", "B", "A", "C", "A", "D"), nrow=2)))

  expected <- do.call(cbind, list(
    d.B.A = c(-1, 0, 0),
    d.B.C = c(-1, 1, 0),
    d.B.D = c(-1, 0, 1))
  )

  expect_that(tree.relative.effect(g, t1=2, t2=c()), equals(expected))
})

test_that("tree.relative.effect handles a more complex tree", {
  network <- thrombolytic
  tree <- minimum.diameter.spanning.tree(mtc.network.graph(network))

  expected <- do.call(cbind, list(
      d.tPa.Ten = c(
        1, 0, -1, # +ASPAC.AtPA -ASPAC.tPA
        0, 0, 1, 0) # +AtPA.Ten
      ))
  expect_that(tree.relative.effect(tree, t1="tPA", t2="Ten"), is_equivalent_to(expected))
})

test_that("tree.relative.effect handles two-treatment case", {
  g <- graph.edgelist(matrix(c("A", "B"), ncol=2))
  expected <- matrix(-1, dimnames=list(NULL, c("d.B.A")))
  expect_that(tree.relative.effect(g, t1=2, t2=c()), equals(expected))

  expected <- matrix(c(0, 1), dimnames=list(NULL, c("d.A.A", "d.A.B")), ncol=2)
  expect_that(tree.relative.effect(g, t1=1, t2=c(1, 2)), equals(expected))
})

test_that("relative.effect can be applied recursively", {
  result <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))
  result <- relative.effect(result, "C")
  stats <- summary(relative.effect(result, "B"))$summaries

  expected <- textConnection('
           Mean     SD  Naive.SE Time-series.SE
  d.B.A -0.4965 0.4081 0.004563       0.004989
  d.B.C  0.3394 0.4144 0.004634       0.004859
  d.B.D  0.6123 0.4789 0.005354       0.005297
  sd.d   0.8465 0.1913 0.002139       0.002965
  ')
  expected <- as.matrix(read.table(expected, header=TRUE))
  colnames(expected)[3] <- "Naive SE"
  colnames(expected)[4] <- "Time-series SE"
  expect_that(stats$statistics, equals(expected, tolerance=0.0001, scale=1))
})

test_that("rank.probability returns the right results", {
  expected <- structure(c(0, 0.058, 0.22825, 0.71375, 0.0025, 0.176, 0.60075, 
                          0.22075, 0.105125, 0.66175, 0.170625, 0.0625, 0.892375, 0.10425, 
                          0.000375, 0.003),
                        .Dim = c(4L, 4L),
                        .Dimnames = list(c("A", "B", "C", "D"), NULL),
                        class = "mtc.rank.probability", direction = 1)
  result <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))
  expect_that(rank.probability(result), equals(expected))
})

test_that("rank.probability can be applied to a subset of parameters", {
  result <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))
  result <- relative.effect(result, "A", c("B", "C"))
  rank.probability(result)
})

test_that("relative.effect.tree throws an error if requested comparison is not connected", {
  g <- graph.edgelist(t(matrix(c("A", "B", "A", "C"), nrow=2)))
  g <- g + vertex("D")

  expect_error(tree.relative.effect(g, t1=1, t2=4))
})

test_that("spanning.tree.mtc.result handles two-treatment case", {
  result <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))
  result <- relative.effect(result, t1="A", t2="B")
  
  g <- graph.edgelist(matrix(c("A", "B"), ncol=2))
  g <- g + vertices(c("C", "D"))

  h <- spanning.tree.mtc.result(result)

  expect_that(V(h)$name, equals(V(g)$name))
  expect_that(h[,], equals(g[,]))
})

test_that("relative.effect is robust to missing sd.d", {
  result <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))

  out <- relative.effect(result, "A", preserve.extra=FALSE)
  expect_that(colnames(out$samples[[1]]), equals(c("d.A.B", "d.A.C", "d.A.D"))) #2

  out <- relative.effect(out, "A", preserve.extra=TRUE)
  expect_that(colnames(out$samples[[1]]), equals(c("d.A.B", "d.A.C", "d.A.D"))) #1
})

test_that("relative.effect is robust to leading columns", {
  result <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))
  leading <- matrix(rep(255,nrow(result[['samples']][[1]])), dimnames=list(NULL, "deviance"))
  for (i in 1:4) {
    result[['samples']][[i]] <- as.mcmc(cbind(leading, result[['samples']][[i]]))
  }

  stats <- summary(relative.effect(result, "B", preserve.extra=FALSE))$summaries

  expected <- textConnection('
           Mean     SD  Naive.SE Time-series.SE
  d.B.A -0.4965 0.4081 0.004563       0.004989
  d.B.C  0.3394 0.4144 0.004634       0.004859
  d.B.D  0.6123 0.4789 0.005354       0.005297
  ')
  expected <- as.matrix(read.table(expected, header=TRUE))
  colnames(expected)[3] <- "Naive SE"
  colnames(expected)[4] <- "Time-series SE"
  expect_that(stats$statistics, equals(expected, tolerance=0.0001, scale=1))
})
