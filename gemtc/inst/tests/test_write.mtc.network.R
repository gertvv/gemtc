context("Write GeMTC XML files")

# Test a dichotomous data file
test_that("write.mtc.network('luades-smoking.gemtc') has expected result", {
	network <- dget(system.file("tests/luades-smoking.txt", package="gemtc"))
	file <- tempfile()
	write.mtc.network(network, file)
	expect_that(read.mtc.network(file), equals(network))
	unlink(file)
})

# Test a continuous data file
test_that("read.mtc.network('parkinson.gemtc') has expected result", {
	network <- dget(system.file("tests/parkinson.txt", package="gemtc"))
	file <- tempfile()
	write.mtc.network(network, file)
	expect_that(read.mtc.network(file), equals(network))
	unlink(file)
})
