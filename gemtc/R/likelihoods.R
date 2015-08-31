fixna <- function(x, v) {
  x[is.na(x)] <- v
  x
}

deviance.code.binom <-
"rhat[i, k] <- p[i, k] * n[i, k]
dev[i, k] <- 2 * (r[i, k] * (log(r[i, k]) - log(rhat[i, k])) +
                  (n[i, k]-r[i, k]) * (log(n[i, k] - r[i, k]) - log(n[i, k] - rhat[i, k])))"

deviance.binom <- function(data, val) {
  r <- data$r
  n <- data$n
  rfit <- val
  2 * (fixna(r * log(r / rfit), 0) + fixna((n - r) * log((n - r) / (n - rfit)), 0))
}

fitted.values.parameter.binom <- function() { "rhat" }

deviance.code.poisson <-
"dev[i, k] <- 2 * ((theta[i, k] - r[i, k]) + r[i, k] * log(r[i, k]/theta[i, k]))"

deviance.poisson <- function(data, val) {
  r <- data$r
  rfit <- val
  2 * ((rfit - r) + fixna(r * log(r / rfit), 0))
}

fitted.values.parameter.poisson <- function() { "theta" }

deviance.code.normal <-
"dev[i, k] <- pow(m[i, k] - theta[i, k], 2) * prec[i, k]"

deviance.normal <- function(data, val) {
  (data$m - val)^2 / data$e^2
}

fitted.values.parameter.normal <- function() { "theta" }
