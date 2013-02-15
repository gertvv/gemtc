\encoding{utf8}
\name{relative.effect}
\alias{forest.mtc.results}
\alias{relative.effect}
\alias{forest}

\title{Calculating relative effects}
\description{Calculates the relative effects of pairs of treatments.}
\usage{
relative.effect(result, t1, t2 = c(), preserve.extra = TRUE)
}
\arguments{
  \item{result}{An object of S3 class \code{mtc.result} to derive the relative effects from.}
  \item{t1}{A list of baselines to calculate a relative effects against. Will be extended to match the length of t2.}
  \item{t2}{A list of treatments to calculate the relative effects for. Will be extended to match the length of t1.
If left empty and t1 is a single treatment, relative effects of all treatments except t1 will be calculated.}
  \item{preserve.extra}{Indicates whether to preserve extra parameters such as the sd.w and sd.d.}
}
\value{
  Returns an \code{mtc.results} object containing the calculated relative effects.
}
\author{Gert van Valkenhoef, Joël Kuiper}
\seealso{
  \code{\link{rank.probability}}
}
\examples{
file <- system.file("extdata/luades-smoking.gemtc", package="gemtc")
network <- read.mtc.network(file)
model <- mtc.model(network)
# To save computation time we load the samples instead of running the model
\dontrun{results <- mtc.run(model)}
results <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))

# Creates a forest plot of the relative effects
forest(relative.effect(results, "A"))

summary(relative.effect(results, "B", c("A", "C", "D")))
## Iterations = 5001:25000
## Thinning interval = 1 
## Number of chains = 4 
## Sample size per chain = 20000 
## 
## 1. Empirical mean and standard deviation for each variable,
##    plus standard error of the mean:
## 
##          Mean     SD  Naive SE Time-series SE
## d.B.A -0.4901 0.4041 0.0014286       0.002935
## d.B.C  0.3429 0.4158 0.0014700       0.002715
## d.B.D  0.6092 0.4868 0.0017210       0.003421
## sd.d   0.8430 0.1853 0.0006551       0.002481
## 
## 2. Quantiles for each variable:
## 
##          2.5%      25%     50%     75%  97.5%
## d.B.A -1.3054 -0.74746 -0.4840 -0.2249 0.2948
## d.B.C -0.4746  0.07405  0.3419  0.6129 1.1702
## d.B.D -0.3378  0.29034  0.6036  0.9237 1.5823
## sd.d   0.5493  0.71041  0.8203  0.9494 1.2718
}
