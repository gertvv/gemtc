context("ll.binom.log")

test_that("mtc.arm.mle (default correction)", {
  expect_that(mtc.arm.mle.binom.log(c('responders'=0, 'sampleSize'=25)),
              equals(c('mean'=log(0.5/26), 'sd'=sqrt(1/0.5 - 1/26))))

  expect_that(mtc.arm.mle.binom.log(c('responders'=12, 'sampleSize'=25)),
              equals(c('mean'=log(12.5/26), 'sd'=sqrt(1/12.5 - 1/26))))

  expect_that(mtc.arm.mle.binom.log(c('responders'=25, 'sampleSize'=25)),
              equals(c('mean'=log(25.5/26), 'sd'=sqrt(1/25.5 - 1/26))))
})

test_that("mtc.arm.mle (no correction)", {
  expect_equal(mtc.arm.mle.binom.log(c('responders'=12, 'sampleSize'=25), k=0),
               c('mean'=log(12/25), 'sd'=sqrt(1/12 - 1/25)))
})

test_that("mtc.arm.mle (other correction)", {
  expect_equal(mtc.arm.mle.binom.log(c('responders'=0, 'sampleSize'=25), k=0.05),
               c('mean'=log(0.05/25.1), 'sd'=sqrt(1/0.05 - 1/25.1)))
})

test_that("mtc.rel.mle (forced default correction)", {
  expect_that(mtc.rel.mle.binom.log(rbind(c('responders'=0, 'sampleSize'=25), c('responders'=3, 'sampleSize'=25))),
              equals(c('mean'=log(7), 'sd'=sqrt(1/3.5 + 1/0.5 - 2/26))))

  expect_that(mtc.rel.mle.binom.log(rbind(c('responders'=0, 'sampleSize'=25), c('responders'=0, 'sampleSize'=25))),
              equals(c('mean'=0, 'sd'=sqrt(2/0.5 - 2/26))))
})

test_that("mtc.rel.mle (as-needed default correction)", {
  expect_equal(mtc.rel.mle.binom.log(rbind(c('responders'=1, 'sampleSize'=25), c('responders'=3, 'sampleSize'=24)), correction.force=FALSE),
               c('mean'=log((3/24)/(1/25)), 'sd'=sqrt(1/3 - 1/24 + 1/1 - 1/25)))

  expect_equal(mtc.rel.mle.binom.log(rbind(c('responders'=0, 'sampleSize'=25), c('responders'=3, 'sampleSize'=24)), correction.force=FALSE),
               c('mean'=log((3.5/25)/(0.5/26)), 'sd'=sqrt(1/3.5 - 1/25 + 1/0.5 - 1/26)))

  expect_equal(mtc.rel.mle.binom.log(rbind(c('responders'=1, 'sampleSize'=25), c('responders'=24, 'sampleSize'=24)), correction.force=FALSE),
               c('mean'=log((24.5/25)/(1.5/26)), 'sd'=sqrt(1/24.5 - 1/25 + 1/1.5 - 1/26)))
})

test_that("mtc.rel.mle (alternative magnitude correction)", {
  expect_equal(mtc.rel.mle.binom.log(rbind(c('responders'=0, 'sampleSize'=25), c('responders'=3, 'sampleSize'=25)), correction.magnitude=0.1),
               c('mean'=log((3.05/25.1)/(0.05/25.1)), 'sd'=sqrt(1/3.05 - 1/25.1 + 1/0.05 - 1/25.1)))
})

test_that("mtc.rel.mle (reciprocal correction)", {
  # no correction
  expect_equal(mtc.rel.mle.binom.log(rbind(c('responders'=1, 'sampleSize'=25), c('responders'=3, 'sampleSize'=24)), correction.type="reciprocal", correction.force=FALSE),
               c('mean'=log((3/24)/(1/25)), 'sd'=sqrt(1/3 - 1/24 + 1/1 - 1/25)))

  # 1:2 group ratio (R = 2), correction for the control is R/(R+1) = 2/3, for the treatment 1/(R+1) = 1/3
  expect_equal(mtc.rel.mle.binom.log(rbind(c('responders'=0, 'sampleSize'=50), c('responders'=3, 'sampleSize'=25)), correction.type="reciprocal"),
               c('mean'=log(((3+1/3)/(25+2/3))/((0+2/3)/(50+4/3))), 'sd'=sqrt(1/(3+1/3) + 1/(0+2/3) - 1/(25+2/3) - 1/(50+4/3))))

  # 1:4 group ratio (R = 4), correction for the control is R/(R+1) = 4/5, for the treatment 1/(R+1) = 1/5
  expect_equal(mtc.rel.mle.binom.log(rbind(c('responders'=0, 'sampleSize'=100), c('responders'=3, 'sampleSize'=25)), correction.type="reciprocal"),
               c('mean'=log((3.2/25.4)/(0.8/101.6)), 'sd'=sqrt(1/3.2 + 1/0.8 - 1/25.4 - 1/101.6)))

  # 1:4 group ratio (R = 4), correction for the control is 0.1 R/(R+1) = 0.4/5, for the treatment 0.1/(R+1) = 0.1/5
  expect_equal(mtc.rel.mle.binom.log(rbind(c('responders'=0, 'sampleSize'=100), c('responders'=3, 'sampleSize'=25)), correction.type="reciprocal", correction.magnitude=0.1),
               c('mean'=log((3.02/25.04)/(0.08/100.16)), 'sd'=sqrt(1/3.02 + 1/0.08 - 1/25.04 - 1/100.16)))
})
