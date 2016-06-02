\encoding{utf8}
\name{mtc.deviance}
\alias{mtc.deviance}
\alias{plot.mtc.deviance}
\alias{mtc.devplot}
\alias{mtc.levplot}

\title{Inspect residual deviance}
\description{Inspect the posterior residual deviance and summarize it using plots}
\usage{
mtc.deviance(result)

mtc.devplot(x, ...)
mtc.levplot(x, ...)

\method{plot}{mtc.deviance}(x, auto.layout=TRUE, ...)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{result}{An object of class \code{\link{mtc.result}}.}
  \item{x}{An object of class \code{\link{mtc.deviance}}.}
  \item{auto.layout}{If \code{TRUE}, the separate plots will be shown as panels on a single page.}
  \item{...}{Graphical parameters.}
}
\value{
\code{mtc.deviance} returns the deviance statistics of a \code{mtc.result}.
}
\details{
\code{mtc.devplot} will generate a stem plot of the posterior deviance per arm (if there are only arm-based data) or the mean per data point deviance per study (if there are contrast-based data).

\code{mtc.levplot} will plot the leverage versus the square root of the residual deviance (mean per data point for each study).

The generic plot function will display both on a single page (unless \code{auto.layout=FALSE}).
}
\author{Gert van Valkenhoef}

\seealso{
\code{\link{mtc.run}}
}
