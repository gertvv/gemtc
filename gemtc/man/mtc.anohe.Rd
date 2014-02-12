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
mtc.anohe(network, ...)
}
\arguments{
  \item{network}{An object of S3 class \code{\link{mtc.network}}.}
  \item{...}{Arguments to be passed to \code{\link{mtc.run}} or \code{\link{mtc.model}}. This can be used to set the likelihood/link or the number of iterations, for example.}
}
\details{
Analysis of heterogeneity is intended to be a unified set of statistics and a visual display that allows the simultaneous assessment of both heterogeneity and inconsistency in network meta-analysis \link[=gemtc-package]{[van Valkenhoef et al. 2014b (draft)]}.

\code{mtc.anohe} returns the MCMC results for all three types of model. To get appropriate summary statistics, call \code{summary()} on the results object. The summary can be plotted.

To control parameters of the MCMC estimation, see \code{\link{mtc.run}}.
To specify the likelihood/link or to control other model parameters, see \code{\link{mtc.model}}.
The \code{...} arguments are first matched against \code{\link{mtc.run}}, and those that do not match are passed to \code{\link{mtc.model}}.
}
\note{This method should not be considered stable. It is an experimental feature and heavily work in progress. The interface may change at any time.
}
\value{
For \code{mtc.anohe}:
an object of class \code{mtc.anohe}. This is a list with the following elements: 
\item{result.use}{The result for the USE model (see \code{\link{mtc.run}}).}
\item{result.ume}{The result for the UME model (see \code{\link{mtc.run}}).}
\item{result.cons}{The result for the consistency model (see \code{\link{mtc.run}}).}

For \code{summary}:
an object of class \code{mtc.anohe.summary}. This is a list with the following elements: 
\item{cons.model}{Generated consistency model.}
\item{studyEffects}{Study-level effect summaries (multi-arm trials downweighted).}
\item{pairEffects}{Pair-wise pooled effect summaries (from the UME model).}
\item{consEffects}{Consistency effect summaries.}
\item{indEffects}{Indirect effect summaries (back-calculated).}
\item{isquared.comp}{Per-comparison I-squared statistics.}
\item{isquared.glob}{Global I-squared statistics.}
}
\author{Gert van Valkenhoef, Joël Kuiper}
\seealso{
\code{\link{mtc.model}}
\code{\link{mtc.run}}
}
