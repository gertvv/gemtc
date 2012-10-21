\encoding{utf8}
\name{relative.effect}
\alias{relative.effect}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{Calculating relative effects}
\description{Calculates the relative effect of a treatment against a baseline. 
}
\usage{
relative.effect(result, t1, t2 = c(), preserve.extra = TRUE)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{result}{An object of class S3 'mtc.result' to derive the relative effect from}
  \item{t1}{A list of baselines to calculate a relative effect from}
  \item{t2}{A list of treatments to calculate the relative effect against a baseline from. If none supplied or an empty list all relevant treatments will be calculated}
  \item{preserve.extra}{Indicates whether to preserve the extra fields such as the sd.w and sd.d}
}
\value{
	Returns a 'mcmc.list' containing objects of type 'mcmc' for each of chains for the relative effects.
}
\author{Gert van Valkenhoef, Joël Kuiper}

%% ~Make other sections like Warning with \section{Warning }{....} ~

\seealso{
	\code{\link{rank.probability}}
}
\examples{
	file <- system.file("extdata/luades-smoking.gemtc", package="gemtc")
	network <- read.mtc.network(file)
	model <- mtc.model(network)
	results <- mtc.run(model, "YADAS")
	summary(relative.effect(results, "B", c("A", "C", "D")))
	
	## Iterations = 5001:25000
	## Thinning interval = 1 
	## Number of chains = 4 
	## Sample size per chain = 20000 
	##
	## 1. Empirical mean and standard deviation for each variable,
	## 	 plus standard error of the mean:
	##
	## 				 Mean     SD  Naive SE Time-series SE
	## d.B.A -0.4972 0.4074 0.0014405       0.005862
	## d.B.C  0.3418 0.4154 0.0014686       0.004880
	## d.B.D  0.6054 0.4892 0.0017295       0.006443
	## sd.d   0.8500 0.1905 0.0006736       0.004591
	##
	## 2. Quantiles for each variable:
	##
	## 				 2.5%      25%     50%     75%  97.5%
	## d.B.A -1.3264 -0.75931 -0.4856 -0.2265 0.2807
	## d.B.C -0.4840  0.07435  0.3415  0.6091 1.1662
	## d.B.D -0.3600  0.28917  0.5979  0.9214 1.5823
	## sd.d   0.5531  0.71509  0.8253  0.9568 1.2928
}
