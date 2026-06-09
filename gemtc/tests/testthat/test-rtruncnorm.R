# The contents of this file have been adapted from version 1.0-9 of the {truncnorm} package, retrieved from:
#   https://github.com/olafmersmann/truncnorm/tree/28593c0b454bdbe860bc00e8e23690c12af89927
# The package is credited to
#   Olaf Mersmann, Heike Trautmann, Detlef Steuer and Bjorn Bornkamp
# and is released under the GNU General Public Licence v2 (or later).
# The package does not include a copy of that license, so it is not reproduced here.
# 
# The superficial changes that led to this particular file were performed by Miguel Lechon on behalf of
# Boehringer-Ingelheim Pharma GmbH & Co.KG. They do not deviate meaningfully from the original work, so no
# separate copyright is claimed.
#

##
## Don't segfault!
##

context("rtruncnorm reg-segfault")

expect_error(truncnorm__rtruncnorm(1, numeric(0), 1, 0, 1))
expect_error(truncnorm__rtruncnorm(1, 0, numeric(0), 0, 1))
expect_error(truncnorm__rtruncnorm(1, 0, 1, numeric(0), 1))
expect_error(truncnorm__rtruncnorm(1, 0, 1, 0, numeric(0)))


################################################################################
## Sanity checks on random number generators

context("rtruncnorm sanity checks")

check_r <- function(a, b, mean, sd, n=10000) {
  prefix <- sprintf("R: a=%f, b=%f, mean=%f, sd=%f", a, b, mean, sd)
  x <- truncnorm__rtruncnorm(n, a, b, mean, sd)
  e.x <- mean(x)

  ## FIXME: Really sample from open intervall?
  test_that(prefix, {
    expect_true(all(x > a))
    expect_true(all(x < b))
  })
}

## rtruncnorm == rnorm:
check_r(-Inf, Inf, 0, 1)

## 0 in (a, b):
check_r(-1, 1, 0, 1)
check_r(-1, 1, 1, 1)
check_r(-1, 1, 0, 2)

## 0 < (a, b):
check_r(1, 2, 0, 1)
check_r(1, 2, 1, 1)
check_r(1, 2, 0, 2)

## 0 > (a, b):
check_r(-2, -1, 0, 1)
check_r(-2, -1, 1, 1)
check_r(-2, -1, 0, 2)

## left truncation:
check_r(-2, Inf, 0, 1)
check_r(-2, Inf, 1, 1)
check_r(-2, Inf, 0, 2)
check_r( 0, Inf, 0, 1)
check_r( 0, Inf, 1, 1)
check_r( 0, Inf, 0, 2)
check_r( 2, Inf, 0, 1)
check_r( 2, Inf, 1, 1)
check_r( 2, Inf, 0, 2)

check_r(-0.2, Inf, 0, 1)
check_r(-0.2, Inf, 1, 1)
check_r(-0.2, Inf, 0, 2)
check_r( 0.0, Inf, 0, 1)
check_r( 0.0, Inf, 1, 1)
check_r( 0.0, Inf, 0, 2)
check_r( 0.2, Inf, 0, 1)
check_r( 0.2, Inf, 1, 1)
check_r( 0.2, Inf, 0, 2)

## Right truncation:
check_r(-Inf, -2, 0, 1)
check_r(-Inf, -2, 1, 1)
check_r(-Inf, -2, 0, 2)
check_r(-Inf,  0, 0, 1)
check_r(-Inf,  0, 1, 1)
check_r(-Inf,  0, 0, 2)
check_r(-Inf,  2, 0, 1)
check_r(-Inf,  2, 1, 1)
check_r(-Inf,  2, 0, 2)

check_r(-Inf, -0.2, 0, 1)
check_r(-Inf, -0.2, 1, 1)
check_r(-Inf, -0.2, 0, 2)
check_r(-Inf,  0.0, 0, 1)
check_r(-Inf,  0.0, 1, 1)
check_r(-Inf,  0.0, 0, 2)
check_r(-Inf,  0.2, 0, 1)
check_r(-Inf,  0.2, 1, 1)
check_r(-Inf,  0.2, 0, 2)

## Extreme examples:
check_r(-5, -4, 0, 1)

## Integer examples:
check_r(-5L, -4L, 0L, 1L)

