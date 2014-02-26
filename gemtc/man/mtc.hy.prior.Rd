\encoding{utf8}
\name{mtc.hy.prior}
\alias{mtc.hy.prior}
\alias{mtc.hy.empirical.lor}

\title{Set priors for the heterogeneity parameter}
\description{
These functions generate priors for the heterogeneity parameter in \code{\link{mtc.model}}. 
Priors can be set explicitly or, for outcomes on the log odds-ratio scale, based on empirical research.
}
\usage{
mtc.hy.prior(type, distr, ...)

mtc.hy.empirical.lor(outcome.type, comparison.type)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{type}{Type of heterogeneity prior: 'std.dev', 'var', or 'prec' for standard deviation, variance, or precision respectively.}
  \item{distr}{Prior distribution name (BUGS/JAGS syntax). Typical ones would be 'dunif' (uniform), 'dgamma' (Gamma), or 'dlnorm' (log-normal).}
  \item{...}{Arguments to the \code{distr}. Can be numerical values or "om.scale" for the estimated outcome measure scale (see \code{\link{mtc.model}})}
  \item{outcome.type}{The type of outcome to get an empirical prior for. Can be one of 'mortality' (all-cause mortality), 'semi-objective' (e.g. cause-specific mortality, major morbidity event, drop-outs), or 'subjective' (e.g. pain, mental health, dichotomous biomarkers).}
  \item{comparison.type}{The type of comparison to get an empirical prior for. Can be one of 'pharma-control' (pharmacological interventions versus control), 'pharma-pharma' (pharmacological versus pharmacological interventions) and 'non-pharma' (any other comparisons).}
}
\value{
A value to be passed to \code{\link{mtc.model}}. 
}
\details{
The generated prior is a list, the structure of which may change without notice. It can be converted to BUGS compatible code using \code{as.character}.

Empirical priors for the log odds-ratio (LOR) are taken from \link[=gemtc-package]{[Turner et al. 2012]}.
}
\author{Gert van Valkenhoef}
\seealso{
\code{\link{mtc.model}}
}
\examples{
file <- system.file("extdata/luades-smoking.gemtc", package="gemtc")
network <- read.mtc.network(file)

# NOTE: the mtc.run commands below are for illustrative purposes, such a small
# number of iterations should obviously not be used in practice.

# set a uniform prior standard deviation
model1 <- mtc.model(network, hy.prior=mtc.hy.prior("std.dev", "dunif", 0, 2))
result <- mtc.run(model1, n.adapt=10, n.iter=10)

# set an empirical (log-normal) prior on the variance
model2 <- mtc.model(network, hy.prior=mtc.hy.empirical.lor("subjective", "non-pharma"))
result <- mtc.run(model2, n.adapt=10, n.iter=10)

# set a gamma prior on the precision
model3 <- mtc.model(network, hy.prior=mtc.hy.prior("prec", "dgamma", 0.01, 0.01))
result <- mtc.run(model3, n.adapt=10, n.iter=10)
}
