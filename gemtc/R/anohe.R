all.pair.matrix <- function(m) {
  do.call(rbind, lapply(1:(m - 1), function(i) {
    do.call(rbind, lapply((i + 1):m, function(j) {
      c(i, j)
    }))
  }))
}

filter.network <- function(network, filter, filter.ab=filter, filter.re=filter) {
  data.ab <- if (!is.null(network[['data.ab']])) {
    network[['data.ab']][apply(network[['data.ab']], 1, filter.ab), , drop=FALSE]
  }
  data.re <- if (!is.null(network[['data.re']])) {
    network[['data.re']][apply(network[['data.re']], 1, filter.re), , drop=FALSE]
  }
  mtc.network(data=data.ab, data.re=data.re, treatments=network$treatments)
}

decompose.variance <- function(V) {
  na <- ncol(V)
  J <- matrix(1, ncol=na, nrow=na)
  # Pseudo-inverse Laplacian
  Lt <- -0.5 * (V - (1 / na) * (V %*% J + J %*% V) + (1 / na^2) * (J %*% V %*% J))
  # Laplacian
  L <- solve(Lt - J/na) + J/na

  prec <- -L
  diag(prec) <- Inf
  1/prec
}

# Decompose trials based on a consistency model and study.samples
decompose.trials <- function(result) {
  decompose.study <- function(samples, study) {
    na <- ncol(samples) + 1
    samples <- cbind(0, samples)
    mu <- sapply(1:na, function(i) {
      sapply(1:na, function(j) {
        mean(samples[ , i, drop=TRUE] - samples[ , j, drop=TRUE])
      })
    })
    # Effective variances
    V <- sapply(1:na, function(i) {
      sapply(1:na, function(j) {
        var(samples[ , i, drop=TRUE] - samples[ , j, drop=TRUE])
      })
    })

    var <- decompose.variance(V)
    se <- sqrt(var)
    if (any(is.nan(se))) {
      stop(paste(paste0("Decomposed variance ill-defined for ", study, ". Most likely the USE did not converge:"), paste(capture.output(print(var)), collapse="\n"), sep="\n"))
    }

    pairs <- all.pair.matrix(na)
    list(
      mu = apply(pairs, 1, function(p) { mu[p[1], p[2], drop=TRUE] }),
      se = apply(pairs, 1, function(p) { se[p[1], p[2], drop=TRUE] })
    )
  }

  study.samples <- as.matrix(result[['samples']])
  studies <- unique(mtc.merge.data(result[['model']][['network']])[['study']])

  decomposed <- lapply(1:length(studies), function(i) {
    study <- mtc.study.design(result[['model']][['network']], studies[i])
    study <- study[!is.na(study)]
    na <- length(study)
    colIndexes <- grep(paste("delta[", i, ",", sep=""), colnames(study.samples), fixed=TRUE)
    stopifnot(length(colIndexes) == (na - 1)) # A bug in WinBUGS caused this to happen -- repeated variables
    effects <- if (na > 2) {
      data <- decompose.study(
        study.samples[, colIndexes, drop=FALSE], studies[i])
      ts <- matrix(study[all.pair.matrix(na)], ncol=2)
      list(m=data[['mu']], e=data[['se']], t=ts)
    } else {
      samples <- study.samples[ , colIndexes, drop=FALSE]
      list(m=apply(samples, 2, mean), e=apply(samples, 2, sd), t=matrix(study, nrow=1))
    }

    # make the baseline treatment consistent (i.e. in the same order as the
    # basic parameters)
    for (k in 1:length(effects[['m']])) {
      t1 <- effects$t[k, 1]
      t2 <- effects$t[k, 2]
      if (t1 > t2) {
        effects$t[k, 1] <- t2
        effects$t[k, 2] <- t1
        effects$m[k] <- -effects$m[k]
      }
    }
    effects
  })

  # (mu1, prec1): posterior parameters
  mu1 <- lapply(decomposed, function(x) { x[['m']] })
  sigma1 <- lapply(decomposed, function(x) { x[['e']] })

  ts <- lapply(decomposed, function(x) { x[['t']] })

  studyNames <- mtc.studies.list(result[['model']][['network']])[['values']]
  names(ts) <- studyNames
  names(mu1) <- studyNames
  names(sigma1) <- studyNames

  list(t=ts, m=mu1, e=sigma1)
}

