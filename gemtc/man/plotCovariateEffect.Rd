\encoding{utf8}
\name{plotCovariateEffect}
\alias{plotCovariateEffect}
\title{Plot treatment effects versus covariate values}
\description{
The plot will show the median treatment effect and the 95\% credible interval on the y-axis and the covariate value on the x-axis.
One plot page will be generated per pair of treatments.
}
\usage{
plotCovariateEffect(result, t1, t2, xlim=NULL, ylim=NULL,
                    ask=dev.interactive(orNone=TRUE))
}
\arguments{
  \item{result}{Results object - created by \code{mtc.result}}
  \item{t1}{A list of baseline treatments to calculate treatment effects against. Will be extended to match the length of t2.}
  \item{t2}{A list of treatments to calculate the effects for. Will be extended to match the length of t1.
If left empty and t1 is a single treatment, effects of all treatments except t1 will be calculated.}
  \item{xlim}{The x-axis limits.}
  \item{ylim}{The y-axis limits.}
  \item{ask}{If TRUE, a prompt will be displayed before generating the next page of a multi-page plot.}
}
\details{
Default x-axis limits will be set to three standard deviations above and below the centering value of the covariate.
The y-axis limits will be set based on the minimum and maximum 95\% CrI limits among the set of effects computed.
}
\value{
None.
}
\author{Gert van Valkenhoef}
\seealso{
\code{\link{relative.effect}}, \code{\link{mtc.run}}
}
