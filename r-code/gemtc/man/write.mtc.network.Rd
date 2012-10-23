\encoding{utf8}
\name{write.mtc.network}
\alias{write.mtc.network}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{Writing GeMTC Network files}
\description{The function mtc.write.network can be used to write a network to
a GeMTC file. See \url{http://drugis.org/gemtc} for interacting with such files using
a GUI.}

\usage{
write.mtc.network(network, file = "")
}

\arguments{
	\item{network}{The network object of S3 class \code{mtc.network} to write}
	\item{file}{Path to write the file to, if none supplied output will be redirected to the REPL}
}
\author{Gert van Valkenhoef, Joël Kuiper}

\seealso{\code{\link{mtc.network}}, \code{\link{read.mtc.network}}}
%% ~Make other sections like Warning with \section{Warning }{....} ~
\examples{
	file <- system.file("extdata/luades-smoking.gemtc", package="gemtc")
	network <- read.mtc.network(file)
	write.mtc.network(network, file=tempfile())
}
