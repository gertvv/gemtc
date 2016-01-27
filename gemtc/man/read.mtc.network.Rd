\encoding{utf8}
\name{read.mtc.network}
\alias{read.mtc.network}

\title{Read mtc.network from legacy GeMTC XML files (.gemtc)}
\description{Read legacy GeMTC XML (.gemtc) files.}
\usage{
read.mtc.network(file)
}
\arguments{
  \item{file}{Path to the file to read (\code{read.mtc.network}).}
}
\value{
\code{read.mtc.network} returns an object of S3 class \code{mtc.network}.
}
\details{
\code{read.mtc.network} exists to migrate data from the older GeMTC XML format, and requires the \code{XML} package to be installed.

Support for writing XML files has been removed.
Instead, use native R methods to read and write networks (e.g. save/load or dput/dget).
}
\author{Gert van Valkenhoef}
\seealso{
\code{\link{mtc.network}}
}
\examples{
# Read an example GeMTC XML file
file <- system.file("extdata/luades-smoking.gemtc", package="gemtc")
network <- read.mtc.network(file)

# Summarize the network (generate some interesting network properties)
summary(network)
## $Description
## [1] "MTC dataset: Smoking cessation rates"
## 
## $`Studies per treatment`
##  A  B  C  D 
## 19  6 19  6 
## 
## $`Number of n-arm studies`
## 2-arm 3-arm 
##    22     2 
}
