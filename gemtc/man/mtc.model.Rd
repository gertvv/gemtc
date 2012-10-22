\encoding{utf8}
\name{mtc.model}
\alias{mtc.model}
\alias{plot.mtc.model}
\alias{summary.mtc.model}
\alias{print.mtc.model}

\title{Mixed Treatment Comparisons Models}
\description{The function \code{mtc.model} is used to create a Mixed Treatment
Comparison model based upon a S3 \code{mtc.network} object.

A mtc.model object may be visualized by the generic plot function or summarized
by the generic summary function.}
\usage{
mtc.model(network, type = "Consistency", factor = 2.5, n.chain = 4)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
	\item{network}{A object of S3 class \code{mtc.network}}
  \item{type}{A string literal indicating whether the model should be
  consistency or inconsistency. Node-split is not yet supported}
	\item{factor}{Variance scaling factor for the starting values}
	\item{n.chain}{Number of chains in the model}
}
\value{
  An object of class \code{mtc.model}. This is a list with the following elements:
  \item{type}{The type of model, if none supplied default is 'Consistency'} 
  \item{description}{A short description of the model} 
  \item{j.network}{Reference to the internal Java network object}
  \item{j.model}{Reference to the internal Java model object}
  \item{j.generator}{Reference to the internal Java starting value generator object}
  \item{n.chain}{Number of chains}
  \item{var.scale}{The previously suplied scaling factor}
}

\section{Visualization}{Calling the generic plot method on an S3 \code{mtc.model} object
will show a graph with the treatments as vertices and the comparisons as edges.
The comparisons between basic parameters are shown as a directed acyclic graph
(DAG) in green, for inconsistency models the added w.factors are shown in red.}

\author{Gert van Valkenhoef, Joël Kuiper}
%% ~Make other sections like Warning with \section{Warning }{....} ~

\seealso{
%% ~~objects to See Also as \code{\link{help}}, ~~~
\code{\link{mtc.network}}, \code{\link{mtc.run}}
}
\examples{
	file <- system.file("extdata/luades-thrombolytic.gemtc", package="gemtc")
	network <- read.mtc.network(file)
	model <- mtc.model(network)
	plot(model)
	print(model)

}
