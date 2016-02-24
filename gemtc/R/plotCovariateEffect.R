plotCovariateEffect <- function(result, t1, t2, xlim=NULL, ylim=NULL, ask=dev.interactive(orNone=TRUE)) {
  regressor <- result[['model']][['regressor']]
  if (is.null(xlim)) {
    if (regressor[['type']] == 'continuous') {
      ctr <- regressor[['center']]
      scale <- regressor[['scale']]
      xlim <- c(ctr - 1.5*scale, ctr + 1.5*scale)
      xvals <- seq(xlim[1], xlim[2], length.out=7)
    } else {
      xlim <- c(0, 1)
      xvals <- xlim
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
    data.frame(t1=comps[,1], t2=comps[,2], median=stats[,"50%"], lower=stats[,"2.5%"], upper=stats[,"97.5%"])
  })

  if (is.null(ylim)) {
    ylim <- c(min(sapply(res, function(stats) { min(stats[['lower']]) })),
              max(sapply(res, function(stats) { max(stats[['upper']]) })))
  }

  first <- TRUE
  devAskNewPage(FALSE)
  for (pair in split(pairs, seq(nrow(pairs)))) {
    yvals <- sapply(res, function(stats) { stats[stats[['t1']] == pair[1] & stats[['t2']] == pair[2], c('median', 'lower', 'upper')] })
    plot(xvals, yvals['median', ], type='l', xlim=xlim, ylim=ylim, main="Treatment effect vs. covariate", xlab=regressor[["variable"]], ylab=paste("d", pair[1], pair[2], sep="."))
    lines(xvals, yvals['lower', ], lty=2)
    lines(xvals, yvals['upper', ], lty=2)
    if (first) devAskNewPage(ask)
    first <- FALSE
  }
}
