\encoding{utf8}
\name{rank.probability}
\alias{rank.probability}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{Calculating rank-probabilities}
\description{
Rank probabilities indicate the probability for each treatment to be best, second best, etc.
}
\details{
For each MCMC iteration, the treatments are ranked by their effect relative to an arbitrary baseline.
A frequency table is constructed from these rankings and normalized by the number of iterations to give the rank probabilities.
}
\usage{
rank.probability(result)
}
\arguments{
  \item{result}{Object of S3 class \code{mtc.result} to be used in creation of the rank probability table}
}
\value{A matrix with the treatments as rows and the ranks as columns}

\author{Gert van Valkenhoef, Joël Kuiper}

\seealso{
\code{\link{relative.effect}}
}
\examples{
file <- system.file("extdata/luades-smoking.gemtc", package="gemtc")
network <- read.mtc.network(file)
model <- mtc.model(network)
# To save computation time we load the samples instead of running the model
\dontrun{results <- mtc.run(model)}
results <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))

ranks <- rank.probability(results)
print(ranks)
##       [,1]      [,2]     [,3]     [,4]
## A 0.0000500 0.0024250 0.104000 0.893525
## B 0.0571125 0.1802375 0.661125 0.101525
## C 0.2409625 0.5893875 0.169375 0.000275
## D 0.7020250 0.2279000 0.065450 0.004625
barplot(t(ranks), beside=TRUE)
}
