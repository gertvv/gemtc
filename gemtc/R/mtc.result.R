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
    stats <- quantiles[-dim(quantiles)[1],]
    if(class(stats) == "numeric") { # Selecting a single row returns a numeric 
        stats <- as.matrix(t(stats))
        row.names(stats) <- row.names(quantiles)[[1]]
    }
    data <- data.frame(id=rownames(stats), pe=stats[,3], ci.l=stats[,1], ci.u=stats[,5], group=NA, style="normal")
    blobbogram(data,
        columns=c(), column.labels=c(),
        id.label="Comparison", ci.label="Odds Ratio (95% CrI)", log.scale=TRUE,
        grouped=FALSE)
}

as.mcmc.list.mtc.result <- function(x, ...) {
    x$samples
}
