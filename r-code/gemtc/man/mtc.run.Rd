\encoding{utf8}
\name{mtc.run}
\alias{mtc.run}
\alias{summary.mtc.result}
\alias{plot.mtc.result}
\alias{forest.mtc.result}
\alias{print.mtc.result}
\alias{as.mcmc.list.mtc.result}

%- Also NEED an '\alias' for EACH other topic documented here.
\title{Running an \code{mtc.model} using an MCMC sampler}
\description{
The function \code{mtc.run} is used to generate samples from a object of type \code{mtc.model} using a MCMC sampler.
The resulting \code{mtc.results} object can be coerced to an \code{mcmc.list} for further analysis of the dataset using the \code{coda} package.
}
\usage{
mtc.run(model, sampler = NA, n.adapt = 5000, n.iter = 20000, thin = 1)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{model}{A object of S3 class \code{mtc.model} describing a consistency or inconsistency model}
  \item{sampler}{
A string literal indicating which sampler to use.
Allowed values are \code{'JAGS'}, \code{'BUGS'} or \code{'YADAS'}. 
Alternatively, the specific package to be used can be specified: \code{'rjags'}, \code{'BRugs'} or \code{'R2WinBUGS'}.
When unspecified it will try JAGS, BUGS and YADAS, in that order.}
  \item{n.adapt}{Amount of adaptation (or tuning) iterations}
  \item{n.iter}{Amount of simulation iterations}
  \item{thin}{Thinning factor}
}
\value{
An object of class \code{mtc.result}. This is a list with the following elements: 
\item{samples}{The samples resulting from running the MCMC model} 
\item{model}{The \code{mtc.model} used to produce the samples}
\item{sampler}{The sampler used to produce the samples}

The object can be coerced to an \code{mcmc.list} from the \code{coda} package by the generic S3 method \code{as.mcmc.list}.
}

\section{Analysis of the results}{
Convergence of the model can be assessed using methods from the \code{coda} package.
For example the Brooks-Gelman-Rubin method (\code{gelman.diag}, \code{gelman.plot}).
The \code{summary} also provides useful information, such as the MCMC error and the time series and densities given by \code{plot} should also be inspected.

The \code{\link{forest}} function from the \code{meta} package has been overridden for \code{mtc.result} objects.
This is especially useful in combination with the \code{\link{relative.effect}} function that can be used to calculate relative effects compared to any baseline for consistency models.
The \code{\link{rank.probability}} function calculated rank probabilities for consistency models.
}

\author{Gert van Valkenhoef, Joël Kuiper}
\seealso{
\code{\link{mtc.model}},
\code{\link{relative.effect}},
\code{\link{rank.probability}},
\code{gelman.diag},
\code{gelman.plot}
}

\examples{
file <- system.file("extdata/luades-thrombolytic.gemtc", package="gemtc")
network <- read.mtc.network(file)
model <- mtc.model(network, n.chain=2)

# Using BUGS or JAGS is preferred over YADAS, 
# however YADAS does not require additional dependencies.
\dontrun{
results <- mtc.run(model, "YADAS", n.iter=10000, n.adapt=2500, thin=10)
}
results <- mtc.run(model, "YADAS", n.iter=1000, n.adapt=250, thin=1)
summary(results)

## Iterations = 2501:12491
## Thinning interval = 10 
## Number of chains = 2 
## Sample size per chain = 1000 
## 
## 1. Empirical mean and standard deviation for each variable,
##    plus standard error of the mean:
## 
##                   Mean      SD Naive SE Time-series SE
## d.ASPAC.AtPA -0.304820 0.18735 0.004189       0.016522
## d.ASPAC.SK   -0.052619 0.12632 0.002825       0.009398
## d.ASPAC.tPA  -0.082183 0.14001 0.003131       0.009953
## d.AtPA.Ret    0.056672 0.13778 0.003081       0.006673
## d.AtPA.SKtPA  0.182576 0.17255 0.003858       0.008097
## d.AtPA.Ten    0.003885 0.18986 0.004245       0.005752
## d.AtPA.UK    -0.010540 0.25126 0.005618       0.020747
## sd.d          0.144836 0.09832 0.002198       0.009121
## 
## 2. Quantiles for each variable:
## 
##                  2.5%      25%        50%       75%    97.5%
## d.ASPAC.AtPA -0.80131 -0.37424 -0.2633908 -0.194203 -0.02693
## d.ASPAC.SK   -0.36079 -0.10434 -0.0446136  0.021031  0.18066
## d.ASPAC.tPA  -0.41917 -0.14239 -0.0607428  0.002906  0.13709
## d.AtPA.Ret   -0.22937 -0.01136  0.0543854  0.132491  0.34123
## d.AtPA.SKtPA -0.12126  0.08231  0.1607389  0.257809  0.59299
## d.AtPA.Ten   -0.36726 -0.08907  0.0006722  0.096692  0.38692
## d.AtPA.UK    -0.50267 -0.17332 -0.0040873  0.165143  0.48285
## sd.d          0.02821  0.07322  0.1186668  0.192236  0.40152

plot(results) # Shows time-series and density plots of the samples
forest(results) # Shows a forest plot
}