# decomposes the given network's multi-arm trials into
# a series of (approximately) equivalent two-arm trials
decompose.network <- function(network, result) {
  # find all multi-arm trials
  data <- mtc.merge.data(network)
  data[['study']] <- as.character(data[['study']])
  studies <- unique(data[['study']])
  studies <- studies[sapply(studies, function(study) { sum(data[['study']] == study) > 2 })]

  data <- decompose.trials(result)
  data.re <- do.call(rbind, lapply(studies, function(study) {
    do.call(rbind, lapply(1:length(data[['m']][[study]]), function(j) {
      ts <- data[['t']][[study]][j, , drop=TRUE]
      m <- data[['m']][[study]][j]
      e <- data[['e']][[study]][j]
      rbind(
        data.frame(study=paste(study, ts[1], ts[2], sep="__"), treatment=ts[1], diff=NA, std.err=NA, stringsAsFactors=FALSE),
        data.frame(study=paste(study, ts[1], ts[2], sep="__"), treatment=ts[2], diff=m, std.err=e, stringsAsFactors=FALSE)
      )
    }))
  }))

  ta.network <- filter.network(network, function(row) { !(row['study'] %in% studies) })
  ta.data.re <- ta.network[['data.re']][ , c('study','treatment','diff','std.err')]
  mtc.network(data.ab=ta.network[['data.ab']], data.re=rbind(ta.data.re, data.re), treatments=network$treatments)
}

mtc.anohe <- function(network, ...) {
  network <- fix.network(network)

  result.use <- mtc.model.run(network, type='use', ...)

  network.decomp <- decompose.network(network, result=result.use)
  result.ume <- mtc.model.run(network.decomp, type='ume', ...)

  result.cons <- mtc.model.run(network, type='consistency', ...)

  result <- list(result.cons=result.cons, result.ume=result.ume, result.use=result.use)
  class(result) <- "mtc.anohe"
  result
}

print.mtc.anohe <- function(x, ...) {
  cat("Analysis of heterogeneity (mtc.anohe) object\n")
  for (name in names(x)) {
    cat(paste("$", name, ": ", class(x[[name]]), "\n", sep=""))
  }
}

plot.mtc.anohe <- function(x, ask=dev.interactive(orNone=TRUE), ...) {
  cat("Analysis of heterogeneity -- convergence plots\n")

  cat("Unrelated Study Effects (USE) model:\n")
  plot(x[['result.use']], ask=ask, ...)
  cat("Unrelated Mean Effects (UME) model:\n")
  par(ask=ask)
  plot(x[['result.ume']], ask=ask, ...)
  cat("Consistency model:\n")
  par(ask=ask)
  plot(x[['result.cons']], ask=ask, ...)
}

i.squared <- function (mu, se, x, df.adj=-1) {
  stopifnot(is.numeric(mu))
  stopifnot(is.numeric(se))
  stopifnot(is.numeric(x))
  stopifnot(is.numeric(df.adj))
  stopifnot(length(mu) == length(se))
  stopifnot(length(mu) == length(x))
  dev <- (mu - x)^2 # squared deviance
  q <- sum(dev * 1/se^2) # Cochran's Q
  100 * max(0, (q - length(mu) - df.adj) / q) # I^2
}

