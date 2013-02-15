\encoding{utf8}
\name{rank.probability}
\alias{rank.probability}
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
##       [,1]      [,2]      [,3]      [,4]
## A 0.000100 0.0021000 0.1073125 0.8904875
## B 0.059125 0.1776500 0.6580000 0.1052250
## C 0.230425 0.5983625 0.1709250 0.0002875
## D 0.710500 0.2218375 0.0637125 0.0039500

barplot(t(ranks), beside=TRUE) # plot a 'rankogram'
}
