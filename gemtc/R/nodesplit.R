# Change-of-Baseline matrix
# old: vector of old arms, first element the baseline
# new: vector of new arms, first element the baseline
cob.matrix <- function(old, new) {
  stopifnot(length(old) >= length(new))
  n <- length(old) - 1
  m <- length(new) - 1
  b <- matrix(0, nrow=m, ncol=n)
  for (i in 1:m) {
    for (j in 1:n) {
      if (old[j + 1] == new[i + 1]) b[i, j] <- 1
      if (old[j + 1] == new[1]) b[i, j] <- -1
    }
  }
  b
}

nodesplit.rewrite.data.ab <- function(data, t1, t2) {
  if (is.null(data)) return(NULL);
  t1 <- as.character(t1)
  t2 <- as.character(t2)
  studies <- unique(data$study)
  per.study <- lapply(studies, function(study) {
    study.data <- data[data$study == study, ]
    study.data$study <- as.character(study.data$study)
    has.both <- all(c(t1, t2) %in% study.data$treatment)
    if (nrow(study.data) == 3 && has.both) {
      study.data <- study.data[study.data$treatment %in% c(t1, t2), ]
    } else if (nrow(study.data) > 3 && has.both) {
      sel <- study.data$treatment %in% c(t1, t2)
      study.data$study[sel] <- paste(study, '*', sep='')
      study.data$study[!sel] <- paste(study, '**', sep='')
    }
    study.data
  })
  data <- do.call(rbind, per.study)
  data$study <- as.factor(data$study)
  data
}

nodesplit.rewrite.data.re <- function(data, t1, t2) {
  if (is.null(data)) return(NULL);
  t1 <- as.character(t1)
  t2 <- as.character(t2)
  cob <- function(study, data, ts) {
    old <- as.character(data$treatment)
    new <- c(ts, old[!(old %in% ts)])
    n <- length(old) - 1
    m <- length(ts)
    b <- cob.matrix(old, new)
    diff <- c(NA, b %*% data$diff[-1])[1:m]

    cov.m <- matrix(data$std.err[1], nrow=n, ncol=n)
    diag(cov.m) <- data$std.err[-1]
    cov.m <- b %*% cov.m %*% t(b)
    std.err <- c(cov.m[1, 2], diag(cov.m))[1:m]

    treatment <- factor(ts, levels=levels(data$treatment))
    data.frame(study=rep(study, m),
               treatment=treatment,
               diff=diff,
               std.err=std.err)
  }
  studies <- unique(data$study)
  per.study <- lapply(studies, function(study) {
    study.data <- data[data$study == study, ]
    study.data$study <- as.character(study.data$study)
    has.both <- all(c(t1, t2) %in% study.data$treatment)
    if (nrow(study.data) == 3 && has.both) {
      study.data <- cob(study, study.data, c(t1, t2))
    } else if (nrow(study.data) > 3 && has.both) {
      ts <- as.character(study.data$treatment)
      ts <- ts[!(ts %in% c(t1, t2))]
      study.data <- rbind(cob(paste(study, '*', sep=''), study.data, c(t1, t2)),
                          cob(paste(study, '**', sep=''), study.data, ts))
    }
    study.data[, c('study','treatment','diff','std.err')]
  })
  data <- do.call(rbind, per.study)
  data$study <- as.factor(data$study)
  data
}

mtc.nodesplit.comparisons <- function(network) {
  comparisons <- as.data.frame(mtc.comparisons(network))
  comparisons <- apply(comparisons, 1, function(comparison) {
    if (has.indirect.evidence(network, comparison['t1'], comparison['t2'])) comparison else NULL
  })
  if (is.list(comparisons)) {
    as.data.frame(do.call(rbind, comparisons), stringsAsFactors=FALSE)
  } else if (is.null(comparisons)) {
    data.frame(t1=character(), t2=character(), stringsAsFactors=FALSE)
  } else {
    as.data.frame(t(comparisons), stringsAsFactors=FALSE)
  }
}

mtc.nodesplit <- function(network, comparisons=mtc.nodesplit.comparisons(network), ...) {
  stopifnot(nrow(comparisons) > 0)
  results <- apply(comparisons, 1, function(comparison) {
        mtc.model.run(network, type='nodesplit', t1=comparison['t1'], t2=comparison['t2'], ...)
  })
  params <- apply(comparisons, 1, function(comparison) {
        paste('d', comparison['t1'], comparison['t2'], sep='.')
  })
  names(results) <- params
  results$consistency <- mtc.model.run(network, type='consistency', ...)
  class(results) <- "mtc.nodesplit"
  results
}

print.mtc.nodesplit <- function(x, ...) {
  cat("Node-splitting analysis of inconsistency (mtc.nodesplit) object\n")
  for (name in names(x)) {
    cat(paste("$", name, ": ", class(x[[name]]), "\n", sep=""))
  }
}

