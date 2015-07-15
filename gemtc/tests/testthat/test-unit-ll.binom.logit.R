context("ll.binom.logit")

test_that("mtc.arm.mle (default correction)", {
  expect_equal(mtc.arm.mle.binom.logit(c('responders'=0, 'sampleSize'=25)),
               c('mean'=log(0.5/25.5), 'sd'=sqrt(1/0.5 + 1/25.5)))

  expect_equal(mtc.arm.mle.binom.logit(c('responders'=12, 'sampleSize'=25)),
               c('mean'=log(12.5/13.5), 'sd'=sqrt(1/12.5 + 1/13.5)))

  expect_equal(mtc.arm.mle.binom.logit(c('responders'=25, 'sampleSize'=25)),
               c('mean'=log(25.5/0.5), 'sd'=sqrt(1/25.5 + 1/0.5)))
})

test_that("mtc.arm.mle (no correction)", {
  expect_equal(mtc.arm.mle.binom.logit(c('responders'=12, 'sampleSize'=25), k=0),
               c('mean'=log(12/13), 'sd'=sqrt(1/12 + 1/13)))
})

test_that("mtc.arm.mle (other correction)", {
  expect_equal(mtc.arm.mle.binom.logit(c('responders'=0, 'sampleSize'=25), k=0.05),
               c('mean'=log(0.05/25.05), 'sd'=sqrt(1/0.05 + 1/25.05)))

  expect_equal(mtc.arm.mle.binom.logit(c('responders'=12, 'sampleSize'=25), k=0.05),
               c('mean'=log(12.05/13.05), 'sd'=sqrt(1/12.05 + 1/13.05)))

  expect_equal(mtc.arm.mle.binom.logit(c('responders'=25, 'sampleSize'=25), k=0.05),
               c('mean'=log(25.05/0.05), 'sd'=sqrt(1/25.05 + 1/0.05)))
})

test_that("mtc.rel.mle (forced default correction)", {
  expect_equal(mtc.rel.mle.binom.logit(rbind(c('responders'=0, 'sampleSize'=25), c('responders'=3, 'sampleSize'=25))),
               c('mean'=log((3.5/22.5)/(0.5/25.5)), 'sd'=sqrt(1/3.5 + 1/0.5 + 1/22.5 + 1/25.5)))

  expect_equal(mtc.rel.mle.binom.logit(rbind(c('responders'=0, 'sampleSize'=25), c('responders'=0, 'sampleSize'=25))),
               c('mean'=0, 'sd'=sqrt(2/0.5 + 2/25.5)))
})

test_that("mtc.rel.mle (as-needed default correction)", {
  expect_equal(mtc.rel.mle.binom.logit(rbind(c('responders'=1, 'sampleSize'=25), c('responders'=3, 'sampleSize'=24)), correction.force=FALSE),
               c('mean'=log((3/21)/(1/24)), 'sd'=sqrt(1/3 + 1/1 + 1/21 + 1/24)))

  expect_equal(mtc.rel.mle.binom.logit(rbind(c('responders'=0, 'sampleSize'=25), c('responders'=3, 'sampleSize'=24)), correction.force=FALSE),
               c('mean'=log((3.5/21.5)/(0.5/25.5)), 'sd'=sqrt(1/3.5 + 1/0.5 + 1/21.5 + 1/25.5)))

  expect_equal(mtc.rel.mle.binom.logit(rbind(c('responders'=1, 'sampleSize'=25), c('responders'=24, 'sampleSize'=24)), correction.force=FALSE),
               c('mean'=log((24.5/0.5)/(1.5/24.5)), 'sd'=sqrt(1/24.5 + 1/1.5 + 1/0.5 + 1/24.5)))
})

test_that("mtc.rel.mle (alternative magnitude correction)", {
  expect_equal(mtc.rel.mle.binom.logit(rbind(c('responders'=0, 'sampleSize'=25), c('responders'=3, 'sampleSize'=25)), correction.magnitude=0.1),
               c('mean'=log((3.05/22.05)/(0.05/25.05)), 'sd'=sqrt(1/3.05 + 1/0.05 + 1/22.05 + 1/25.05)))
})

test_that("mtc.rel.mle (reciprocal correction)", {
  # no correction
  expect_equal(mtc.rel.mle.binom.logit(rbind(c('responders'=1, 'sampleSize'=25), c('responders'=3, 'sampleSize'=24)), correction.type="reciprocal", correction.force=FALSE),
               c('mean'=log((3/21)/(1/24)), 'sd'=sqrt(1/3 + 1/1 + 1/21 + 1/24)))

  # 1:2 group ratio (R = 2), correction for the control is R/(R+1) = 2/3, for the treatment 1/(R+1) = 1/3
  expect_equal(mtc.rel.mle.binom.logit(rbind(c('responders'=0, 'sampleSize'=50), c('responders'=3, 'sampleSize'=25)), correction.type="reciprocal"),
               c('mean'=log(((3+1/3)/(22+1/3))/((0+2/3)/(50+2/3))), 'sd'=sqrt(1/(3+1/3) + 1/(0+2/3) + 1/(22+1/3) + 1/(50+2/3))))

  # 1:4 group ratio (R = 4), correction for the control is R/(R+1) = 4/5, for the treatment 1/(R+1) = 1/5
  expect_equal(mtc.rel.mle.binom.logit(rbind(c('responders'=0, 'sampleSize'=100), c('responders'=3, 'sampleSize'=25)), correction.type="reciprocal"),
               c('mean'=log((3.2/22.2)/(0.8/100.8)), 'sd'=sqrt(1/3.2 + 1/0.8 + 1/22.2 + 1/100.8)))

  # 1:4 group ratio (R = 4), correction for the control is 0.1 R/(R+1) = 0.4/5, for the treatment 0.1/(R+1) = 0.1/5
  expect_equal(mtc.rel.mle.binom.logit(rbind(c('responders'=0, 'sampleSize'=100), c('responders'=3, 'sampleSize'=25)), correction.type="reciprocal", correction.magnitude=0.1),
               c('mean'=log((3.02/22.02)/(0.08/100.08)), 'sd'=sqrt(1/3.02 + 1/0.08 + 1/22.02 + 1/100.08)))
})
