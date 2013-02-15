tree.relative.effect <- function(g, t1, t2) {
    if((is.null(t2) || length(t2) == 0) && length(t1) == 1) {
        t2 <- V(g)[V(g) != as.numeric(t1)]
    }
    if(length(t1) > length(t2)) t2 <- rep(t2, length.out=length(t1))
    if(length(t2) > length(t1)) t1 <- rep(t1, length.out=length(t2))
    pairs <- matrix(c(t1, t2), ncol=2)
    paths <- apply(pairs, 1, function(rel) {
        p <- unlist(get.shortest.paths(g, rel[1], rel[2], mode='all'))
        p <- matrix(c(p[1:length(p)-1], p[-1]), ncol=2)
        edges <- sapply(E(g), function(e) {
            v <- get.edge(g, e)
            if (sum(p[,1] == v[1] & p[,2] == v[2])) 1
            else if (sum(p[,1] == v[2] & p[,2] == v[1])) -1
            else 0
        })
    })
    paths <- as.matrix(paths) # necessary when there is only 1
    colnames(paths) <-  apply(pairs, 1, function(pair) { 
        pair <- V(g)[pair]$name
        paste('d', pair[1], pair[2], sep='.')
    })
    paths
}

relative.effect <- function(result, t1, t2 = c(), preserve.extra=TRUE) {
    if(result$model$type != "Consistency") stop("Cannot apply relative.effect to this model")

    # Build relative effect transformation matrix
    network <- result$model$network
    g <- result$model$tree
    if (is.character(t1)) {
        t1 <- as.treatment.factor(t1, network)
    }
    if (is.character(t2)) {
        t2 <- as.treatment.factor(t2, network)
    }
    effects <- tree.relative.effect(g, t1, t2)

    # Add rows/columns for parameters that are not relative effects
    nOut <- ncol(effects)
    nIn <- nrow(effects)
    nExtra <- ncol(result$samples[[1]]) - nIn
    effects <- rbind(effects, matrix(0, nrow=nExtra, ncol=nOut))
    if (preserve.extra) {
        allNames <- c(colnames(effects), colnames(result$samples[[1]])[nIn+(1:nExtra)])
        effects <- cbind(effects, 
            rbind(matrix(0, nrow=nIn, ncol=nExtra), diag(nExtra)))
        colnames(effects) <- allNames
    }

    # Apply tranformation to each chain
    samples <- as.mcmc.list(lapply(result$samples, function(chain) { 
        mcmc(chain %*% effects, start=start(chain), end=end(chain), thin=thin(chain))
    }))
    effects <- list(
        samples=samples,
        model=result$model,
        sampler=result$sampler)

    class(effects) <- "mtc.result"
    effects
}
