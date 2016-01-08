relative.effect.table <- function(result, covariate=NA) {
  ts <- as.character(result[['model']][['network']][['treatments']][['id']])
  tbl <- array(NA, dim=c(length(ts), length(ts), 3), dimnames=list(ts, ts, c("2.5%", "50%", "97.5%")))
  comps <- combn(ts, 2)

  for (i in 1:ncol(comps)) {
    comp <- comps[,i]
    samples <- as.matrix(relative.effect(result, comp[1], comp[2], preserve.extra=FALSE, covariate=covariate)$samples)
    q <- quantile(samples, prob=c(0.025, 0.5, 0.975))
    tbl[comp[1], comp[2],] <- unname(q)
    q.inv <- c(-q[3], -q[2], -q[1])
    tbl[comp[2], comp[1],] <- unname(q.inv)
  }

  attr(tbl, "model") <- result[['model']]
  attr(tbl, "covariate") <- covariate
  class(tbl) <- "mtc.relative.effect.table"

  tbl
}

relative.effect.table.to.matrix <- function(x, formatNumber=formatC) {
  y <- apply(x, c(1,2), function(x) {
    if (all(!is.na(x))) {
      paste0(formatNumber(x[2]), " (", formatNumber(x[1]), ", ", formatNumber(x[3]), ")")
    } else {
      NA
    }
  })
  diag(y) <- rownames(x)
  y
}

as.data.frame.mtc.relative.effect.table <- function(x, ...) {
  as.data.frame(relative.effect.table.to.matrix(x, paste), stringsAsFactors=FALSE)
}

print.mtc.relative.effect.table <- function(x, ...) {
  scale.log <- if (ll.call('scale.log', attr(x, 'model'))) 'Log ' else ''
  scale.name <- ll.call('scale.name', attr(x, 'model'))
  y <- relative.effect.table.to.matrix(x)

  cat(paste0(scale.log, scale.name, " (95% CrI)\n\n"))
  write.table(format(y, justify="centre"), quote=FALSE, row.names=FALSE, col.names=FALSE)
}

forest.mtc.relative.effect.table <- function(x, t1=rownames(x)[1], use.description=FALSE, ...) {
  i1 <- which(rownames(x) == t1)
  stats <- x[i1, -i1,]

  model <- attr(x, 'model')
  network <- model[['network']]

  ts <- rownames(stats)
  if (use.description) {
    ts <- treatment.id.to.description(network, ts)
    t1 <- treatment.id.to.description(network, t1)
  }

  data <- data.frame(id=ts,
                     pe=stats[,2], ci.l=stats[,1], ci.u=stats[,3],
                     style="normal")

  blobbogram(data,
    columns=c(), column.labels=c(),
    id.label="",
    ci.label=paste(ll.call('scale.name', model), "(95% CrI)"),
    log.scale=ll.call('scale.log', model),
    center.label=paste("Compared with", t1),
    ...)
}
