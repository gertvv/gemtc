\encoding{utf8}
\name{mtc.network}
\alias{mtc.network}
\alias{plot.mtc.network}

\title{Create an mtc.network}
\description{Creates an object of class \code{mtc.network}}
\usage{
mtc.network(data.ab=data, treatments=NULL, description="Network",
            data.re=NULL, studies=NULL, data=NULL)

\method{plot}{mtc.network}(x, layout=igraph::layout.circle, dynamic.edge.width=TRUE, ...)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{data.ab}{Arm-level data.
A data frame defining the arms of each study, containing the columns `study' and `treatment', where
`treatment' must refer to an existing treatment ID if treatments were specified.
Further columns define the data per arm, and depend on the likelihood/link to be used.
See \code{\link{mtc.model}} for supported likelihood/links and their data requirements.}
  \item{data.re}{Relative effect data.
A data frame defining the arms of each study, containing the columns `study' and `treatment', where
`treatment' must refer to an existing treatment ID if treatments were specified.
The column `diff' specifies the mean difference between the current arm and the baseline arm; set `diff=NA' for the baseline arm.
The column `std.err' specifies the standard error of the mean difference (for non-baseline arms).
For trials with more than two arms, specify the standard error of the mean of the baseline arm in
`std.err', as this determines the covariance of the differences.}
  \item{treatments}{Optional.
A data frame with columns `id' and `description' defining the treatments or a vector giving the treatment IDs.}
  \item{studies}{Optional.
  A data frame with a `study' column naming the studies and additional columns containing covariate values.}
  \item{description}{Optional. Short description of the network.}
  \item{data}{Deprecated. Arm-level data; automatically assigned to data.ab if it is not specified.
Present for compatibility with older versions.}
  
  \item{x}{An mtc.network object.}
  \item{layout}{An igraph-compatible layout.}
  \item{dynamic.edge.width}{If set to TRUE, dynamically set the edge width based on the number of studies.}
  \item{...}{Additional arguments passed to \code{plot.igraph}.}
}
\value{
For \code{mtc.network}, an object of the class \code{mtc.network} which is a list containing:
  \item{description}{A short description of the network}
  \item{treatments}{A data frame describing the treatments}
  \item{data.ab}{A data frame containing the network data (arm-level)}
  \item{data.re}{A data frame containing the network data (relative effects)}
  \item{studies}{A data frame containing study-level information (covariates)}
These are cleaned up and standardized versions of the arguments provided, or generated defaults for `treatments' if the argument was omitted.
}
\details{
One-arm trials are automatically removed, which results in a warning.

Also see \code{\link{mtc.data.studyrow}} for a convenient way to import data from the one-study-per-row format, which is very popular for BUGS code.
}
\author{Gert van Valkenhoef, Joël Kuiper}

\seealso{
\code{\link{mtc.data.studyrow}},
\code{\link{mtc.model}}
}
\examples{
# Create a new network by specifying all information.
treatments <- read.table(textConnection('
  id  description
  A   "Treatment A"
  B   "Treatment B"
  C   "Treatment C"'), header=TRUE)
data <- read.table(textConnection('
  study  treatment  responders  sampleSize
  01     A          2           100
  01     B          5           100
  02     B          6           110
  02     C          1           110
  03     A          3           60
  03     C          4           80
  03     B          7           80'), header=TRUE)
network <- mtc.network(data, description="Example", treatments=treatments)
plot(network)

# Create a new network by specifying only the data.
data <- read.table(textConnection('
  study  treatment  mean   std.dev  sampleSize
  01     A          -1.12  0.6      15
  01     B          -1.55  0.5      16
  02     A          -0.8   0.7      33
  02     B          -1.1   0.5      31'), header=TRUE)
network <- mtc.network(data)

# Print the network
print(network)
## MTC dataset: Network
##   study treatment  mean std.dev sampleSize
## 1     1         A -1.12     0.6         15
## 2     1         B -1.55     0.5         16
## 3     2         A -0.80     0.7         33
## 4     2         B -1.10     0.5         31
}
