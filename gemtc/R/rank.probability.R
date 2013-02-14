rank.probability <- function(result) {
    treatments <- result$model$network$treatments$id
    n.alt <- length(treatments)

    # count ranks given a matrix d of relative effects (treatments as rows)
    rank.count <- function(d) {
        n.iter <- dim(d)[2]
        .C("gemtc_rank_count",
            as.double(d), as.integer(n.iter), as.integer(n.alt),
            counts=matrix(0.0, nrow=n.alt, ncol=n.alt),
            NAOK=FALSE, DUP=FALSE, PACKAGE="gemtc")$counts
    }

    d <- relative.effect(result, treatments[1], treatments, preserve.extra=FALSE)$samples
    counts <- lapply(d, function(chain) { rank.count(t(chain)) })
    ranks <- Reduce(function(a, b) { a + b }, counts)
    colnames(ranks) <- treatments

    data <- result$samples
    n.iter <- nchain(data) * (end(data) - start(data) + thin(data)) / thin(data)

    t(ranks / n.iter)
}
