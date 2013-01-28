\encoding{utf8}
\name{forest}
\alias{forest}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{Creating forest plots for results}
\description{The function forest can be used to create forest plots of mtc.result objects.}

\usage{
forest(x,...) 
}

\arguments{
  \item{x}{The result object of S3 class \code{mtc.result} to create a forest plot of}
  \item{...}{Additional arguments will be ignored}
}

\seealso{\code{\link{mtc.network}}, \code{\link{read.mtc.network}}, \code{\link{write}}}

\author{Gert van Valkenhoef, Joël Kuiper}
\examples{
file <- system.file("extdata/luades-thrombolytic.gemtc", package="gemtc")
network <- read.mtc.network(file)
model <- mtc.model(network, n.chain=2)

# Using BUGS or JAGS is preferred over YADAS, 
# however YADAS does not require additional dependencies.
\dontrun{
results <- mtc.run(model, "YADAS", n.iter=10000, n.adapt=2500, thin=10)
}
results <- mtc.run(model, "YADAS", n.iter=1000, n.adapt=250, thin=1)
summary(results)

forest(results) 

# Creates a forest plot of the relative effects
forest(relative.effect(results, "tPA"))
}
