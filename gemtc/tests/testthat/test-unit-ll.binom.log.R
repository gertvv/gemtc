context("ll.binom.log")

test_that("mtc.arm.mle returns correct values", {
  expect_that(mtc.arm.mle.binom.log(c('responders'=0, 'sampleSize'=25)),
              equals(c('mean'=log(0.5/26), 'sd'=sqrt(1/0.5 - 1/26))))

  expect_that(mtc.arm.mle.binom.log(c('responders'=12, 'sampleSize'=25)),
              equals(c('mean'=log(12.5/26), 'sd'=sqrt(1/12.5 - 1/26))))

  expect_that(mtc.arm.mle.binom.log(c('responders'=25, 'sampleSize'=25)),
              equals(c('mean'=log(25.5/26), 'sd'=sqrt(1/25.5 - 1/26))))
})

test_that("mtc.rel.mle returns correct values", {
  expect_that(mtc.rel.mle.binom.log(rbind(c('responders'=0, 'sampleSize'=25), c('responders'=3, 'sampleSize'=25))),
              equals(c('mean'=log(7), 'sd'=sqrt(1/3.5 + 1/0.5 - 2/26))))

  expect_that(mtc.rel.mle.binom.log(rbind(c('responders'=0, 'sampleSize'=25), c('responders'=0, 'sampleSize'=25))),
              equals(c('mean'=0, 'sd'=sqrt(2/0.5 - 2/26))))
})
