context("mtc.model.inits")

test_that("mtc.init.mle.regression just returns 0 +/- om.scale", {
  data.ab <- read.table(textConnection('
study treatment responders sampleSize
Brown control 0 52
Brown statinA 1 94
CCAIT control 2 166
CCAIT statinA 2 165
Downs control 77 3301
Downs statinB 80 3304
EXCEL control 3 1663
EXCEL statinB 33 6582'), header=TRUE, stringsAsFactors=FALSE)

  network <- mtc.network(data.ab=data.ab)
  model <- list(network=network,
                type='regression',
                likelihood='binom',
                link='logit',
                om.scale=2.5,
                n.chain=4,
                var.scale=2.5,
                linearModel='random',
                tree=minimum.diameter.spanning.tree(mtc.network.graph(network)),
                hy.prior=mtc.hy.prior("std.dev", "dunif", 0, "om.scale"),
                data=list(x=c(1,1,0,0),nt=3),
                regressor=list('variable'='x', 'coefficient'='shared', 'control'=as.treatment.factor('control', network)))

  expected <- read.table(textConnection('
parameter type        mean std.err
B         coefficient 0.0  2.5'), header=TRUE, stringsAsFactors=FALSE)
  expect_equal(mtc.init.mle.regression(model), expected, tolerance=1E-5)

  expected <- read.table(textConnection('
parameter type        mean std.err
beta[2]   coefficient 0.0  2.5
beta[3]   coefficient 0.0  2.5'), header=TRUE, stringsAsFactors=FALSE)
  model[['regressor']][['coefficient']] <- 'unrelated'
  expect_equal(mtc.init.mle.regression(model), expected, tolerance=1E-5)

  expected <- read.table(textConnection('
parameter type        mean std.err
beta[2]   coefficient 0.0  2.5
beta[3]   coefficient 0.0  2.5
B         coefficient 0.0  2.5'), header=TRUE, stringsAsFactors=FALSE)
  model[['regressor']][['coefficient']] <- 'exchangeable'
  expect_equal(mtc.init.mle.regression(model), expected, tolerance=1E-5)

  expected <- read.table(textConnection('
parameter type        mean std.err
B[2]      coefficient 0.0  2.5'), header=TRUE, stringsAsFactors=FALSE)
  model[['regressor']][['coefficient']] <- 'unrelated'
  model[['regressor']][['classes']] <- list('control'=as.treatment.factor('control', network), 'statin'=as.treatment.factor(c('statinA', 'statinB'), network))
  model[['regressor']][['control']] <- NULL
  expect_equal(mtc.init.mle.regression(model), expected, tolerance=1E-5)
})

test_that("mtc.linearModel.matrix works correctly", {
  data.ab <- read.table(textConnection('
study treatment mean std.err
1     A         10.5 0.18
1     B         15.3 0.17
2     B         15.7 0.12
2     C         18.3 0.15
3     B         13.1 0.19
3     C         14.2 0.20'), header=T)
  data.re <- read.table(textConnection('
study treatment diff std.err
4     A         NA   0.15
4     B         3.1  0.22
4     C         4.2  0.24'), header=T)
  network <- mtc.network(data.ab=data.ab, data.re=data.re)
  model <- list(network=network,
                type='consistency',
                likelihood='normal',
                link='identity',
                om.scale=2.5,
                n.chain=4,
                var.scale=2.5,
                linearModel='random',
                tree=minimum.diameter.spanning.tree(mtc.network.graph(network)),
                hy.prior=mtc.hy.prior("std.dev", "dunif", 0, "om.scale"))

  params.re <- c("mu[1]", "mu[2]", "mu[3]", "delta[1,2]", "delta[2,2]", "delta[3,2]", "delta[4,2]", "delta[4,3]", "d.A.B", "d.A.C")
  expected.re <- rbind(c(1, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                       c(1, 0, 0, 1, 0, 0, 0, 0, 0, 0),
                       c(0, 1, 0, 0, 0, 0, 0, 0, 0, 0),
                       c(0, 1, 0, 0, 1, 0, 0, 0, 0, 0),
                       c(0, 0, 1, 0, 0, 0, 0, 0, 0, 0),
                       c(0, 0, 1, 0, 0, 1, 0, 0, 0, 0),
                       c(0, 0, 0, 0, 0, 0, 1, 0, 0, 0),
                       c(0, 0, 0, 0, 0, 0, 0, 1, 0, 0))
  expect_equal(mtc.linearModel.matrix(model, params.re), expected.re)

  params.fe <- c("mu[1]", "mu[2]", "mu[3]", "d.A.B", "d.A.C")
  expected.fe <- rbind(c(1, 0, 0,  0, 0),
                       c(1, 0, 0,  1, 0),
                       c(0, 1, 0,  0, 0),
                       c(0, 1, 0, -1, 1),
                       c(0, 0, 1,  0, 0),
                       c(0, 0, 1, -1, 1),
                       c(0, 0, 0,  1, 0),
                       c(0, 0, 0,  0, 1))
  model[['linearModel']] <- 'fixed'
  expect_equal(mtc.linearModel.matrix(model, params.fe), expected.fe)

  studies <- c("1"=TRUE, "2"=TRUE, "3"=FALSE, "4"=FALSE)
  params.omit <- c("mu[1]", "mu[2]", "delta[1,2]", "delta[2,2]", "d.A.B", "d.A.C")
  expected.omit <- rbind(c(1, 0, 0, 0, 0, 0),
                         c(1, 0, 1, 0, 0, 0),
                         c(0, 1, 0, 0, 0, 0),
                         c(0, 1, 0, 1, 0, 0))
  model[['linearModel']] <- 'random'
  expect_equal(mtc.linearModel.matrix(model, params.omit, includedStudies=studies), expected.omit)
})

test_that("mtc.linearModel.matrix works correctly for regression", {
  data.ab <- read.table(textConnection('
study treatment mean std.err
1     A         10.5 0.18
1     B         15.3 0.17
2     B         15.7 0.12
2     C         18.3 0.15
3     B         13.1 0.19
3     C         14.2 0.20'), header=T)
  data.re <- read.table(textConnection('
study treatment diff std.err
4     A         NA   0.15
4     B         3.1  0.22
4     C         4.2  0.24'), header=T)

  network <- mtc.network(data.ab=data.ab, data.re=data.re)
  model <- list(network=network,
                type='regression',
                likelihood='normal',
                link='identity',
                om.scale=2.5,
                n.chain=4,
                var.scale=2.5,
                linearModel='random',
                data=list(x=c(1,0,0.5,0.3), nt=3),
                tree=minimum.diameter.spanning.tree(mtc.network.graph(network)),
                regressor=list('variable'='x', 'coefficient'='shared', 'control'=as.treatment.factor('A', network)),
                hy.prior=mtc.hy.prior("std.dev", "dunif", 0, "om.scale"))

  params.re <- c("mu[1]", "mu[2]", "mu[3]", "delta[1,2]", "delta[2,2]", "delta[3,2]", "delta[4,2]", "delta[4,3]", "d.A.B", "d.A.C", "B")
  expected.re <- rbind(c(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                       c(1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1),
                       c(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                       c(0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0),
                       c(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0),
                       c(0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0),
                       c(0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0.3),
                       c(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0.3))
  expect_equal(mtc.linearModel.matrix(model, params.re), expected.re)

  params.fe <- c("mu[1]", "mu[2]", "mu[3]", "d.A.B", "d.A.C", "B")
  expected.fe <- rbind(c(1, 0, 0,  0, 0, 0),
                       c(1, 0, 0,  1, 0, 1),
                       c(0, 1, 0,  0, 0, 0),
                       c(0, 1, 0, -1, 1, 0),
                       c(0, 0, 1,  0, 0, 0),
                       c(0, 0, 1, -1, 1, 0),
                       c(0, 0, 0,  1, 0, 0.3),
                       c(0, 0, 0,  0, 1, 0.3))
  model[['linearModel']] <- 'fixed'
  expect_equal(mtc.linearModel.matrix(model, params.fe), expected.fe)
})

test_that("likelihood.arm.list returns the correct arms", {
  data.ab <- read.table(textConnection('
study treatment mean std.err
s1    A         10.5 0.18
s1    B         15.3 0.17
s2    B         15.7 0.12
s2    C         18.3 0.15
s2    D         18.3 0.15
s3    B         13.1 0.19
s3    C         14.2 0.20'), header=T)
  data.re <- read.table(textConnection('
study treatment diff std.err
s4    A         NA   0.15
s4    B         3.1  0.22
s4    C         4.2  0.24'), header=T)
  network <- mtc.network(data.ab=data.ab, data.re=data.re)

  expected1 <- read.table(textConnection('
study studyIndex armIndex t1 t2
s1    1          2        A  B
s2    2          2        B  C
s2    2          3        B  D
s3    3          2        B  C
s4    4          2        A  B
s4    4          3        A  C'), header=TRUE, stringsAsFactors=FALSE)
  expect_equal(likelihood.arm.list(network, baseline=FALSE), expected1)

  expected2 <- read.table(textConnection('
study studyIndex armIndex t1 t2
s1    1          1        NA NA
s1    1          2        A  B
s2    2          1        NA NA
s2    2          2        B  C
s2    2          3        B  D
s3    3          1        NA NA
s3    3          2        B  C
s4    4          2        A  B
s4    4          3        A  C'), header=TRUE, stringsAsFactors=FALSE)
  expect_equal(likelihood.arm.list(network, baseline=TRUE), expected2)

  expected3 <- read.table(textConnection('
study studyIndex armIndex t1 t2
s1    1          1        NA NA
s1    1          2        A  B
s3    3          1        NA NA
s3    3          2        B  C
s4    4          2        A  B
s4    4          3        A  C'), header=TRUE, stringsAsFactors=FALSE)
  expect_equal(likelihood.arm.list(network, baseline=TRUE, includedStudies=c("s1"=TRUE, "s2"=FALSE, "s3"=TRUE, "s4"=TRUE)), expected3)
})

test_that("mtc.model.inits has correct shape", {
  data.ab <- read.table(textConnection('
study treatment mean std.err
1     A         10.5 0.18
1     B         15.3 0.17
2     B         15.7 0.12
2     C         18.3 0.15
3     B         13.1 0.19
3     C         14.2 0.20'), header=T)
  data.re <- read.table(textConnection('
study treatment diff std.err
4     A         NA   0.15
4     B         3.1  0.22
4     C         4.2  0.24
5     A         2.9  0.28
5     B         NA   NA
6     A         0.2  0.23
6     B         0.8  0.27
6     C         NA   0.16
7     A         NA   0.18
7     B         4.3  0.21
8     B         NA   NA
8     C         4.0  0.19'), header=T)
  network <- mtc.network(data.ab, data.re=data.re)
  model <- list(network=network,
                type='consistency',
                likelihood='normal',
                link='identity',
                om.scale=2.5,
                n.chain=4,
                var.scale=2.5,
                linearModel='random',
                tree=minimum.diameter.spanning.tree(mtc.network.graph(network)),
                hy.prior=mtc.hy.prior("std.dev", "dunif", 0, "om.scale"))
  inits <- mtc.init(model)
  whereNA <- rbind(
    c(NA, 1, NA), # 1
    c(NA, 1, NA), # 2
    c(NA, 1, NA), # 3
    c(NA, 1, NA), # 5
    c(NA, 1, NA), # 7
    c(NA, 1, NA), # 8
    c(NA, 1, 1),  # 4
    c(NA, 1, 1))  # 6

  expect_equal(is.na(inits[[1]]$delta), is.na(whereNA))
  expect_equal(is.na(inits[[2]]$delta), is.na(whereNA))
  expect_equal(is.na(inits[[3]]$delta), is.na(whereNA))
  expect_equal(is.na(inits[[4]]$delta), is.na(whereNA))
  expect_equal(length(inits[[1]]$mu), 3)
  expect_equal(length(inits[[2]]$mu), 3)
  expect_equal(length(inits[[3]]$mu), 3)
  expect_equal(length(inits[[4]]$mu), 3)
})

test_that("mtc.model.inits - regression parameters have the correct shape", {
  data.ab <- read.table(textConnection('
study treatment mean std.err
1     A         10.5 0.18
1     B         15.3 0.17
2     B         15.7 0.12
2     C         18.3 0.15
3     B         13.1 0.19
3     C         14.2 0.20'), header=T)
  studies <- read.table(textConnection('
study x 
1     0
2     1
3     1'), header=T)
  network <- mtc.network(data.ab=data.ab, studies=studies)
  ts <- as.treatment.factor(c('A','B','C'), network)
  model <- list(network=network,
                type='regression',
                likelihood='normal',
                link='identity',
                om.scale=2.5,
                n.chain=4,
                var.scale=2.5,
                linearModel='fixed',
                tree=minimum.diameter.spanning.tree(mtc.network.graph(network)),
                hy.prior=mtc.hy.prior("std.dev", "dunif", 0, "om.scale"),
                data=list(x=c(0,1,1),nt=3),
                regressor=list('variable'='x', 'coefficient'='unrelated', 'control'=ts[1]))

  inits <- mtc.init(model)
  expect_equal(is.na(inits[[1]]$beta), c(TRUE, FALSE, FALSE))

  model$regressor$control <- ts[2]
  inits <- mtc.init(model)
  expect_equal(is.na(inits[[1]]$beta), c(FALSE, TRUE, FALSE))

  model$regressor$control <- ts[3]
  inits <- mtc.init(model)
  expect_equal(is.na(inits[[1]]$beta), c(FALSE, FALSE, TRUE))

  model$regressor$coefficient <- 'shared'
  inits <- mtc.init(model)
  expect_equal(inits[[1]]$beta, NULL)
  expect_equal(is.na(inits[[1]]$B), FALSE)

  model$regressor$coefficient <- 'exchangeable'
  inits <- mtc.init(model)
  expect_equal(is.na(inits[[1]]$beta), c(FALSE, FALSE, TRUE))
  expect_equal(is.na(inits[[1]]$B), FALSE)
  expect_equal(is.na(inits[[1]]$reg.sd), FALSE)

  model$regressor$coefficient <- 'shared'
  model$regressor$control <- NULL
  model$regressor$classes <- list('control'=ts[1], 'other'=ts[2:3])
  inits <- mtc.init(model)
  expect_equal(is.na(inits[[1]]$B), c(TRUE, FALSE))
})

test_that("mtc.model.inits has correct heterogeneity parameter", {
  data.ab <- read.table(textConnection('
study treatment mean std.err
1     A         10.5 0.18
1     B         15.3 0.17
2     B         15.7 0.12
2     C         18.3 0.15
3     B         13.1 0.19
3     C         14.2 0.20'), header=T)
  network <- mtc.network(data.ab)
  model <- list(network=network,
                type='consistency',
                likelihood='normal',
                link='identity',
                om.scale=0.1,
                n.chain=4,
                var.scale=2.5,
                linearModel='random',
                tree=minimum.diameter.spanning.tree(mtc.network.graph(network)))

  # standard deviation prior
  model[['hy.prior']] <- mtc.hy.prior("std.dev", "dunif", 0, "om.scale")
  inits <- mtc.init(model)
  expect_true("sd.d" %in% names(inits[[1]]))
  expect_false("var.d" %in% names(inits[[1]]))
  expect_false("tau.d" %in% names(inits[[1]]))
  expect_true(inits[[1]][['sd.d']] <= 0.1)
  expect_true(inits[[1]][['sd.d']] >= 0.0)

  # variance prior
  model[['hy.prior']] <- mtc.hy.prior("var", "dlnorm", -4, 2)
  inits <- mtc.init(model)
  expect_false("sd.d" %in% names(inits[[1]]))
  expect_true("var.d" %in% names(inits[[1]]))
  expect_false("tau.d" %in% names(inits[[1]]))
  expect_true(inits[[1]][['var.d']] >= 0.0)

  # precision prior
  model[['hy.prior']] <- mtc.hy.prior("prec", "dgamma", 0.01, 0.01)
  inits <- mtc.init(model)
  expect_false("sd.d" %in% names(inits[[1]]))
  expect_false("var.d" %in% names(inits[[1]]))
  expect_true("tau.d" %in% names(inits[[1]]))
  expect_true(inits[[1]][['tau.d']] >= 0.0)
})

test_that('mtc.init correctly restrains the baseline probability', {
  # based on an example where initial values often violated the p < 1 constraint
  network <- mtc.network(read.csv('../data/rr-pairwise.csv'))
  model <- list(network=network,
                type='consistency',
                likelihood='binom',
                link='log',
                var.scale=10,
                om.scale=2,
                n.chain=4,
                linearModel='fixed',
                tree=minimum.diameter.spanning.tree(mtc.network.graph(network)))

  for (i in 1:50) {
    inits <- mtc.init(model)
    base <- sapply(inits, function(x) { x$p.base })
    expect_true(all(base < 1))
    expect_true(all(base > 0))
    rel <- sapply(inits, function(x) { x$p.base * exp(x$d.10.12) })
    expect_true(all(rel < 1))
    expect_true(all(rel > 0))
  }
})
