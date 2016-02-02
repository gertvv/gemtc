as.character.mtc.hy.prior <- function(x, ...) {
  type <- x[['type']]
  distr <- x[['distr']]
  args <- x[['args']]
  
  expr <- if (distr == "dhnorm") {
    paste0("dnorm", "(", paste(args, collapse=", "), ") T(0,)")
  } else {
    paste0(distr, "(", paste(args, collapse=", "), ")")
  }

  if (type == "std.dev") {
    paste0("sd.d ~ ", expr, "\ntau.d <- pow(sd.d, -2)")
  } else if (type == "var") {
    paste0("var.d ~ ", expr, "\nsd.d <- sqrt(var.d)\ntau.d <- 1 / var.d")
  } else {
    paste0("tau.d ~ ", expr, "\nsd.d <- sqrt(1 / tau.d)")
  }
}

mtc.hy.prior <- function(type, distr, ...) {
  stopifnot(class(type) == "character")
  stopifnot(length(type) == 1)
  stopifnot(type %in% c('std.dev', 'var', 'prec'))

  obj <- list(type=type, distr=distr, args=list(...))
  class(obj) <- "mtc.hy.prior"
  obj
}

hy.lor.outcomes <- c('mortality', 'semi-objective', 'subjective')
hy.lor.comparisons <- c('pharma-control', 'pharma-pharma', 'non-pharma')

hy.lor.mu <- matrix(
  c(-4.06, -3.02, -2.13, -4.27, -3.23, -2.34, -3.93, -2.89, -2.01),
  ncol=3, nrow=3,
  dimnames=list(hy.lor.outcomes, hy.lor.comparisons))

hy.lor.sigma <- matrix(
  c(1.45, 1.85, 1.58, 1.48, 1.88, 1.62, 1.51, 1.91, 1.64),
  ncol=3, nrow=3,
  dimnames=list(hy.lor.outcomes, hy.lor.comparisons))

mtc.hy.empirical.lor <- function(outcome.type, comparison.type) {
  stopifnot(outcome.type %in% hy.lor.outcomes)
  stopifnot(comparison.type %in% hy.lor.comparisons)
  mtc.hy.prior("var", "dlnorm",
    hy.lor.mu[outcome.type, comparison.type],
    signif(hy.lor.sigma[outcome.type, comparison.type]^-2, digits=3))
}
