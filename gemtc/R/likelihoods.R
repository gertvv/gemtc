#' @include template.R

fixna <- function(x, v) {
  x[is.na(x)] <- v
  x
}

likelihood.code.binom <- list(
  read.template("gemtc.likelihood.binom.txt"),
  read.template("gemtc.likelihood.binom.power.txt"))

deviance.binom <- function(data, val, alpha=1) {
  r <- data$r
  n <- data$n
  rfit <- val
  2 * alpha * (fixna(r * log(r / rfit), 0) + fixna((n - r) * log((n - r) / (n - rfit)), 0))
}

fitted.values.parameter.binom <- function() { "rhat" }

likelihood.code.poisson <- list(
  read.template("gemtc.likelihood.poisson.txt"),
  read.template("gemtc.likelihood.poisson.power.txt"))

deviance.poisson <- function(data, val, alpha=1) {
  r <- data$r
  rfit <- val
  2 * alpha * ((rfit - r) + fixna(r * log(r / rfit), 0))
}

fitted.values.parameter.poisson <- function() { "theta" }

likelihood.code.normal <- list(
  read.template("gemtc.likelihood.normal.txt"),
  read.template("gemtc.likelihood.normal.power.txt"))

deviance.normal <- function(data, val, alpha=1) {
  alpha * (data$m - val)^2 / data$e^2
}

fitted.values.parameter.normal <- function() { "theta" }
