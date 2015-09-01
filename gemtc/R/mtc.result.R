## mtc.result class methods
print.mtc.result <- function(x, ...) {
  cat("MTC ", x[['model']][['type']], " results: ", x[['model']][['description']], sep="")
  print(x[['samples']])
}

summary.mtc.result <- function(object, ...) {
  scale.log <- if (ll.call('scale.log', object[['model']])) 'Log ' else ''
  scale.name <- ll.call('scale.name', object[['model']])
  rval <- list('measure'=paste0(scale.log, scale.name),
       'summaries'=summary(object[['samples']]),
       'DIC'=unlist(object[['deviance']][c('Dbar', 'pD', 'DIC')]))
  class(rval) <- 'summary.mtc.result'
  rval
}

print.summary.mtc.result <- function(x, ...) {
  cat(paste("\nResults on the", x[['measure']], "scale\n"))
  print(x[['summaries']])
  cat("3. Model fit (residual deviance):\n\n")
  print(x[['DIC']])
  cat("\n")
}

plot.mtc.result <- function(x, ...) {
  plot(x[['samples']], ...)
}

forest.mtc.result <- function(x, use.description=FALSE, ...) {
  if (tolower(x[['model']][['type']]) != 'consistency') stop("Can only apply forest.mtc.result to consistency models")

  varnames <- colnames(x[['samples']][[1]])
  samples <- as.matrix(x[['samples']][, grep("^d\\.", varnames)])
  stats <- t(apply(samples, 2, quantile, probs=c(0.025, 0.5, 0.975)))
  model <- x[['model']]
  network <- model[['network']]
  comps <- extract.comparisons(varnames)
  groups <- comps[,1]
  group.names <- unique(groups)
  group.labels <- paste("Compared with", if (use.description) treatment.id.to.description(network, group.names) else group.names)
  names(group.labels) <- group.names
  params <- list(...)

  data <- data.frame(
    id=if (use.description) treatment.id.to.description(network, comps[,2]) else comps[,2],
    pe=stats[,2], ci.l=stats[,1], ci.u=stats[,3],
    group=groups, style="normal")

  blobbogram(data,
    columns=c(), column.labels=c(),
    id.label="",
    ci.label=paste(ll.call('scale.name', model), "(95% CrI)"),
    log.scale=ll.call('scale.log', model),
    grouped=TRUE, group.labels=group.labels,
    ...)
}

as.mcmc.list.mtc.result <- function(x, ...) {
  x[['samples']]
}
