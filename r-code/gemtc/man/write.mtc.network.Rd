\encoding{utf8}
\name{write.mtc.network}
\alias{write.mtc.network}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{Writing GeMTC Network files}
\description{The function mtc.write.network can be used to write a network to
  a GeMTC file.}

\usage{
write.mtc.network(network, file = "")
}

\arguments{
  \item{network}{The network object of S3 class \code{mtc.network} to write}
	\item{file}{Path to write the file to,  If \code{""}, print to the standard output connection.  If it is \code{"|cmd"}, the output is piped to the command given by \code{cmd}}
}

\seealso{\code{\link{mtc.network}}, \code{\link{read.mtc.network}}, \code{\link{write}}}

\author{Gert van Valkenhoef, Joël Kuiper}
\examples{
file <- system.file("extdata/luades-smoking.gemtc", package="gemtc")
network <- read.mtc.network(file)
write.mtc.network(network, file=tempfile())
}