summary.mtc.anohe <- function(object, ...) {
  result.use <- object[['result.use']]
  result.ume <- object[['result.ume']]
  result.cons <- object[['result.cons']]
  network <- result.cons[['model']][['network']]

  se <- decompose.trials(result.use)
  se[['s']] <- lapply(names(se[['e']]), function(s) { rep(s, length(se[['e']][[s]])) })
  se[['s']] <- do.call(c, se[['s']])
  se[['t']] <- do.call(rbind, se[['t']])
  se[['m']] <- do.call(c, se[['m']])
  se[['e']] <- do.call(c, se[['e']])
  studyEffects <- data.frame(study=se[['s']], t1=se[['t']][,1], t2=se[['t']][,2], pe=se[['m']], ci.l=se[['m']]-1.96*se[['e']], ci.u=se[['m']]+1.96*se[['e']], stringsAsFactors=FALSE)
  rownames(studyEffects) <- NULL

  ume.samples <- as.matrix(result.ume[['samples']])
  varNames <- colnames(ume.samples)
  varNames <- varNames[grep('^d\\.', varNames)]
  ume.samples <- ume.samples[,varNames,drop=FALSE]
  comps <- extract.comparisons(varNames)
  qs <- apply(ume.samples, 2, function(samples) { quantile(samples, c(0.025, 0.5, 0.975)) })
  pairEffects <- data.frame(t1=comps[,1], t2=comps[,2], pe=qs[2,], ci.l=qs[1,], ci.u=qs[3,], stringsAsFactors=FALSE)
  rownames(pairEffects) <- NULL

  cons.samples <- as.matrix(relative.effect(result.cons, t1=comps[,1], t2=comps[,2], preserve.extra=FALSE)[['samples']])
  qs <- apply(cons.samples, 2, function(samples) { quantile(samples, c(0.025, 0.5, 0.975)) })
  consEffects <- data.frame(t1=comps[,1], t2=comps[,2], pe=qs[2,], ci.l=qs[1,], ci.u=qs[3,], stringsAsFactors=FALSE)
  rownames(consEffects) <- NULL

  data <- studyEffects
  data[['t1']] <- as.character(data[['t1']])
  data[['t2']] <- as.character(data[['t2']])
  data[['p']] <- sapply(1:nrow(data), function(i) {
    row <- data[i, , drop=TRUE]
    pairEffects[['pe']][pairEffects[['t1']] == row[['t1']] & pairEffects[['t2']] == row[['t2']]]
  })
  data[['c']] <- sapply(1:nrow(data), function(i) {
    row <- studyEffects[i, , drop=TRUE]
    consEffects[['pe']][consEffects[['t1']] == row[['t1']] & consEffects[['t2']] == row[['t2']]]
  })
  data[['se']] <- (data[['ci.u']] - data[['ci.l']]) / 3.92

  pairEffects[['t1']] <- as.character(pairEffects[['t1']])
  pairEffects[['t2']] <- as.character(pairEffects[['t2']])

  indEffects <- data.frame(t1=pairEffects[['t1']], t2=pairEffects[['t2']], stringsAsFactors=FALSE)
  indEffects <- cbind(indEffects, t(apply(pairEffects, 1, function(row) {
    has.indirect <- has.indirect.evidence(network, row['t1'], row['t2'])
    if (has.indirect) {
      dir <- as.numeric(row[3:5])
      names(dir) <- c('pe', 'ci.l', 'ci.u') # sigh
      con <- consEffects[consEffects[['t1']] == row['t1'] & consEffects[['t2']] == row['t2'], 3:5, drop=]

      se.con <- (con['ci.u'] - con['ci.l']) / 3.92
      se.dir <- (dir['ci.u'] - dir['ci.l']) / 3.92
      pe.con <- con['pe']
      pe.dir <- dir['pe']

      # Back-calculate indirect estimate (see Dias et al. 2010)
      if (se.con < se.dir) {
        se.ind <- sqrt(1 / (1 / se.con^2 - 1 / se.dir^2))
        pe.ind <- (pe.con / se.con^2 - pe.dir / se.dir^2) * se.ind^2
        unlist(list('pe'=unname(pe.ind), 'se'=unname(se.ind)))
      } else {
        unlist(list('pe'=NA, 'se'=NA))
      }
    } else {
      unlist(list('pe'=NA, 'se'=NA))
    }
  })))

  i2.pair <- apply(pairEffects, 1, function(row) {
    data2 <- data[data[['t1']] == row['t1'] & data[['t2']] == row['t2'], , drop=FALSE]
    if (nrow(data2) > 1) {
      i.squared(data2[['pe']], data2[['se']], data2[['p']])
    } else {
      NA
    }
  })

  i2.cons <- apply(pairEffects, 1, function(row) {
    data2 <- data[data[['t1']] == row['t1'] & data[['t2']] == row['t2'], , drop=FALSE]
    ind <- indEffects[indEffects[['t1']] == row['t1'] & indEffects[['t2']] == row['t2'], 3:4, drop=]
    se.ind <- unname(ind['se'])
    pe.ind <- unname(ind['pe'])
    if (!is.na(se.ind)) {
      i.squared(unlist(c(data2[['pe']], pe.ind)), unlist(c(data2[['se']], se.ind)), c(data2[['c']], data2[['c']][1]))
    } else if (nrow(data2) > 1) {
      i.squared(data2[['pe']], data2[['se']], data2[['c']])
    } else {
      NA
    }
  })

  incons <- apply(pairEffects, 1, function(row) {
    dir <- as.numeric(row[3:5])
    names(dir) <- c('pe', 'ci.l', 'ci.u') # sigh
    se.dir <- (dir['ci.u'] - dir['ci.l']) / 3.92
    pe.dir <- dir['pe']

    ind <- indEffects[indEffects[['t1']] == row['t1'] & indEffects[['t2']] == row['t2'], 3:4, drop=]
    se.ind <- ind['se']
    pe.ind <- ind['pe']
    if (!is.na(se.ind)) {
      pe.inc <- (pe.dir - pe.ind)
      se.inc <- sqrt(se.dir^2 + se.ind^2)
      p.inc <- pnorm(as.numeric(pe.inc / se.inc))

      as.numeric(2 * min(p.inc, 1 - p.inc))
    } else {
      NA
    }
  })

  i.sq <- data.frame(t1=pairEffects[['t1']], t2=pairEffects[['t2']], i2.pair=i2.pair, i2.cons=i2.cons, incons.p=incons, stringsAsFactors=FALSE)
  total <- list(i2.pair=i.squared(data[['pe']], data[['se']], data[['p']], df.adj=-nrow(pairEffects)), i2.cons=i.squared(data[['pe']], data[['se']], data[['c']], df.adj=-nrow(network[['treatments']])+1))
  result <- list(studyEffects=studyEffects, pairEffects=pairEffects, consEffects=consEffects, indEffects=indEffects,
    isquared.comp=i.sq, isquared.glob=total, cons.model=result.cons[['model']])
  class(result) <- 'mtc.anohe.summary'
  result
}

