## mtc.result class methods
print.mtc.result <- function(x, ...) {
    cat("MTC ", x$model$type, " results: ", x$model$description, sep="")
    print(x$samples)
}

summary.mtc.result <- function(object, ...) {
    summary(object$samples)
}

plot.mtc.result <- function(x, ...) {
    plot(x$samples, ...)
}

forest.mtc.result <- function(x, ...) { 
    quantiles <- summary(x)$quantiles 
    stats <- quantiles[grep("^d\\.", rownames(quantiles)),]
    if(class(stats) == "numeric") { # Selecting a single row returns a numeric 
        stats <- as.matrix(t(stats))
        row.names(stats) <- row.names(quantiles)[[1]]
    }
    groups <- extract.comparisons(rownames(quantiles))[,1]
    group.names <- unique(groups)
    group.labels <- rep("", length(group.names))
    #group.labels <- paste("Relative to ", group.names)
    names(group.labels) <- group.names
    data <- data.frame(
        id=rownames(stats),
        pe=stats[,3], ci.l=stats[,1], ci.u=stats[,5],
        group=groups, style="normal")
    blobbogram(data,
        columns=c(), column.labels=c(),
        id.label="Comparison",
		ci.label=paste(ll.call('scale.name', model), "(95% CrI)"),
		log.scale=ll.call('scale.log', model),
        grouped=length(group.labels)>1, group.labels=group.labels)
}

as.mcmc.list.mtc.result <- function(x, ...) {
    x$samples
}
