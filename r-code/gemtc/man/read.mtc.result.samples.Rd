\encoding{utf8}
\name{read.mtc.result.samples}
\alias{read.mtc.result.samples}
\title{Reading previously saved MCMC samples}
\description{
This is a semi-internal method for creating objects of the class \code{mtc.result} from samples written by the \code{dput} method.
}
\usage{
read.mtc.result.samples(file, model, sampler = NULL)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{file}{Path to a file created by calling \code{dput(results$samples)} on a \code{mtc.result} object}
  \item{model}{Reference to a \code{mtc.model} object}
  \item{sampler}{Optional argument to describe the type of sampler used to generate the objects}
}
\value{
An object of class \code{mtc.result}. This is a list with the following elements: 
\item{samples}{The samples resulting from running the MCMC model} 
\item{model}{A reference to the model used to produce the samples}
\item{sampler}{The sampler used to produce the samples}
}

\author{Gert van Valkenhoef, Joël Kuiper}

\seealso{
\code{\link{mtc.run}}, \code{\link{dput}}
}
\examples{
## The function is currently defined as
function (file, model, sampler = NULL) {
    samples <- dget(file)
    results <- list(samples = samples, model = model, sampler = sampler)
    class(results) <- "mtc.result"
}
}
