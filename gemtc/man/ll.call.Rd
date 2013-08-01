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
  \item{\code{mtc.code.likelihood}: generates JAGS/BUGS code implementing the likelihood.}
  \item{\code{scale.log}: returns TRUE if plots should use the log scale.}
  \item{\code{scale.name}: returns the user-facing name of the outcome metric.}
  \item{\code{scale.limit.inits}: returns an upper and lower bound for the initial values, because
  some initial values might trigger boundary conditions such as probability 0 or 1 for the binomial.}
  \item{\code{required.columns.ab}: returns the required columns for arm-based data.}
}
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
