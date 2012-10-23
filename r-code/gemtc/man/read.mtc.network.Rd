\encoding{utf8}
\name{read.mtc.network}
\alias{read.mtc.network}
\alias{summary.mtc.network}
\alias{plot.mtc.network}
\alias{print.mtc.network}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{Reading GeMTC Network files}
\description{ The function mtc.read.network can be used to read existing GeMTC
files. See \url{http://drugis.org/gemtc} for creating such files using a GUI.  }

\usage{
read.mtc.network(file)
}

\arguments{
  \item{file}{Path to a valid GeMTC file}
}
\value{
  Reading a network will return an object of class \code{mtc.network}. This is a list with the following elements:
  \item{description}{A short description of the model} 
  \item{treatments}{A list of included treatments in the network. Treatments have an \code{id} and \code{description}.}
  \item{data}{A data frame of lists containing a study, treatment combination with an amount of responders and sample size for dichotomous networks and a mean, std.dev. and sample size for continuous networks}
}
\author{Gert van Valkenhoef, Joël Kuiper}

\seealso{\code{\link{mtc.network}}, \code{\link{write.mtc.network}}}
%% ~Make other sections like Warning with \section{Warning }{....} ~
\examples{
file <- system.file("extdata/luades-smoking.gemtc", package="gemtc")
network <- read.mtc.network(file)
print(network$treatments)
##    id           description
##  A  A            No Contact
##  B  B             Self-help
##  C  C Individual Counseling
##  D  D      Group Counseling
plot(network)
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
