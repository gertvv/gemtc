\encoding{utf8}
\name{mtc.network.xml}
\alias{read.mtc.network}
\alias{write.mtc.network}

\title{Read and write mtc.network as legacy GeMTC XML files (.gemtc)}
\description{Read and write legacy GeMTC XML (.gemtc) files - only arm-based data with responders/sampleSize or mean/std.dev/sampleSize are supported.}
\usage{
read.mtc.network(file)
write.mtc.network(network, file)
}
\arguments{
  \item{network}{An object of the S3 class \code{mtc.network}.}
  \item{file}{Path to the file to read (\code{read.mtc.network}) or write (\code{write.mtc.network}).
For \code{write.mtc.network}, if \code{file=""}, the output is printed to standard output.  If it is \code{file="|cmd"}, the output is piped to the command given by \code{cmd}. See \code{base::cat} for further details.}
}
\value{
\code{read.mtc.network} returns an object of S3 class \code{mtc.network}.
}
\details{
\code{read.mtc.network} and \code{write.mtc.network} deal with the older GeMTC XML format.
This format supports dichotomous data as responders/sampleSize and continuous data as mean/std.dev/sampleSize only.
Relative effect data, or other forms of arm-based data, are not supported.
In general, it may be more convenient to use native R methods to read and write networks (e.g. dput/dget or save/load).

These functions need the \code{XML} package to be installed.
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

# Write the network to a temporary file in the GeMTC XML format
tmp <- tempfile()
write.mtc.network(network, file=tmp)
}
