context("ll.poisson.log")

test_that("mtc.arm.mle (default correction)", {
  expect_equal(mtc.arm.mle.poisson.log(c('responders'=0, 'exposure'=25)),
              c('mean'=log(0.5/25), 'sd'=sqrt(1/25)))

  expect_equal(mtc.arm.mle.poisson.log(c('responders'=12, 'exposure'=25)),
              c('mean'=log(12.5/25), 'sd'=sqrt(1/25)))

  expect_equal(mtc.arm.mle.poisson.log(c('responders'=25, 'exposure'=25)),
              c('mean'=log(25.5/25), 'sd'=sqrt(1/25)))
})

test_that("mtc.arm.mle (no correction)", {
  expect_equal(mtc.arm.mle.poisson.log(c('responders'=12, 'exposure'=25), k=0),
               c('mean'=log(12/25), 'sd'=sqrt(1/25)))
})

test_that("mtc.arm.mle (other correction)", {
  expect_equal(mtc.arm.mle.poisson.log(c('responders'=0, 'exposure'=25), k=0.05),
               c('mean'=log(0.05/25), 'sd'=sqrt(1/25)))
})

test_that("mtc.rel.mle (forced default correction)", {
  expect_that(mtc.rel.mle.poisson.log(rbind(c('responders'=0, 'exposure'=25), c('responders'=3, 'exposure'=25))),
              equals(c('mean'=log(7), 'sd'=sqrt(2/25))))

  expect_that(mtc.rel.mle.poisson.log(rbind(c('responders'=0, 'exposure'=25), c('responders'=0, 'exposure'=25))),
              equals(c('mean'=0, 'sd'=sqrt(2/25))))
})

test_that("mtc.rel.mle (as-needed default correction)", {
  expect_equal(mtc.rel.mle.poisson.log(rbind(c('responders'=1, 'exposure'=25), c('responders'=3, 'exposure'=24)), correction.force=FALSE),
               c('mean'=log((3/24)/(1/25)), 'sd'=sqrt(1/24 + 1/25)))

  expect_equal(mtc.rel.mle.poisson.log(rbind(c('responders'=0, 'exposure'=25), c('responders'=3, 'exposure'=24)), correction.force=FALSE),
               c('mean'=log((3.5/24)/(0.5/25)), 'sd'=sqrt(1/24 + 1/25)))

  expect_equal(mtc.rel.mle.poisson.log(rbind(c('responders'=1, 'exposure'=25), c('responders'=24, 'exposure'=24)), correction.force=FALSE),
               c('mean'=log((24/24)/(1/25)), 'sd'=sqrt(1/24 + 1/25)))
})

test_that("mtc.rel.mle (alternative magnitude correction)", {
  expect_equal(mtc.rel.mle.poisson.log(rbind(c('responders'=0, 'exposure'=25), c('responders'=3, 'exposure'=25)), correction.magnitude=0.1),
               c('mean'=log((3.05/25)/(0.05/25)), 'sd'=sqrt(2/25)))
})

test_that("mtc.rel.mle (reciprocal correction)", {
  # no correction
  expect_equal(mtc.rel.mle.poisson.log(rbind(c('responders'=1, 'exposure'=25), c('responders'=3, 'exposure'=24)), correction.type="reciprocal", correction.force=FALSE),
               c('mean'=log((3/24)/(1/25)), 'sd'=sqrt(1/24 + 1/25)))

  # 1:2 group ratio (R = 2), correction for the control is R/(R+1) = 2/3, for the treatment 1/(R+1) = 1/3
  expect_equal(mtc.rel.mle.poisson.log(rbind(c('responders'=0, 'exposure'=50), c('responders'=3, 'exposure'=25)), correction.type="reciprocal"),
               c('mean'=log(((3+1/3)/25)/((0+2/3)/50)), 'sd'=sqrt(1/25 + 1/50)))

  # 1:4 group ratio (R = 4), correction for the control is R/(R+1) = 4/5, for the treatment 1/(R+1) = 1/5
  expect_equal(mtc.rel.mle.poisson.log(rbind(c('responders'=0, 'exposure'=100), c('responders'=3, 'exposure'=25)), correction.type="reciprocal"),
               c('mean'=log((3.2/25)/(0.8/100)), 'sd'=sqrt(1/25 + 1/100)))

  # 1:4 group ratio (R = 4), correction for the control is 0.1 R/(R+1) = 0.4/5, for the treatment 0.1/(R+1) = 0.1/5
  expect_equal(mtc.rel.mle.poisson.log(rbind(c('responders'=0, 'exposure'=100), c('responders'=3, 'exposure'=25)), correction.type="reciprocal", correction.magnitude=0.1),
               c('mean'=log((3.02/25)/(0.08/100)), 'sd'=sqrt(1/25 + 1/100)))

  # 1:4 group ratio (R = 4), correction for the control is 0.1 R/(R+1) = 0.4/5, for the treatment 0.1/(R+1) = 0.1/5
  expect_equal(mtc.rel.mle.poisson.log(rbind(c('responders'=0, 'exposure'=100), c('responders'=0, 'exposure'=25)), correction.type="reciprocal", correction.magnitude=0.1),
               c('mean'=0, 'sd'=sqrt(1/25 + 1/100)))
})
