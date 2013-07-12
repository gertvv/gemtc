\encoding{utf8}
\name{mtc.anohe}
\alias{mtc.anohe}
\alias{plot.mtc.anohe}
\alias{print.mtc.anohe}
\alias{summary.mtc.anohe}
\alias{plot.mtc.anohe.summary}
\alias{print.mtc.anohe.summary}

\title{Analysis of heterogeneity (ANOHE)}
\description{
(EXPERIMENTAL)
Generate an analysis of heterogeneity for the given network. Three types of model are estimated: unrelated study effects, unrelated mean effects, and consistency. Output of the \code{summary} function can passed to \code{plot} for a visual representation.
}
\usage{
anohe <- mtc.anohe(network, likelihood=NULL, link=NULL, ...)
anohe.summary <- summary(anohe)
print(summary)
plot(summary)
}
\arguments{
  \item{model}{An object of S3 class \code{\link{mtc.network}}.}
  \item{likelihood}{The likelihood to be used (see \code{\link{mtc.model}}).}
  \item{link}{The link function to be used (see \code{\link{mtc.model}}).}
  \item{...}{Arguments to be passed to \code{\link{mtc.run}}}
}
\value{
For \code{mtc.anohe}:
an object of class \code{mtc.anohe}. This is a list with the following elements: 
\item{result.use}{The result for the USE model (see \code{\link{mtc.run}}).}
\item{result.ume}{The result for the UME model (see \code{\link{mtc.run}}).}
\item{result.cons}{The result for the consistency model (see \code{\link{mtc.run}}).}

For \code{summary}:
an object of class \code{mtc.anohe.summary}. This is a list with the following elements: 
\item{studyEffects}{Study-level effect summaries (multi-arm trials downweighted).}
\item{pairEffects}{Pair-wise pooled effect summaries (from the UME model).}
\item{consEffects}{Consistency effect summaries.}
\item{isquared.comp}{Per-comparison I-squared statistics.}
\item{isquared.glob}{Global I-squared statistics.}
\item{cons.model}{Generated consistency model.}
}
\author{Gert van Valkenhoef, Joël Kuiper}
\seealso{
\code{\link{mtc.run}}
}
