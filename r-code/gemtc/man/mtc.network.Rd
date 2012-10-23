\encoding{utf8}
\name{mtc.network}
\alias{mtc.network}
\title{Constructing an MTC Network}
\description{Creates an network of class \code{mtc.network} based upon treatments and
data.}
\usage{
mtc.network(description, treatments, data)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{description}{Short description describing the subject of the network}
  \item{treatments}{A list of treatments consisting of a vector of ids and descriptions}
  \item{data}{A data frame of lists containing a study, treatment combination with an amount of responders and sample size for dichotomous networks and a mean, std.dev. and sample size for continuous networks}
}
\value{
A object of the class \code{mtc.network}
%%  ~Describe the value returned
%%  If it is a LIST, use
%%  \item{comp1 }{Description of 'comp1'}
%%  \item{comp2 }{Description of 'comp2'}
%% ...
}
\author{Gert van Valkenhoef, Joël Kuiper}

\seealso{
\code{\link{read.mtc.network}}
}
\examples{
treatments <- list( c('id' = 'A', 'description' = 'tA'),
                    c('id' = 'B', 'description' = 'tB'),
                    c('id' = 'C', 'description' = 'tC'))
data <- list( c('study' = '01', 'treatment' = 'A', 'responders'=2, 'sampleSize'=100),
              c('study' = '01', 'treatment' = 'B', 'responders'=5, 'sampleSize'=100),
              c('study' = '02', 'treatment' = 'B', 'responders'=6, 'sampleSize'=110),
              c('study' = '02', 'treatment' = 'C', 'responders'=1, 'sampleSize'=110),
              c('study' = '03', 'treatment' = 'A', 'responders'=3, 'sampleSize'=60),
              c('study' = '03', 'treatment' = 'C', 'responders'=4, 'sampleSize'=80),
              c('study' = '03', 'treatment' = 'B', 'responders'=7, 'sampleSize'=80))
network <- mtc.network("Example", treatments, data)
plot(network)
}
