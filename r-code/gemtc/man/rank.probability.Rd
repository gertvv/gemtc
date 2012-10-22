\encoding{utf8}
\name{rank.probability}
\alias{rank.probability}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{Calculating rank-probabilities}
\description{
}
\usage{
rank.probability(result)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{result}{Object of S3 class \code{mtc.result} to create the rank probability table for}
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
	results <- mtc.run(model, "YADAS")

	ranks <- rank.probability(results)
	summary(ranks)
	barplot(t(ranks), beside=TRUE)
}