print.mtc.anohe.summary <- function(x, ...) {
  cat("Analysis of heterogeneity\n")
  cat("=========================\n\n")
  cat("Per-comparison I-squared:\n")
  cat("-------------------------\n\n")
  print(x[['isquared.comp']])
  cat("\nGlobal I-squared:\n")
  cat("-------------------------\n\n")
  print(as.data.frame(x[['isquared.glob']]))
}

plot.mtc.anohe.summary <- function(x, ...) {
  stats <- x
  data <- list(id=character(), group=character(), i2=c(), pe=c(), ci.l=c(), ci.u=c(), style=factor(levels=c('normal','pooled')))
  group.labels <- character()

  studyEffects <- stats[['studyEffects']]
  pairEffects <- stats[['pairEffects']]
  consEffects <- stats[['consEffects']]
  indEffects <- stats[['indEffects']]
  isq <- stats[['isquared.comp']]

  appendEstimates <- function(data, rows) {
    if (!is.null(rows[['i2']]) && !is.na(rows[['i2']])) {
      data[['i2']] <- c(data[['i2']], paste(formatC(rows[['i2']], format="f", digits=1), "%", sep=""))
    } else {
      data[['i2']] <- c(data[['i2']], rep(NA, length(rows[['pe']])))
    }
    data[['pe']] <- c(data[['pe']], rows[['pe']])
    data[['ci.l']] <- c(data[['ci.l']], rows[['ci.l']])
    data[['ci.u']] <- c(data[['ci.u']], rows[['ci.u']])
    data
  }

  for (i in 1:nrow(pairEffects)) {
    t1 <- pairEffects[['t1']][i]
    t2 <- pairEffects[['t2']][i]

    param <- paste('d', t1, t2, sep='.')
    group.labels[param] <- paste(t2, 'vs', t1)

    # Study-level effects
    rows <- studyEffects[studyEffects[['t1']] == t1 & studyEffects[['t2']] == t2, , drop=FALSE]
    data[['id']] <- c(data[['id']], rows[['study']])
    data[['group']] <- c(data[['group']], rep(param, nrow(rows)))
    data[['style']] <- c(data[['style']], rep('normal', nrow(rows)))
    data <- appendEstimates(data, rows)

    isq.rows <- isq[isq[['t1']] == t1 & isq[['t2']] == t2, ,drop=FALSE]
    # Pair-wise pooled effect
    rows <- pairEffects[i, , drop=FALSE]
    rows[['i2']] <- isq.rows[, 'i2.pair']
    data[['id']] <- c(data[['id']], 'Pooled (pair-wise)')
    data[['group']] <- c(data[['group']], param)
    data[['style']] <- c(data[['style']], 'pooled')
    data <- appendEstimates(data, rows)

    # Indirect effect
    rows <- indEffects[indEffects[['t1']] == t1 & indEffects[['t2']] == t2, , drop=FALSE]
    data[['id']] <- c(data[['id']], 'Indirect (back-calculated)')
    data[['group']] <- c(data[['group']], param)
    data[['style']] <- c(data[['style']], 'indirect')
    rows[['ci.l']] <- rows[['pe']] - 1.96 * rows[['se']]
    rows[['ci.u']] <- rows[['pe']] + 1.96 * rows[['se']]
    data <- appendEstimates(data, rows)

    # Network pooled effect
    rows <- consEffects[consEffects[['t1']] == t1 & consEffects[['t2']] == t2, , drop=FALSE]
    rows[['i2']] <- isq.rows[, 'i2.cons']
    data[['id']] <- c(data[['id']], 'Pooled (network)')
    data[['group']] <- c(data[['group']], param)
    data[['style']] <- c(data[['style']], 'pooled')
    data <- appendEstimates(data, rows)
  }

  styles <- blobbogram.styles.default()
  my.styles <- data.frame(style='indirect', font.weight='plain', row.height=1, pe.style='circle', pe.scale=FALSE, lty=3)
  rownames(my.styles) <- my.styles[['style']]
  styles <- rbind(styles, my.styles)


  data <- as.data.frame(data)
  om.scale <- x[['cons.model']][['om.scale']]
  log.scale <- ll.call("scale.log", x[['cons.model']])
  default.xlim=c(nice.value(-om.scale, floor, log.scale), nice.value(om.scale, ceiling, log.scale))
  do.plot <- function(xlim=default.xlim, ...) {
    blobbogram(data, group.labels=group.labels,
      columns=c('i2'), column.labels=c('I^2'),
      ci.label=paste(ll.call("scale.name", x[['cons.model']]), "(95% CrI)"),
      log.scale=log.scale,
      styles=styles, xlim=xlim, ...)
  }
  do.plot(...)
}
