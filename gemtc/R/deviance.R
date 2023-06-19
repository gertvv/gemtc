#'@include arrayize.R

study.seq.ab <- function(data) {
  data[['studies.a']]
}

study.seq.re <- function(data) {
  c(data[['studies.r2']], data[['studies.rm']])
}

deviance_monitors_residuals_ab <- function(model) {
  do.call(c, lapply(study.seq.ab(model[['data']]), function(i) { paste0("dev[", i, ",", 1:model[['data']][['na']][i], "]") }))
}

deviance_monitors_residuals_re <- function(model) {
  do.call(c, lapply(study.seq.re(model[['data']]), function(i) { paste0("dev[", i, ",", 1, "]") }))
}

deviance_monitors_fitted_ab <- function(model) {
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

deviance_monitors_fitted_re <- function(model) {
  do.call(c, lapply(study.seq.re(model[['data']]), function(i) { paste0("delta[", i, ",", 2:model[['data']][['na']][i], "]") }))
}

deviance_monitors <- function(model) {
  c(deviance_monitors_residuals_ab(model),
    deviance_monitors_residuals_re(model),
    deviance_monitors_fitted_ab(model),
    deviance_monitors_fitted_re(model))
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
    ll.call("deviance_fn", model, data, fit.ab, alpha=alpha.ab(model))
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

  nd <- apply(!is.na(arms), 1, sum)
  nd.ab <- nd[studies.ab]
  nd.re <- nd[studies.re] - 1
  dp <- sum(nd.ab) + sum(nd.re)

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

  name.ab <- function(x) {
    if (length(x) > 0) {
      names(x) <- rownames(arms)[studies.ab]
      x
    } else {
      c()
    }
  }

  name.re <- function(x) {
    if (length(x) > 0) {
      names(x) <- rownames(arms)[studies.re]
      x
    } else {
      c()
    }
  }

  dev.ab <- stats[deviance_monitors_residuals_ab(model)]
  if (length(dev.ab) == 0) {
    dev.ab <- NULL
  }
  dev.re <- stats[deviance_monitors_residuals_re(model)]
  if (length(dev.re) == 0) {
    dev.re <- NULL
  }

  Dbar <- sum(c(dev.ab, dev.re))

  fit.ab <- devfit.ab(model, stats[deviance_monitors_fitted_ab(model)])
  if (length(fit.ab) == 0) {
    fit.ab <- NULL
  }
  fit.re <- devfit.re(model, stats[deviance_monitors_fitted_re(model)])
  if (length(fit.re) == 0) {
    fit.re <- NULL
  }

  lev.ab <- dev.ab - fit.ab
  if (length(lev.ab) == 0) {
    lev.ab <- NULL
  }
  lev.re <- dev.re - fit.re
  if (length(lev.re) == 0) {
    lev.re <- NULL
  }

  pD <- Dbar - sum(c(fit.ab, fit.re))

  fitted <- c(stats[deviance_monitors_fitted_ab(model)], stats[deviance_monitors_fitted_re(model)])

  info <- list(Dbar=Dbar, pD=pD, DIC=Dbar+pD, "data points"=dp,
               dev.ab=shape.ab(dev.ab), dev.re=name.re(dev.re),
               fit.ab=shape.ab(fit.ab), fit.re=name.re(fit.re),
               lev.ab=shape.ab(lev.ab), lev.re=name.re(lev.re),
               nd.ab=name.ab(nd.ab), nd.re=name.re(nd.re),
               fitted=arrayize(fitted))
  class(info) <- "mtc.deviance"
  info
}

mtc.deviance <- function(result) {
  stopifnot(class(result) == "mtc.result")
  result[['deviance']]
}

mtc.devplot <- function(x, ...) {
  stopifnot(class(x) == "mtc.deviance")

  if (is.null(x[['dev.re']])) {
    tpl <- x[['dev.ab']]
    study <- matrix(rep(1:nrow(tpl), times=ncol(tpl)), nrow=nrow(tpl), ncol=ncol(tpl))
    study <- t(study)[t(!is.na(tpl))]
    devbar <- t(x[['dev.ab']])[t(!is.na(tpl))]
    title <- "Per-arm residual deviance"
    xlab <- "Arm"
  } else {
    nd <- c(x[['nd.ab']], x[['nd.re']])
    devbar <- c(apply(x[['dev.ab']], 1, sum, na.rm=TRUE), x[['dev.re']]) / nd
    study <- 1:length(devbar)
    title <- "Per-study mean per-datapoint residual deviance"
    xlab <- "Study"
  }

  plot(devbar, ylim=c(0,max(devbar, na.rm=TRUE)),
      ylab="Residual deviance", xlab=xlab,
      main=title, pch=c(1, 22)[(study%%2)+1],
      ...)
  for (i in 1:length(devbar)) {
    lines(c(i, i), c(0, devbar[i]))
  }
}

mtc.levplot <- function(x, ...) {
  stopifnot(class(x) == "mtc.deviance")

  fit.ab <- apply(x[['fit.ab']], 1, sum, na.rm=TRUE)
  dev.ab <- apply(x[['dev.ab']], 1, sum, na.rm=TRUE)
  lev.ab <- dev.ab - fit.ab
  fit.re <- x[['fit.re']]
  dev.re <- x[['dev.re']]
  lev.re <- dev.re - fit.re
  nd <- c(x[['nd.ab']], x[['nd.re']])
  w <- sqrt(c(dev.ab, dev.re) / nd)
  lev <- c(lev.ab, lev.re) / nd

  plot(w, lev, xlim=c(0, max(c(w, 2.5))), ylim=c(0, max(c(lev, 4))),
       xlab="Square root of residual deviance", ylab="Leverage",
       main="Leverage versus residual deviance",
       ...)
  mtext("Per-study mean per-datapoint contribution")

  x <- seq(from=0, to=3, by=0.05)
  for (c in 1:4) { lines(x, c - x^2) }
}

plot.mtc.deviance <- function(x, auto.layout=TRUE, ...) {
  if (auto.layout) {
    par(mfrow=c(2,1))
  }
  mtc.devplot(x, ...)
  mtc.levplot(x, ...)
}
