study.seq.ab <- function(data) {
  data[['studies.a']]
}

study.seq.re <- function(data) {
  c(data[['studies.r2']], data[['studies.rm']])
}

deviance.monitors.residuals.ab <- function(model) {
  do.call(c, lapply(study.seq.ab(model[['data']]), function(i) { paste0("dev[", i, ",", 1:model[['data']][['na']][i], "]") }))
}

deviance.monitors.residuals.re <- function(model) {
  do.call(c, lapply(study.seq.re(model[['data']]), function(i) { paste0("dev[", i, ",", 1, "]") }))
}

deviance.monitors.fitted.ab <- function(model) {
  fpname <- ll.call("fitted.values.parameter", model)
  do.call(c, lapply(study.seq.ab(model[['data']]), function(i) { paste0(fpname, "[", i, ",", 1:model[['data']][['na']][i], "]") }))
}

alpha.ab <- function(model) {
  if (!is.na(model[['powerAdjust']]) && !is.null(model[['powerAdjust']])) {
    do.call(c, lapply(study.seq.ab(model[['data']]), function(i) { rep(model[['data']][['alpha']][i], model[['data']][['na']][i]) }))
  } else {
    1
  }
}

deviance.monitors.fitted.re <- function(model) {
  do.call(c, lapply(study.seq.re(model[['data']]), function(i) { paste0("delta[", i, ",", 2:model[['data']][['na']][i], "]") }))
}

deviance.monitors <- function(model) {
  c(deviance.monitors.residuals.ab(model),
    deviance.monitors.residuals.re(model),
    deviance.monitors.fitted.ab(model),
    deviance.monitors.fitted.re(model))
}

devfit.ab <- function(model, fit.ab) {
  studies <- study.seq.ab(model[['data']])
  if (length(studies) > 0) {
    cols <- names(ll.call("required.columns.ab", model))
    data <- model[['data']][cols]
    data <- lapply(data, function(el) {
      x <- as.vector(t(el[studies, , drop=FALSE]))
      x[!is.na(x)]
    })
    ll.call("deviance", model, data, fit.ab, alpha=alpha.ab(model))
  } else {
    c()
  }
}

devfit.re <- function(model, mfit) {
  data <- model[['data']]
  s <- study.seq.re(data)
  if (length(s) > 0) {
    sapply(s, function(i) {
      na <- data[['na']][i]
      prev <- s[1:which(s == i)]
      start <- sum(data[['na']][prev] - 1) - na + 2
      ifit <- mfit[start:(start + na - 2)]

      cov <- if (!is.na(data[['e']][i, 1])) data[['e']][i, 1]^2 else 0
      Sigma <- matrix(cov, nrow=(na-1), ncol=(na-1))
      diag(Sigma) <- data[['e']][i, 2:na]^2
      Omega <- solve(Sigma)
      m <- data[['m']][i, 2:na]

      mdiff <- m - ifit

      alpha <-
        if (!is.na(model[['powerAdjust']]) && !is.null(model[['powerAdjust']])) data[['alpha']][i]
        else 1

      alpha * t(mdiff) %*% Omega %*% mdiff
    })
  } else {
    c()
  }
}

computeDeviance <- function(model, stats) {
  arms <- arm.index.matrix(model[['network']])
  studies.ab <- study.seq.ab(model[['data']])
  studies.re <- study.seq.re(model[['data']])

  dp <- sum(!is.na(arms)) - length(studies.re)

  shape.ab <- function(x) {
    if (length(x) > 0) {
      tpl <- arms[studies.ab,]
      x <- unname(x)
      y <- t(tpl)
      y[!is.na(y)] <- x
      t(y)
    } else {
      c()
    }
  }

  dev.ab <- stats[deviance.monitors.residuals.ab(model)]
  dev.re <- stats[deviance.monitors.residuals.re(model)]

  Dbar <- sum(c(dev.ab, dev.re))

  fit.ab <- devfit.ab(model, stats[deviance.monitors.fitted.ab(model)])
  fit.re <- devfit.re(model, stats[deviance.monitors.fitted.re(model)])

  pD <- Dbar - sum(c(fit.ab, fit.re))

  list(Dbar=Dbar, pD=pD, DIC=Dbar+pD, "data points"=dp,
       dev.ab=shape.ab(dev.ab), dev.re=unname(dev.re),
       fit.ab=shape.ab(fit.ab), fit.re=unname(fit.re))
}
