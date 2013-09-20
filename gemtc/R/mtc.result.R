## mtc.result class methods
print.mtc.result <- function(x, ...) {
  cat("MTC ", x[['model']][['type']], " results: ", x[['model']][['description']], sep="")
  print(x[['samples']])
}

summary.mtc.result <- function(object, ...) {
  scale.log <- if (ll.call('scale.log', object[['model']])) 'Log ' else ''
  scale.name <- ll.call('scale.name', object[['model']])
  list('measure'=paste(scale.log, scale.name, sep=''),
       'summaries'=summary(object[['samples']]),
       'DIC'=object[['dic']])
}

plot.mtc.result <- function(x, ...) {
  plot(x[['samples']], ...)
}

forest.mtc.result <- function(x, ...) {
  quantiles <- summary(x[['samples']])[['quantiles']]
  model <- x[['model']]
  stats <- quantiles[grep("^d\\.", rownames(quantiles)), , drop=FALSE]
  comps <- extract.comparisons(rownames(stats))
  groups <- comps[,1]
  group.names <- unique(groups)
  group.labels <- rep("", length(group.names))
  #group.labels <- paste("Relative to ", group.names)
  names(group.labels) <- group.names
  params <- list(...)

  data <- data.frame(
    id=paste(comps[,2], comps[,1], sep=" vs "),
    pe=stats[,3], ci.l=stats[,1], ci.u=stats[,5],
    group=groups, style="normal")

  blobbogram(data,
    columns=c(), column.labels=c(),
    id.label="Comparison",
    ci.label=paste(ll.call('scale.name', model), "(95% CrI)"),
    log.scale=ll.call('scale.log', model),
    grouped=length(group.labels)>1, group.labels=group.labels,
    left.label=params[['left.label']], right.label=params[['right.label']],
    xlim=params[['xlim']])
}

as.mcmc.list.mtc.result <- function(x, ...) {
  x[['samples']]
}
