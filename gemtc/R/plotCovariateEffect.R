plotCovariateEffect <- function(result, t1, t2, xlim=NULL, ylim=NULL, ask=dev.interactive(orNone=TRUE)) {
  regressor <- result[['model']][['regressor']]
  if (is.null(xlim)) {
    if (regressor[['type']] == 'continuous') {
      studies <- result[['model']][['network']][['studies']]
      observed <- studies[, regressor[['variable']]]
      ctr <- regressor[['center']]
      xlim <- c(min(observed, na.rm=TRUE), max(observed, na.rm=TRUE))
      xvals <- seq(xlim[1], xlim[2], length.out=7)
    } else {
      xlim <- c(-0.5, 1.5)
      xvals <- c(0, 1)
    }
  } else {
    xvals <- seq(xlim[1], xlim[2], length.out=7)
  }

  pairs <- treatment.pairs(t1, t2, result[['model']][['network']][['treatments']][['id']])
  res <- lapply(xvals, function(xval) {
    re <- relative.effect(result, t1, t2, preserve.extra=FALSE, covariate=xval)
    samples <- as.matrix(re[['samples']])
    stats <- t(apply(samples, 2, quantile, probs=c(0.025, 0.5, 0.975)))
    comps <- extract.comparisons(rownames(stats))
    data.frame(t1=comps[,1], t2=comps[,2], median=stats[,"50%"], lower=stats[,"2.5%"], upper=stats[,"97.5%"], stringsAsFactors=FALSE)
  })

  if (is.null(ylim)) {
    ylim <- c(min(sapply(res, function(stats) { min(stats[['lower']]) })),
              max(sapply(res, function(stats) { max(stats[['upper']]) })))
  }

  first <- TRUE
  devAskNewPage(FALSE)
  for (pair in split(pairs, seq(nrow(pairs)))) {
    pair <- as.treatment.factor(pair, result[['model']][['network']])
    yvals <- sapply(res, function(stats) { stats[stats[['t1']] == pair[1] & stats[['t2']] == pair[2], c('median', 'lower', 'upper')] })
    if (regressor[['type']] == 'continuous') {
      plot(xvals, yvals['median', ], type='l', xlim=xlim, ylim=ylim, main="Treatment effect vs. covariate", xlab=regressor[["variable"]], ylab=paste("d", pair[1], pair[2], sep="."))
      lines(xvals, yvals['lower', ], lty=2)
      lines(xvals, yvals['upper', ], lty=2)
    } else {
      plot(xvals, yvals['median', ], type='p', xlim=xlim, ylim=ylim, main="Treatment effect vs. covariate", xlab=regressor[["variable"]], ylab=paste("d", pair[1], pair[2], sep="."), xaxp=c(0, 1, 1))
      segments(xvals, unlist(yvals['lower',]), xvals, unlist(yvals['upper',]))
      eps <- 0.01
      segments(xvals-eps, unlist(yvals['lower',]), xvals+eps, unlist(yvals['lower',]))
      segments(xvals-eps, unlist(yvals['upper',]), xvals+eps, unlist(yvals['upper',]))
    }
    if (first) devAskNewPage(ask)
    first <- FALSE
  }
}
