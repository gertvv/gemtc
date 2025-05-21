\encoding{utf8}
\name{mtc.model}
\alias{mtc.model}
\alias{plot.mtc.model}
\alias{summary.mtc.model}
\alias{print.mtc.model}

\title{Generate network meta-analysis models}
\description{
The \code{mtc.model} function generates network meta-analysis models from an \code{mtc.network} object.
}
\usage{
mtc.model(network, type = "consistency", factor = 2.5, n.chain = 4,
  likelihood=NULL, link=NULL, linearModel="random",
  om.scale=NULL, hy.prior=mtc.hy.prior("std.dev", "dunif", 0, "om.scale"),
  re.prior.sd=15 * om.scale, dic=TRUE, powerAdjust=NA, ...)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{network}{An object of S3 class \code{mtc.network}}
  \item{type}{A string literal indicating the type of model (allowed values are "consistency", "regression", "nodesplit", "ume", or "use").}
  \item{factor}{Variance scaling factor for the starting values}
  \item{n.chain}{Number of chains in the model}
  \item{likelihood}{The likelihood to be used. If unspecified, a suitable likelihood will be inferred for the given data.}
  \item{link}{The link function to be used. If unspecified, a suitable link function will be inferred for the given data.}
  \item{linearModel}{The type of linear model to be generated. Can be "random" for a random effects model, or "fixed" for a fixed effect model.}
  \item{om.scale}{Outcome measure scale. Represents a "very large" difference on the analysis' outcome scale. This is used to set vague priors. For the log odds-ratio, values between 2 and 5 are considered reasonable. For continuous outcomes, this depends heavily on the specific outcome. If left unspecified, it is determined from the data.}
  \item{hy.prior}{Heterogeneity prior. See \code{\link{mtc.hy.prior}}.}
  \item{re.prior.sd}{Standard deviation for the relative effects prior (normal distribution).}
  \item{dic}{When set to TRUE, deviance and fitted values will be monitored to allow computation of the Deviance Information Criterion (DIC) at residual.}
  \item{powerAdjust}{Optional: the name of a column in the studies data frame of the \code{mtc.network}. This column must contain values between 0 and 1. The likelihood for each study will be adjusted by inflating the variance, where 0 means the study is excluded and 1 means it receives full weight. See details for more.}
  \item{...}{Additional arguments to be passed to the type-specific model generation function.}
}
\value{
An object of class \code{mtc.model}.
The following elements are descriptive:
  \item{type}{The type of model}
  \item{network}{Network the model was generated from}
  \item{tree}{Spanning tree formed by the basic parameters}
  \item{var.scale}{The scaling factor used to over-disperse starting values}
  \item{likelihood}{The likelihood used}
  \item{link}{The link function used}
  \item{om.scale}{The scale for the variance parameters}
  \item{regressor}{Regressor specification (regression models only): includes additional elements "center" and "scale" describing how the regressor input was standardized}
These elements determine the model run by JAGS:
  \item{n.chain}{The number of chains}
  \item{code}{Model code in JAGS syntax. Use \code{cat()} for proper formatting.}
  \item{data}{Data in JAGS compatible format}
  \item{inits}{Initial values in JAGS compatible format}
  \item{monitors}{The nodes of the JAGS model to monitor}
  \item{dic}{Whether to compute the DIC}
These latter fields can be modified to alter the statistical model, but such changes may break the model or assumptions made elsewhere in the package.
}
\details{
The \code{mtc.model} function generates an object of S3 class \code{mtc.model}, which can be
visualized by the generic \code{plot} function or summarized by the generic \code{summary} function.

These likelihood/links are supported:
\itemize{
  \item{normal/identity: for continuous (mean difference) data.

  Required columns: \code{[mean, std.err]} or \code{[mean, std.dev, sampleSize]}.

  Result: relative mean difference.}
  \item{binom/logit: for dichotomous data.

  Required columns \code{[responders, sampleSize]}.

  Result: (log) odds ratio.}
  \item{binom/log: for dichotomous data.

  Required columns \code{[responders, sampleSize]}.

  Result: (log) risk ratio.}
  \item{binom/cloglog: for rate (survival) data - equal follow-up in each arm.

  Required columns \code{[responders, sampleSize]}.

  Result: (log) hazard ratio.}
  \item{poisson/log: for rate (survival) data.

  Required columns \code{[responders, exposure]}.

  Result: (log) hazard ratio.}
}
Most likelihood/links follow \link[=gemtc-package]{[Dias et al. 2013a]}, and the binom/log model follows \link[=gemtc-package]{[Warn et al. 2002]}.

The following model types are supported:
\itemize{
  \item{\code{consistency}: ordinary consistency model. No additional parameters. \link[=gemtc-package]{[Dias et al. 2013a, van Valkenhoef et al. 2012]}}
  \item{\code{nodesplit}: node-splitting model. Removes both arms used to estimate the direct evidence from the network of indirect evidence, rather than just one of those arms. This means that three-arm trials do not contribute any evidence in the network of indirect evidence. When relative effect data are present, these are transformed appropriately (using an assumption of normality) to enable this direct/indirect evidence split. Additional parameters: \code{t1} and \code{t2}, which indicate the comparison to be split. \link[=gemtc-package]{[Dias et al. 2010, van Valkenhoef et al. 2015]}}
  \item{\code{regression}: meta-regression model. Additional parameters: \code{regressor}, which indicates how to structure the treatment-interaction model for the regression. See below for details.}
  \item{\code{use}: unrelated study effects. Models the effects within each study as if the studies are independent. No additional parameters. \link[=gemtc-package]{[van Valkenhoef et al. (draft)]}}
  \item{\code{ume}: unrelated mean effects. Models the effects within each comparison as if they are independent. Does not properly handle multi-arm trials, and warns when they are present in the network. No additional parameters. \link[=gemtc-package]{[Dias et al. 2013b, van Valkenhoef et al. (draft)]}}
}

Regressor specification: a list with elements: \code{variable}, \code{coefficient}, and either \code{control} or \code{classes}.
The \code{variable} is the name of the covariate to include in the regression analysis, and must be a column of the studies data frame in the network.
The regressor variable is automatically centered and standardized using the method recommended by \link[=gemtc-package]{Gelman et al. (2008)}.
The \code{coefficient} indicates the type of treatment-interaction model: "shared", "unrelated", or "exchangeable".
\code{control}, if specified, must be the ID of a treatment in the network.
All other treatments have a coefficient relative to the control, which can be the same for all treatments ("shared"), different for all treatments ("unrelated") or exchangeable between treatments ("exchangeable").
\code{classes} is a named list of treatment classes, the first of which will act as the control class.
Each class is a vector of treatment IDs from the network.
Only "shared" coefficients can currently be used, meaning a single coefficient per class (except the control class).
See also \code{\link{atrialFibrillation}}, \code{\link{certolizumab}}, and \code{\link{hfPrevention}} for examples of meta-regression analyses.

Studies can be downweighted by using the \code{powerAdjust} argument, which applies a variance inflation (also known as "power prior") to the likelihood.
This allows a weight \eqn{\alpha_i \in [0, 1]}{a[i] in [0, 1])} to be specified for each study \eqn{i}{i}.
The log-likelihood will be multiplied by a factor \eqn{\alpha_i}{a[i]}, or equivalently for normal distributions the variance will be multiplied by \eqn{1/\alpha_i}{1/a[i]}.
Setting \eqn{\alpha_i = 0}{a[i] = 0} will completely exclude that study, whereas setting \eqn{\alpha_i = 1}{a[i] = 1} will weight it fully.
Essentially, down-weighted models modify the data and hence model fit statistics such as DIC can not be compared between models with different weightings.
}

