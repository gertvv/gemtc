context("mtc.network(data.re)")

data.re <- read.table(textConnection("
study  treatment  diff  std.err
s01    A          NA    NA
s01    C          -0.31 0.67
s02    A          NA    NA
s02    B          -1.7  0.38
s03    C          NA    NA
s03    D          -0.35 0.44
s04    C          NA    NA
s04    D          0.55  0.55
s05    D          NA    NA
s05    E          -0.3  0.27 
s06    D          NA    NA
s06    E          -0.3  0.32
s07    A          NA    0.50
s07    B          -2.3  0.72
s07    D          -0.9  0.69"), header=T)
 

test_that("either data or data.re must be specified", {
	expect_error(mtc.network())
})

test_that("data can be unspecified if data.re is given", {
	mtc.network(data.re=data.re)
})

test_that("data.re is properly stored", {
	network <- mtc.network(data.re=data.re)
	expect_that(network$data.re$study, equals(data.re$study))
	expect_that(network$data.re$treatment, equals(data.re$treatment))
	expect_that(network$data.re$diff, equals(data.re$diff))
	expect_that(network$data.re$std.err, equals(data.re$std.err))
})

data.arm <- read.table(textConnection("
	study  treatment
	s08    A
	s08    C
	s09    A
	s09    D
	s09    F"), header=TRUE)

test_that("treatments for data and data.re are merged", {
	network <- mtc.network(data=data.arm, data.re=data.re)
	expect_that(levels(network$treatments$id), equals(c("A", "B", "C", "D", "E", "F")))
	expect_that(as.character(network$data$treatment), equals(as.character(data.arm$treatment)))
	expect_that(as.character(network$data.re$treatment), equals(as.character(data.re$treatment)))
})
