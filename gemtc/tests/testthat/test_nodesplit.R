context("node splitting")

test_that("change of baseline: simple permutations", {
  expect_that(cob.matrix(old=c('X', 'A'), new=c('X', 'A')), equals(diag(1)))
  expect_that(cob.matrix(old=c('X', 'A', 'B'), new=c('X', 'A', 'B')), equals(diag(2)))
  expect_that(cob.matrix(old=c('X', 'A', 'B', 'C'), new=c('X', 'A', 'B', 'C')), equals(diag(3)))
  expect_that(cob.matrix(old=c('X', 'B', 'A'), new=c('X', 'A', 'B')),
              equals(rbind(c(0, 1), c(1, 0))))
  expect_that(cob.matrix(old=c('X', 'B', 'A', 'C'), new=c('X', 'A', 'B', 'C')),
              equals(rbind(c(0, 1, 0), c(1, 0, 0), c(0, 0, 1))))
})

test_that("change of baseline: change of baseline", {
  expect_that(cob.matrix(old=c('A', 'B'), new=c('B', 'A')), equals(diag(-1, nrow=1)))
  expect_that(cob.matrix(old=c('A', 'B', 'C', 'D'), new=c('B', 'A', 'C', 'D')),
              equals(rbind(c(-1, 0, 0), c(-1, 1, 0), c(-1, 0, 1))))
})

test_that("change of baseline: shorter new", {
  expect_that(cob.matrix(old=c('A', 'B', 'C', 'D'), new=c('B', 'A', 'C')),
              equals(rbind(c(-1, 0, 0), c(-1, 1, 0))))
})

test_that("rewrite AB: 2-arm trials untouched", {
  data.ab <- read.table(textConnection("
study treatment
s01   A
s01   B
s02   A
s02   C
s03   B
s03   C"), header=T)
  expect_that(nodesplit.rewrite.data.ab(data.ab, "A", "B"), equals(data.ab))
})

test_that("rewrite AB: 3-arm trials arm removed", {
  data.ab <- read.table(textConnection("
study treatment
s01   A
s01   B
s02   A
s02   C
s03   A
s03   B
s03   C"), header=T)
  expect_that(nodesplit.rewrite.data.ab(data.ab, "A", "B"), equals(data.ab[1:6,]))
})

test_that("rewrite AB: 4+-arm trials split", {
  data.ab <- read.table(textConnection("
study treatment
s01   A
s01   B
s01   C
s01   D"), header=T)
  data.ab.rewrite <- read.table(textConnection("
study treatment
s01*  A
s01*  B
s01** C
s01** D"), header=T)
  expect_that(nodesplit.rewrite.data.ab(data.ab, "A", "B"), equals(data.ab.rewrite))
})

test_that("rewrite RE: 2-arm trials untouched", {
  data.re <- read.table(textConnection("
study treatment diff std.err
s01   A         NA   NA
s01   B         1.0  0.4
s02   A         -1.5 0.3
s02   C         NA   0.2
s03   B         NA   NA
s03   C         0.8  0.4"), header=T)
  expect_that(nodesplit.rewrite.data.re(data.re, "A", "B"), equals(data.re))
})

test_that("rewrite RE: 3-arm trials arm removed, baseline changed", {
  data.re <- read.table(textConnection("
study treatment diff std.err
s01   A         NA   0.2
s01   B         1.0  0.4
s01   C         -1.5 0.3
s02   B         NA   0.1
s02   A         -1.2 0.3
s02   C         0.8  0.4
s03   C         NA   0.1
s03   A         -1.2 0.3
s03   B         0.8  0.4"), header=T)
  data.re.rewrite <- read.table(textConnection("
study treatment diff std.err
s01   A         NA   0.2
s01   B         1.0  0.4
s02   A         NA   0.2
s02   B         1.2  0.3
s03   A         NA   0.2
s03   B         2.0  0.5"), header=T)
  levels(data.re.rewrite$treatment) <- c("A", "B", "C")
  expect_that(nodesplit.rewrite.data.re(data.re, "A", "B"), equals(data.re.rewrite))
})

test_that("rewrite RE: 4+-arm trials split, baseline changed", {
  data.re <- read.table(textConnection("
study treatment diff std.err
s01   B         NA   0.3
s01   A         0.7  0.6
s01   C         0.9  0.5
s01   D         0.5  0.6"), header=T)
  data.re.rewrite <- read.table(textConnection("
study treatment diff std.err
s01*  A         NA   0.3
s01*  B         -0.7 0.6
s01** C         NA   0.2
s01** D         -0.4 0.5"), header=T)
  expect_that(nodesplit.rewrite.data.re(data.re, "A", "B"), equals(data.re.rewrite))
})

## Regression test for issue #10
test_that("non-lexicographical treatment order works correctly", {
  data.ab <- read.table(textConnection("
study treatment mean std.err
1     1         1    0.5
1     10        1    0.5
2     1         1    0.5
2     11        1    0.5
3     2         1    0.5
3     10        1    0.5
4     10        1    0.5
4     11        1    0.5
"), header=T)
  treatments <- read.table(textConnection("
id description
1  A
2  B
10 C
11 D
"), header=T)
  network <- mtc.network(data.ab=data.ab, treatments=treatments)
  mtc.model(network, type="nodesplit", t1=10, t2=11)
})

## Regression test for issue #22
test_that("study names do not mess up nodesplit with RE data", {
  data <- read.csv("../data/ns-complex.csv")
  network <- mtc.network(data.re=data)
  expect_that(mtc.nodesplit.comparisons(network), equals(data.frame(t1=c("B", "D"), t2=c("D", "H"), stringsAsFactors=FALSE)))
  data$study <- data$Study
  data$Study <- NULL
  network <- mtc.network(data.re=data)
  expect_that(mtc.nodesplit.comparisons(network), equals(data.frame(t1=c("B", "D"), t2=c("D", "H"), stringsAsFactors=FALSE)))
})

## Regression test for issue #25
test_that("mixing AB and RE data will not duplicate comparisons", {
  data.ab <- data.frame(study=c('1', '1', '2', '2', '4', '4'), treatment=c('A', 'B', 'A', 'C', 'B', 'C'))
  data.re <- data.frame(study=c('3', '3', '3'), treatment=c('C', 'A', 'B'), diff=c(NA, 1, 1), std.err=c(0.5, 1, 1))
  network <- mtc.network(data.ab=data.ab, data.re=data.re)
  expect_that(mtc.nodesplit.comparisons(network), equals(data.frame(t1=c("A", "A", "B"), t2=c("B", "C", "C"), stringsAsFactors=FALSE)))
})
