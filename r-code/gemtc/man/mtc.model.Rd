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
mtc.model(network, type = "Consistency", factor = 2.5, n.chain = 4)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{network}{An object of S3 class \code{mtc.network}}
  \item{type}{A string literal indicating whether the model should be
  "consistency" or "inconsistency"}
  \item{factor}{Variance scaling factor for the starting values}
  \item{n.chain}{Number of chains in the model}
}
\value{
An object of class \code{mtc.model}.
This is a list with the following elements:
  \item{type}{The type of model} 
  \item{description}{A short description of the model} 
  \item{n.chain}{Number of chains}
  \item{var.scale}{The previously supplied scaling factor}
  \item{j.network}{The Java network object (internal)}
  \item{j.model}{The Java model object (internal)}
  \item{j.generator}{The Java starting value generator object (internal)}
}
\note{
The \code{mtc.model} object contains references to Java objects.
These can \emph{not} currently be restored from a saved R session.
}

\section{Visualization}{Calling the generic plot method on an S3 \code{mtc.model} object
will show a graph with the treatments as vertices and the comparisons as edges.
The solid arrows represent basic parameters, the dotted arrows represent inconsistency factors and solid lines represent comparisons that are not associated with any parameter but do have direct evidence from trials.

\if{html}{
The example code will generate the following graph:

\figure{parkinson-inconsistency.png}{Graph showing structure of inconsistency model generated from parkison example data}
}
\if{latex}{
The example code will generate the following graph:

\figure{parkinson-inconsistency.pdf}{options: width=3in}
}

Note that the layout algorithm used in visualization is non-deterministic and might produce different results on each run.
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
model <- mtc.model(network, "incons")
plot(model)
summary(model)
}
