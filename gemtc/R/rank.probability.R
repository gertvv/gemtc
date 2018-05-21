#' @include stopIfNotConsistent.R

rank.probability <- function(result, preferredDirection=1, covariate=NA) {
  stopIfNotConsistent(result, 'rank.probability')

  stopifnot(preferredDirection %in% c(1, -1))

  treatments <- sort(unique(as.vector(extract.comparisons(colnames(result[['samples']][[1]])))))

  n.alt <- length(treatments)

  # count ranks given a matrix d of relative effects (treatments as rows)
  rank.count <- function(d) {
    .Call(gemtc_rank_count, d)
  }

  d <- relative.effect(result, treatments[1], treatments, covariate=covariate, preserve.extra=FALSE)[['samples']]
  ranks <- rank.count(t(as.matrix(d)))
  colnames(ranks) <- treatments

  data <- result[['samples']]
  n.iter <- nchain(data) * (end(data) - start(data) + thin(data)) / thin(data)

  result <- t(ranks / n.iter)
  if (identical(preferredDirection, -1)) {
    result <- result[,ncol(result):1]
  }
  class(result) <- "mtc.rank.probability"
  attr(result, "direction") <- preferredDirection
  result
}

sucra <- function(ranks) {
  apply(ranks, 1, function(p) {
    a <- length(p)
    sum(cumsum(p[-a]))/(a-1)
  })
}

rank.quantiles <- function(ranks, probs=c("2.5%"=0.025, "50%"=0.5, "97.5%"=0.975)) {
  sapply(probs, function(x) {
    apply(ranks, 1, function(p) {
      which(cumsum(p) >= x)[1]
    })
  })
}

print.mtc.rank.probability <- function(x, ...) {
  cat(paste("Rank probability; preferred direction = ", attr(x, "direction"), "\n", sep=""))
  attr(x, "direction") <- NULL
  print(unclass(x), ...)
}

plot.mtc.rank.probability <- function(x, ...) { 
  barplot(t(x), ...)
}
