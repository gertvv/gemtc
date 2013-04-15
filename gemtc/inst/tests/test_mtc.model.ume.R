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
