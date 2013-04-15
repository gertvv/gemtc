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
\details{
The \code{mtc.model} function generates an object of S3 class \code{mtc.model}, which can be visualized by the generic \code{plot} function or summarized by the generic \code{summary} function.
}
\usage{
mtc.model(network, type = "consistency", factor = 2.5, n.chain = 4, likelihood=NULL, link=NULL)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{network}{An object of S3 class \code{mtc.network}}
  \item{type}{A string literal indicating the type of model (only "consistency" is allowed).}
  \item{factor}{Variance scaling factor for the starting values}
  \item{n.chain}{Number of chains in the model}
  \item{likelihood}{The likelihood to be used. If unspecified, a suitable likelihood will be inferred for the given data.}
  \item{link}{The link function to be used. If unspecified, a suitable link function will be inferred for the given data.}
}
\value{
An object of class \code{mtc.model}.
This is a list with the following elements:
  \item{type}{The type of model} 
  \item{network}{Network the model was generated from}
  \item{tree}{Spanning tree formed by the basic parameters}
  \item{n.chain}{The number of chains}
  \item{var.scale}{The scaling factor used to over-disperse starting values}
  \item{likelihood}{The likelihood used}
  \item{link}{The link function used}
  \item{om.scale}{The scale for the variance parameters}
  \item{code}{Model code in JAGS/BUGS syntax}
  \item{data}{Data in JAGS/BUGS compatible format}
  \item{inits}{Initial values in JAGS/BUGS compatible format}
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
