context("relative.effect")

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
  stats <- summary(relative.effect(result, "B"))

  expected <- textConnection('
           Mean     SD  Naive.SE Time-series.SE
  d.B.A -0.4901 0.4041 0.0014286       0.002935
  d.B.C  0.3429 0.4158 0.0014700       0.002715
  d.B.D  0.6092 0.4868 0.0017210       0.003421
  sd.d   0.8430 0.1853 0.0006551       0.002481
  ')
  expected <- as.matrix(read.table(expected, header=TRUE))
  colnames(expected)[3] <- "Naive SE"
  colnames(expected)[4] <- "Time-series SE"
  expect_that(stats$statistics, equals(expected, tolerance=0.00005, scale=1))
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
  network <- read.mtc.network(system.file("extdata/luades-thrombolytic.gemtc", package="gemtc"))
  tree <- minimum.diameter.spanning.tree(mtc.network.graph(network))

  expected <- do.call(cbind, list(
      d.tPa.Ten = c(
        1, 0, -1, # +ASPAC.AtPA -ASPAC.tPA
        0, 0, 1, 0) # +AtPA.Ten
      ))
  expect_that(tree.relative.effect(tree, t1="tPA", t2="Ten"), is_equivalent_to(expected))
})
