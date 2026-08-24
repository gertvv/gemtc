#' Draw a single sample from a truncated normal distribution
#'
#' Generates random samples from a normal distribution truncated to the interval
#' `[a, b]`. This is a pure R implementation using inverse transform sampling
#' and does not depend on compiled code or external packages.
#'
#' For each observation, a uniform random value is drawn between the normal CDF
#' values at the lower and upper truncation limits, and then transformed back
#' using the normal quantile function.' 
#'
#' NOTE: this method is not numerically stable at the tails of the distribution.
#' This is not expected to be a problem for how it is used within this package,
#' but beware of using it in a more general context.
rtruncnorm <- function(mean, sd, a, b) {
  stopifnot(
    length(a) == 1,
    length(b) == 1,
    length(mean) == 1,
    length(sd) == 1,
    is.numeric(a),
    is.numeric(b),
    is.numeric(mean),
    is.numeric(sd),
    sd > 0,
    a <= b
  )

  if (a == b) {
    a
  } else {
    # calculate the CDF values at the truncation bounds
    p_lo <- pnorm(a, mean, sd)
    p_up <- pnorm(b, mean, sd)
    # draw uniformly from the probability interval
    p <- runif(n = 1, min = p_lo, max = p_up)
    # transform from probability to value
    mean + sd * qnorm(p)
  }
}
