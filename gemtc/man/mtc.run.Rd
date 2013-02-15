\encoding{utf8}
\name{mtc.run}
\alias{mtc.run}
\alias{summary.mtc.result}
\alias{plot.mtc.result}
\alias{forest.mtc.result}
\alias{print.mtc.result}
\alias{as.mcmc.list.mtc.result}

\title{Running an \code{mtc.model} using an MCMC sampler}
\description{
The function \code{mtc.run} is used to generate samples from a object of type \code{mtc.model} using a MCMC sampler.
The resulting \code{mtc.results} object can be coerced to an \code{mcmc.list} for further analysis of the dataset using the \code{coda} package.
}
\usage{
mtc.run(model, sampler = NA, n.adapt = 5000, n.iter = 20000, thin = 1)
}
\arguments{
  \item{model}{An object of S3 class \code{mtc.model} describing a network meta-analysis model.}
  \item{sampler}{
A string literal indicating which sampler to use.
Allowed values are \code{'JAGS'} or \code{'BUGS'}.
Alternatively, the specific package to be used can be specified: \code{'rjags'}, \code{'BRugs'} or \code{'R2WinBUGS'}.
When unspecified it will try \code{'rjags'}, \code{'BRugs'}, and \code{'R2WinBUGS'}, in that order.}
  \item{n.adapt}{Amount of adaptation (or tuning) iterations.}
  \item{n.iter}{Amount of simulation iterations.}
  \item{thin}{Thinning factor.}
}
\value{
An object of class \code{mtc.result}. This is a list with the following elements: 
\item{samples}{The samples resulting from running the MCMC model, in \code{mcmc.list} format.}
\item{model}{The \code{mtc.model} used to produce the samples.}
\item{sampler}{The sampler (R package) used to produce the samples.}

The object can be coerced to an \code{mcmc.list} from the \code{coda} package by the generic S3 method \code{as.mcmc.list}.
}

\section{Analysis of the results}{
Convergence of the model can be assessed using methods from the \code{coda} package.
For example the Brooks-Gelman-Rubin method (\code{coda::gelman.diag}, \code{coda::gelman.plot}).
The \code{summary} also provides useful information, such as the MCMC error and the time series and densities given by \code{plot} should also be inspected.

The \code{forest} function can provide forest plots for \code{mtc.result} objects.
This is especially useful in combination with the \code{\link{relative.effect}} function that can be used to calculate relative effects compared to any baseline for consistency models.
The \code{\link{rank.probability}} function calculates rank probabilities for consistency models.
}

\author{Gert van Valkenhoef, Joël Kuiper}
\seealso{
\code{\link{mtc.model}},
\code{\link{relative.effect}},
\code{\link{rank.probability}},
\code{coda::gelman.diag},
\code{coda::gelman.plot}
}

\examples{
file <- system.file("extdata/luades-smoking.gemtc", package="gemtc")
network <- read.mtc.network(file)
model <- mtc.model(network, n.chain=4)

\dontrun{results <- mtc.run(model, n.iter=20000, n.adapt=5000, thin=1)}
results <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))
summary(results)
## Iterations = 5001:25000
## Thinning interval = 1 
## Number of chains = 4 
## Sample size per chain = 20000 
## 
## 1. Empirical mean and standard deviation for each variable,
##    plus standard error of the mean:
## 
##         Mean     SD  Naive SE Time-series SE
## d.A.B 0.4901 0.4041 0.0014286       0.002935
## d.A.C 0.8330 0.2398 0.0008478       0.002130
## d.A.D 1.0992 0.4382 0.0015493       0.003751
## sd.d  0.8430 0.1853 0.0006551       0.002481
## 
## 2. Quantiles for each variable:
## 
##          2.5%    25%    50%    75% 97.5%
## d.A.B -0.2948 0.2249 0.4840 0.7475 1.305
## d.A.C  0.3781 0.6744 0.8261 0.9848 1.328
## d.A.D  0.2679 0.8063 1.0873 1.3809 2.003
## sd.d   0.5493 0.7104 0.8203 0.9494 1.272


plot(results) # Shows time-series and density plots of the samples
forest(results) # Shows a forest plot
}
