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
  likelihood=NULL, link=NULL, linearModel="random", ...)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{network}{An object of S3 class \code{mtc.network}}
  \item{type}{A string literal indicating the type of model (either "consistency", "ume", or "use").}
  \item{factor}{Variance scaling factor for the starting values}
  \item{n.chain}{Number of chains in the model}
  \item{likelihood}{The likelihood to be used. If unspecified, a suitable likelihood will be inferred for the given data.}
  \item{link}{The link function to be used. If unspecified, a suitable link function will be inferred for the given data.}
  \item{linearModel}{The type of linear model to be generated. Can be "random" for a random effects
model, or "fixed" for a fixed effect model.}
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
These elements determine the model run by JAGS/BUGS:
  \item{n.chain}{The number of chains}
  \item{code}{Model code in JAGS/BUGS syntax. Use \code{cat()} for proper formatting.}
  \item{data}{Data in JAGS/BUGS compatible format}
  \item{inits}{Initial values in JAGS/BUGS compatible format}
  \item{monitors}{The nodes of the JAGS/BUGS model to monitor}
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
  \item{binom/cloglog: for rate (survival) data - equal follow-up in each arm.

  Required columns \code{[responders, sampleSize]}.

  Result: (log) hazard ratio.}
  \item{poisson/log: for rate (survival) data.

  Required columns \code{[responders, exposure]}.

  Result: (log) hazard ratio.}
}
}
\note{
Due to various issues, rJava has been removed from the gemtc package.
Unfortunately, inconsistency models have not been re-implemented, and are unsupported by the current version of the package.
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
The default up-to version 0.1-2 was \code{igraph::layout.kamada.kawai} but could produce overlapping edges.
}


\author{Gert van Valkenhoef, Joël Kuiper}
%% ~Make other sections like Warning with \section{Warning }{....} ~

\seealso{
%% ~~objects to See Also as \code{\link{help}}, ~~~
\code{\link{mtc.network}}, \code{\link{mtc.run}}
}
\examples{
file <- system.file("extdata/parkinson.gemtc", package="gemtc")
network <- read.mtc.network(file)
model <- mtc.model(network)
plot(model)
summary(model)
}
