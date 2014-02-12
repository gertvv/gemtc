context("has.indirect.evidence")

test_that("single pair-wise comparison gives FALSE", {
  data <- read.table(textConnection('study treatment
s01 A
s01 B'), header=T)
  network <- mtc.network(data)
  expect_that(has.indirect.evidence(network, 'A', 'B'), equals(FALSE))
})

test_that("comparison in triangle gives TRUE", {
  data <- read.table(textConnection('study treatment
s01 A
s01 B
s02 B
s02 C
s03 C
s03 A'), header=T)
  network <- mtc.network(data)
  expect_that(has.indirect.evidence(network, 'A', 'B'), equals(TRUE))

  data <- read.table(textConnection('study treatment
s01 A
s01 B
s01 C
s02 B
s02 C
s03 C
s03 A'), header=T)
  network <- mtc.network(data)
  expect_that(has.indirect.evidence(network, 'A', 'B'), equals(TRUE))
})

test_that("comparison in three-arm trial gives FALSE", {
  data <- read.table(textConnection('study treatment
s01 A
s01 B
s01 C'), header=T)
  network <- mtc.network(data)
  expect_that(has.indirect.evidence(network, 'A', 'B'), equals(FALSE))

  data <- read.table(textConnection('study treatment
s01 A
s01 B
s01 C
s02 A
s02 C'), header=T)
  network <- mtc.network(data)
  expect_that(has.indirect.evidence(network, 'A', 'B'), equals(FALSE))
})

test_that("four-arm trials are handled correctly", {
  data <- read.table(textConnection('study treatment
s01 A
s01 B
s01 C
s01 D'), header=T)
  network <- mtc.network(data)
  expect_that(has.indirect.evidence(network, 'A', 'B'), equals(FALSE))

  data <- read.table(textConnection('study treatment
s01 A
s01 B
s01 C
s01 D
s02 B
s02 C'), header=T)
  network <- mtc.network(data)
  expect_that(has.indirect.evidence(network, 'A', 'B'), equals(FALSE))

  data <- read.table(textConnection('study treatment
s01 A
s01 B
s01 C
s01 D
s02 B
s02 C
s03 A
s03 D'), header=T)
  network <- mtc.network(data)
  expect_that(has.indirect.evidence(network, 'A', 'B'), equals(FALSE)) # used to be TRUE pre-0.6
})

test_that("data.re is incorporated", {
  data <- read.table(textConnection('study treatment diff std.err
s01 A NA 0.5
s01 B 1.0 0.8
s01 C -1.0 0.8'), header=T)
  network <- mtc.network(data.re=data)
  expect_that(has.indirect.evidence(network, 'A', 'B'), equals(FALSE))

  data <- read.table(textConnection('study treatment diff std.err
s01 A NA 0.5
s01 B 1.0 0.8
s01 C -1.0 0.8
s02 A NA NA
s02 C 0.1 0.8
s03 B NA NA
s03 C -1.8 0.8'), header=T)
  network <- mtc.network(data.re=data)
  expect_that(has.indirect.evidence(network, 'A', 'B'), equals(TRUE))
})
