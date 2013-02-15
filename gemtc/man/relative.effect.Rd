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
## Iterations = 5010:25000
## Thinning interval = 10 
## Number of chains = 4 
## Sample size per chain = 2000 
## 
## 1. Empirical mean and standard deviation for each variable,
##    plus standard error of the mean:
## 
##          Mean     SD Naive SE Time-series SE
## d.B.A -0.4965 0.4081 0.004563       0.004989
## d.B.C  0.3394 0.4144 0.004634       0.004859
## d.B.D  0.6123 0.4789 0.005354       0.005297
## sd.d   0.8465 0.1913 0.002139       0.002965
## 
## 2. Quantiles for each variable:
## 
##          2.5%     25%     50%     75%  97.5%
## d.B.A -1.3407 -0.7530 -0.4910 -0.2312 0.2985
## d.B.C -0.4809  0.0744  0.3411  0.5977 1.1702
## d.B.D -0.3083  0.3005  0.6044  0.9152 1.5790
## sd.d   0.5509  0.7119  0.8180  0.9542 1.2827
}
