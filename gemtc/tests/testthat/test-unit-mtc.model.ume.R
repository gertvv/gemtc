context("mtc.model.ume")

test_that("mtc.comparisons.baseline identical to mtc.comparisons for two-arm trials", {
  data <- read.table(textConnection('
    study  treatment  responders  sampleSize
        s01    A          3           10
        s01    B          5           9
        s02    B          10          40
        s02    C          10          38'), header=T)
  network <- mtc.network(data)
  expect_that(mtc.comparisons.baseline(network), equals(mtc.comparisons(network)))
})

test_that("mtc.comparisons.baseline only includes baseline comparisons for multi-arm trials", {
  data <- read.table(textConnection('
    study  treatment  responders  sampleSize
        s01    A          3           10
        s01    B          5           9
        s01    C          10          40'), header=T)
  network <- mtc.network(data)
  expected <- data.frame(
    t1=as.treatment.factor(c('A', 'A'), network),
    t2=as.treatment.factor(c('B', 'C'), network))
  expect_that(mtc.comparisons.baseline(network), equals(expected))
})

test_that("mtc.comparisons.baseline respects baseline in data.re", {
  data <- read.table(textConnection('
    study  treatment  diff  std.err
        s01    A          2.0   0.5
        s01    B          NA    0.3
        s01    C          1.5   0.6'), header=T)
  network <- mtc.network(data.re=data)
  expected <- data.frame(
    t1=as.treatment.factor(c('B', 'B'), network),
    t2=as.treatment.factor(c('A', 'C'), network))
  expect_that(mtc.comparisons.baseline(network), equals(expected))
})

test_that("mtc.model.ume warns about mutli-arm trials", {
  data <- read.table(textConnection('
    study  treatment  diff  std.err
        s01    A          2.0   0.5
        s01    B          NA    0.3
        s01    C          1.5   0.6'), header=T)
  network <- mtc.network(data.re=data)
    expect_warning(mtc.model(network, type='ume', likelihood='normal', link='identity'), "multi-arm trials")
})

test_that("Vertices agree between mtc.network.graph and ume model$graph", {
    network <- thrombolytic
    suppressWarnings(model <- mtc.model(network, type='ume'))
    graph <- mtc.network.graph(network)
    expect_that(V(model$graph)$name, equals(V(graph)$name))
    expect_that(V(mtc.model.graph(model))$name, equals(V(graph)$name))
})

test_that("Edges are consistent for ume model$graph", {
  data <- read.table(textConnection('
    study  treatment  diff  std.err
        s01    A          2.0   0.5
        s01    B          NA    0.3
        s01    C          1.5   0.6'), header=T)
  network <- mtc.network(data.re=data)
  suppressWarnings(model <- mtc.model(network, type='ume', likelihood='normal', link='identity'))

  expect_that(length(E(model$graph)), equals(2))
  expect_that(model$graph['A', 'B'], equals(1))
  expect_that(model$graph['B', 'C'], equals(1))
})

## Regression test for #26
test_that("RE data will not introduce duplicate basic parameters", {
  data.ab <- data.frame(
    study=c('1', '1', '2', '2', '3', '3'),
    treatment=c('A', 'B', 'A', 'C', 'B', 'C'),
    mean=rep(1,6), std.err=rep(0.5,6))
  data.re <- data.re <- data.frame(study=c('4', '4'), treatment=c('C', 'A'), diff=c(NA, 1), std.err=c(0.3, 0.5))
  network <- mtc.network(data.ab=data.ab, data.re=data.re)

  model <- mtc.model(network, type='ume', likelihood='normal', link='identity')

  expect_that(length(E(model$graph)), equals(3))
  expect_that(model$graph['A', 'B'], equals(1))
  expect_that(model$graph['A', 'C'], equals(1))
  expect_that(model$graph['B', 'C'], equals(1))

  # check that the relative effects matrix has the correct entries
  expect_that(grep("d\\[1, 2\\] <- d.A.B", model$code), equals(1))
  expect_that(grep("d\\[1, 3\\] <- d.A.C", model$code), equals(1))
  expect_that(grep("d\\[2, 3\\] <- d.B.C", model$code), equals(1))
  expect_that(grep("d\\[3, 1\\] <- -d.A.C", model$code), equals(1))
})

test_that("func.param.matrix was implemented correctly", {
  model <- list(
    'type'='ume',
    'graph'=igraph::make_graph(c('A','B','A','C','B','C','B','D')))

  expect_equal(matrix(c(1,0,0,0), nrow=4, dimnames=list(NULL, 'd.A.B')),
               mtc.model.call('func.param.matrix', model, t1='A', t2='B'))
  expect_equal(matrix(c(1,0,0,0,0,1,0,0), nrow=4, dimnames=list(NULL, c('d.A.B', 'd.A.C'))),
               mtc.model.call('func.param.matrix', model, t1='A', t2=c('B', 'C')))
  expect_equal(matrix(c(0,0,0,-1), nrow=4, dimnames=list(NULL, c('d.D.B'))),
               mtc.model.call('func.param.matrix', model, t1='D', t2='B'))
  expect_error(mtc.model.call('func.param.matrix', model, t1='A', t2='D'))
})
