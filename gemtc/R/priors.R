# Returns a matrix with one row for each of the given pairs,
# and columns 'mean' and 'sd' describing their relative effect
rel.mle.ab <- function(data, model, pairs) {
  matrix(sapply(1:nrow(pairs), function(i) {
    sel1 <- data[['treatment']] == pairs[['t1']][i]
    sel2 <- data[['treatment']] == pairs[['t2']][i]
    columns <- ll.call("required.columns.ab", model)
    ll.call("mtc.rel.mle", model, as.matrix(data[sel1 | sel2, columns, drop=FALSE]))
  }), ncol=2, byrow=TRUE, dimnames=list(NULL, c('mean', 'sd')))
}

# Returns a matrix with one row for each of the given pairs,
# and columns 'mean' and 'sd' describing their relative effect
rel.mle.re <- function(data, pairs) {
  # the mean vector
  mu <- data[['diff']]
  mu[1] <- 0.0 # the baseline relative to itself

  # construct the covariance matrix
  se <- data[['std.err']]
  sigma <- matrix(se[1]^2, nrow=length(se), ncol=length(se))
  diag(sigma) <- se^2
  sigma[1,] <- 0
  sigma[,1] <- 0

  # construct the permutation matrix
  b <- sapply(1:nrow(pairs), function(i) {
    x <- rep(0, length(se))
    x[data[['treatment']] == pairs[['t1']][i]] <- -1
    x[data[['treatment']] == pairs[['t2']][i]] <- 1
    x
  })
  b <- matrix(b, nrow=length(se))

  mu <- t(b) %*% mu
  sigma <- t(b) %*% sigma %*% b
  rval <- cbind(mu, sqrt(diag(sigma)))
  colnames(rval) <- c('mean', 'sd')
  rval
}

# Guess the measurement scale based on differences observed in the data set
guess.scale <- function(model) {
  data.ab <- model[['network']][['data.ab']]
  max.ab <- 0
  if (!is.null(data.ab)) {
    max.ab <- max(sapply(levels(data.ab[['study']]), function(study) {
      pairs <- mtc.treatment.pairs(mtc.study.design(model[['network']], study))
      max(abs(rel.mle.ab(data.ab[data.ab[['study']] == study, , drop=TRUE], model, pairs)[,'mean']))
    }))
  }
  data.re <- model[['network']][['data.re']]
  max.re <- 0
  if (!is.null(data.re)) {
    max.re <- max(sapply(levels(data.re[['study']]), function(study) {
      pairs <- mtc.treatment.pairs(mtc.study.design(model[['network']], study))
      max(abs(rel.mle.re(data.re[data.re[['study']] == study, , drop=TRUE], pairs)[,'mean']))
    }))
  }

  max(max.ab, max.re)
}
