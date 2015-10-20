\encoding{utf8}
\name{ll.call}
\alias{ll.call}
\title{Call a likelihood/link-specific function}
\description{
GeMTC implements various likelihood/link combinations.
Functionality specific to the likelihood/link is handled by methods with names ending in
\code{.<likelihood>.<link>}. 
This convenience function calls such methods.
}
\usage{
ll.call(fnName, model, ...)
}
\arguments{
  \item{fnName}{The name of the function to call. See details for available functions.}
  \item{model}{An object of S3 class \code{mtc.model} describing a network meta-analysis model, or a
list containing elements named 'likelihood' and 'link'.}
  \item{...}{Additional arguments to be passed to the function.}
}
\value{
The return value of the called function.
}
\details{
The following methods currently need to be implemented to implement a likelihood/link:
\itemize{
  \item{\code{mtc.arm.mle}: calculates a (corrected) maximum likelihood estimate for an arm-level
  effect. Used to generate starting values.}
  \item{\code{mtc.rel.mle}: calculates a (corrected) maximum likelihood estimate for a relative
  effect. Used to generate starting values.}
  \item{\code{mtc.code.likelihood}: generates JAGS code implementing the likelihood.}
  \item{\code{scale.log}: returns TRUE if plots should use the log scale.}
  \item{\code{scale.name}: returns the user-facing name of the outcome metric.}
  \item{\code{scale.limit.inits}: returns an upper and lower bound for the initial values, because
  some initial values might trigger boundary conditions such as probability 0 or 1 for the binomial.}
  \item{\code{required.columns.ab}: returns the required columns for arm-based data.}
}
The first two methods can now also be used to selectively apply continuity corrections in case the maximum likelihood estimates are used for other purposes. \code{mtc.arm.mle} has an additional \code{k=0.5} argument to specify the correction factor. \code{mtc.rel.mle} has arguments \code{correction.force=TRUE} to force application of the continuity correction even if unnecessary, \code{correction.type="constant"} to specify the type of correction (specify \code{"reciprocal"}) for a correction proportional to the reciprocal of the size of the other arm, and \code{correction.magnitude=1} to specify the (total) magnitude of the correction. These corrections apply only for count data, and will be ignored for continuous likelihood/links.
}
\author{Gert van Valkenhoef}
\seealso{
\code{\link{mtc.model}}
}
\examples{
# The "model" may be a stub.
model <- list(likelihood="poisson", link="log")

ll.call("scale.name", model)
# "Hazard Ratio"

ll.call("mtc.arm.mle", model, c('responders'=12, 'exposure'=80))
#      mean         sd
#-1.8562980  0.1118034
}
