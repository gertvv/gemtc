rank.probability <- function(result, preferredDirection=1) {
  stopifnot(preferredDirection %in% c(1, -1))

  treatments <- sort(unique(as.vector(extract.comparisons(colnames(result[['samples']][[1]])))))

  n.alt <- length(treatments)

  # count ranks given a matrix d of relative effects (treatments as rows)
  rank.count <- function(d) {
    n.iter <- dim(d)[2]
    .C("gemtc_rank_count",
      as.double(d), as.integer(n.iter), as.integer(n.alt),
      counts=matrix(0.0, nrow=n.alt, ncol=n.alt),
      NAOK=FALSE, DUP=FALSE, PACKAGE="gemtc")[['counts']]
  }

  d <- relative.effect(result, treatments[1], treatments, preserve.extra=FALSE)[['samples']]
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
