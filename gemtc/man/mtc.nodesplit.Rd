\encoding{utf8}
\name{mtc.nodesplit}
\alias{mtc.nodesplit}
\alias{mtc.nodesplit.comparisons}
\alias{plot.mtc.nodesplit}
\alias{print.mtc.nodesplit}
\alias{summary.mtc.nodesplit}
\alias{plot.mtc.nodesplit.summary}
\alias{print.mtc.nodesplit.summary}

\title{Node-splitting analysis of inconsistency}
\description{
Generate and run an ensemble of node-splitting models, results of which can be jointly summarized and plotted.
}
\usage{
mtc.nodesplit(network, comparisons=mtc.nodesplit.comparisons(network), ...)
mtc.nodesplit.comparisons(network)
}
\arguments{
  \item{network}{An object of S3 class \code{\link{mtc.network}}.}
  \item{comparisons}{Data frame specifying the comparisons to be split. The frame has two columns: 't1' and 't2'.}
  \item{...}{Arguments to be passed to \code{\link{mtc.run}} or \code{\link{mtc.model}}. This can be used to set the likelihood/link or the number of iterations, for example.}
}
\details{
\code{mtc.nodesplit} returns the MCMC results for all relevant node-splitting models \link[=gemtc-package]{[van Valkenhoef et al. 2014a (draft)]}. To get appropriate summary statistics, call \code{summary()} on the results object. The summary can be plotted.
See \code{\link{mtc.model}} for details on how the node-splitting models are generated.

To control parameters of the MCMC estimation, see \code{\link{mtc.run}}.
To specify the likelihood/link or to control other model parameters, see \code{\link{mtc.model}}.
The \code{...} arguments are first matched against \code{\link{mtc.run}}, and those that do not match are passed to \code{\link{mtc.model}}.

\code{mtc.nodesplit.comparisons} returns a data frame enumerating all comparisons that can reasonably be split (i.e. have independent indirect evidence).
}
\value{
For \code{mtc.nodesplit}:
an object of class \code{mtc.nodesplit}. This is a list with the following elements: 
\item{d.X.Y}{For each comparison (t1=X, t2=Y), the MCMC results}
\item{consistency}{The consistency model results}

For \code{summary}:
an object of class \code{mtc.nodesplit.summary}. This is a list with the following elements: 
\item{dir.effect}{Summary of direct effects for each split comparison}
\item{ind.effect}{Summary of indirect effects for each split comparison}
\item{cons.effect}{Summary of consistency model effects for each split comparison}
\item{p.value}{Inconsistency p-values for each split comparison}
\item{cons.model}{The generated consistency model}
}
\author{Gert van Valkenhoef, Joël Kuiper}
\seealso{
\code{\link{mtc.model}}
\code{\link{mtc.run}}
}
\examples{
# Read the network
fileName <- system.file('extdata/parkinson.gemtc', package='gemtc')
network <- read.mtc.network(fileName)

# Run all relevant node-splitting models
\dontrun{ result.ns <- mtc.nodesplit(network, thin=50) }
# (read results from file instead of running:)
result.ns <- readRDS(system.file('extdata/parkinson.ns.rds', package='gemtc'))

# List the individual models 
names(result.ns)

# Time series plots and convergence diagnostics for d.A.C model
plot(result.ns$d.A.C)
gelman.diag(result.ns$d.A.C, multivariate=FALSE)

# Overall summary and plot
summary.ns <- summary(result.ns)
print(summary.ns)
plot(summary.ns)
}
