context("Read GeMTC XML files")

# Test a dichotomous data file
test_that("read.mtc.network('luades-smoking.gemtc') has expected result", {
	expected <- dget(system.file("tests/luades-smoking.txt", package="gemtc"))
	file <- system.file("extdata/luades-smoking.gemtc", package="gemtc")
	expect_that(read.mtc.network(file), equals(expected))
})

# Test a continuous data file
test_that("read.mtc.network('parkinson.gemtc') has expected result", {
	expected <- dget(system.file("tests/parkinson.txt", package="gemtc"))
	file <- system.file("extdata/parkinson.gemtc", package="gemtc")
	expect_that(read.mtc.network(file), equals(expected))
})