plot.mtc.nodesplit <- function(x, ask=dev.interactive(orNone=TRUE), ...) {
  cat("Node-splitting -- convergence plots\n")
  for (name in names(x)) {
    cat(if (name == "consistency") "Consistency model:\n" else paste("Split-node ", name, ":\n", sep=""))
    if(name != names(x)[1]) par(ask=ask)
    plot(x[[name]], ask=ask, ...)
  }
}

summary.mtc.nodesplit <- function(object, ...) {
  params <- names(object)
  params <- params[params != 'consistency']
  p.value <- do.call(rbind, lapply(params, function(param) {
    samples <- object[[param]][['samples']]
    split <- object[[param]][['model']][['split']]
    samples.dir <- as.matrix(samples[ , 'd.direct', drop=FALSE])
    samples.ind <- as.matrix(samples[ , 'd.indirect', drop=FALSE])
    p <- sum(samples.dir > samples.ind) / length(samples.dir)
    data.frame(t1=split[1], t2=split[2], p=2 * min(p, 1 - p))
  }))
  dir.effect <- do.call(rbind, lapply(params, function(param) {
    samples <- object[[param]][['samples']]
    split <- object[[param]][['model']][['split']]
    samples.dir <- as.matrix(samples[ , 'd.direct', drop=FALSE])
    qs <- quantile(as.numeric(samples.dir), c(0.025, 0.5, 0.975))
    data.frame(t1=split[1], t2=split[2], pe=qs[2], ci.l=qs[1], ci.u=qs[3])
  }))
  ind.effect <- do.call(rbind, lapply(params, function(param) {
    samples <- object[[param]][['samples']]
    split <- object[[param]][['model']][['split']]
    samples.ind <- as.matrix(samples[ , 'd.indirect', drop=FALSE])
    qs <- quantile(as.numeric(samples.ind), c(0.025, 0.5, 0.975))
    data.frame(t1=split[1], t2=split[2], pe=qs[2], ci.l=qs[1], ci.u=qs[3])
  }))
  cons.effect <- do.call(rbind, lapply(params, function(param) {
    split <- object[[param]][['model']][['split']]
    samples <- relative.effect(object[['consistency']], t1=split[1], t2=split[2], preserve.extra=FALSE)$samples
    qs <- quantile(as.matrix(samples), c(0.025, 0.5, 0.975))
    data.frame(t1=split[1], t2=split[2], pe=qs[2], ci.l=qs[1], ci.u=qs[3])
  }))
  result <- list(dir.effect=dir.effect,
                 ind.effect=ind.effect,
                 cons.effect=cons.effect,
                 p.value=p.value,
                 cons.model=object[['consistency']][['model']])
  class(result) <- 'mtc.nodesplit.summary'
  result
}

print.mtc.nodesplit.summary <- function(x, ...) {
  cat("Node-splitting analysis of inconsistency\n")
  cat("========================================\n\n")
  data <- do.call(rbind, lapply(1:length(x[['p.value']][['t1']]), function(i) {
    data.frame(comparison=c(paste('d', x$p.value[i,'t1'], x$p.value[i,'t2'], sep='.'), '-> direct', '-> indirect', '-> network'),
               p.value=c(x$p.value[i, 'p'], NA, NA, NA),
               CrI=c(NA,
                     formatCI(x$dir.effect[i,c('pe', 'ci.l', 'ci.u')]),
                     formatCI(x$ind.effect[i,c('pe', 'ci.l', 'ci.u')]),
                     formatCI(x$cons.effect[i,c('pe', 'ci.l', 'ci.u')])))

  }))
  formatted <- as.matrix(format.data.frame(data))
  formatted[is.na(data)] <- NA
  print(formatted, na.print="", quote=FALSE, row.names=FALSE)
}

plot.mtc.nodesplit.summary <- function(x, ...) {
  data <- list(id=character(), group=character(), p=c(), pe=c(), ci.l=c(), ci.u=c(), style=factor(levels=c('normal','pooled')))
  t1 <- x[['p.value']][['t1']]
  t2 <- x[['p.value']][['t2']]
  params <- paste('d', t1, t2, sep='.')
  group.labels <- paste(t2, 'vs', t1)
  names(group.labels) <- params

  data <- do.call(rbind, lapply(1:length(params), function(i) {
    data.frame(id=c('direct', 'indirect', 'network'),
               group=rep(params[i], 3),
               style=rep('normal', 3),
               p=c(NA, x$p.value[i, 'p'], NA),
               pe=c(x$dir.effect[i,'pe'], x$ind.effect[i,'pe'], x$cons.effect[i,'pe']),
               ci.l=c(x$dir.effect[i,'ci.l'], x$ind.effect[i,'ci.l'], x$cons.effect[i,'ci.l']),
               ci.u=c(x$dir.effect[i,'ci.u'], x$ind.effect[i,'ci.u'], x$cons.effect[i,'ci.u']))
  }))

  blobbogram(data, group.labels=group.labels, columns=c('p'), column.labels='P-value',
    ci.label=paste(ll.call("scale.name", x[['cons.model']]), "(95% CrI)"),
    log.scale=ll.call("scale.log", x[['cons.model']]), ...)
}
