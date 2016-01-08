#' @include stopIfNotConsistent.R

rank.probability <- function(result, preferredDirection=1, covariate=NA) {
  stopIfNotConsistent(result, 'rank.probability')

  stopifnot(preferredDirection %in% c(1, -1))

  treatments <- sort(unique(as.vector(extract.comparisons(colnames(result[['samples']][[1]])))))

  n.alt <- length(treatments)

  # count ranks given a matrix d of relative effects (treatments as rows)
  rank.count <- function(d) {
    .Call("gemtc_rank_count", d)
  }

  d <- relative.effect(result, treatments[1], treatments, covariate=covariate, preserve.extra=FALSE)[['samples']]
  counts <- lapply(d, function(chain) { rank.count(t(chain)) })
  ranks <- Reduce(function(a, b) { a + b }, counts)
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

print.mtc.rank.probability <- function(x, ...) {
  cat(paste("Rank probability; preferred direction = ", attr(x, "direction"), "\n", sep=""))
  attr(x, "direction") <- NULL
  print(unclass(x), ...)
}

plot.mtc.rank.probability <- function(x, ...) { 
  barplot(t(x), ...)
}