\section{Visualization}{Calling the generic plot method on an S3 \code{mtc.model} object
will show a graph with the treatments as vertices and the comparisons as edges.
The lines with solid arrows represent basic parameters, and the other lines represent comparisons that are not associated with any parameter but do have direct evidence from trials.

\if{html}{
The example code will generate the following graph:

\figure{parkinson.png}{Graph showing structure of a consistency model generated from parkison example data}
}
\if{latex}{
The example code will generate the following graph:

\figure{parkinson.pdf}{options: width=3in}
}
The default layout algorithm is \code{igraph::layout.circle}, other layout algorithms can be used by passing them as an optional argument called \code{layout} to \code{plot}.
The \code{igraph::layout.fruchterman.reingold} algorithm also seems to produce nice results and may be better for large graphs.
}

\section{Reproducible results}{
It is possible to get reproducible results, but as JAGS uses its own pseudo-random number generator, this is somewhat more involved.

\code{mtc.model} generates random numbers using the R functions (for example to generate initial values). Therefore, \code{set.seed} must be called before calling \code{mtc.model}.

Then, before calling \code{mtc.run}, the random number generator type and seed must be set for each of the chains. This can be done as documented in \code{jags.model}, by setting the \code{.RNG.name} and \code{.RNG.seed} for each chain in \code{model$inits}. See below for an example.}


\author{Gert van Valkenhoef, Joël Kuiper}
%% ~Make other sections like Warning with \section{Warning }{....} ~

\seealso{
%% ~~objects to See Also as \code{\link{help}}, ~~~
\code{\link{mtc.network}}, \code{\link{mtc.run}}
}
\examples{
# Random effects consistency model for Parkinson network
model <- mtc.model(parkinson)
plot(model)
summary(model)

# Fixed effect meta-regression for heart failure prevention
regressor <- list(coefficient='shared',
                  variable='secondary',
                  control='control')

model <- mtc.model(hfPrevention,
                   type="regression",
                   regressor=regressor,
                   linearModel="fixed")

# Reproducible results
# Set the R RNG seed
set.seed(42)
model <- mtc.model(parkinson, likelihood='normal', link='identity')
# By default, the model will have 4 chains - generate a seed for each
seeds <- sample.int(4, n = .Machine$integer.max)
# Apply JAGS RNG settings to each chain
model$inits <- mapply(c, model$inits, list(
  list(.RNG.name="base::Wichmann-Hill", .RNG.seed=seeds[1]),
  list(.RNG.name="base::Marsaglia-Multicarry", .RNG.seed=seeds[2]),
  list(.RNG.name="base::Super-Duper", .RNG.seed=seeds[3]),
  list(.RNG.name="base::Mersenne-Twister", .RNG.seed=seeds[4])), SIMPLIFY=FALSE)
}
