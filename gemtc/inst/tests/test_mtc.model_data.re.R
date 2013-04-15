context("mtc.model(data.re)")

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

test_that("study counts are defined even if there is no data of said type", {
	data <- read.table(textConnection("
		study   treatment  mean std.dev sampleSize
		s01     A          2.0  0.5     20
        s01     B          1.5  0.5     20"), header=T)
	network <- mtc.network(data=data)
	model <- mtc.model(network)
	expect_that(model$data$ns.r2, equals(0))
	expect_that(model$data$ns.rm, equals(0))
})
