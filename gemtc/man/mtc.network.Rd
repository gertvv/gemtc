\encoding{utf8}
\name{mtc.network}
\alias{mtc.network}
\alias{read.mtc.network}
\alias{write.mtc.network}

\title{Create an mtc.network}
\description{Creates an object of class \code{mtc.network}}
\usage{
mtc.network(data, treatments, description, data.re)
read.mtc.network(file)
write.mtc.network(network, file)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{data}{Arm-level data.
A data frame defining the arms of each study, containing the columns `study' and `treatment', where `treatment' must refer to an existing treatment ID if treatments were specified.
To specify a network with dichotomous data, the number of responders should be given in a column `responders' and the sample size in a column `sampleSize'.
To specify a network with continuous data, the mean should be given in a column `mean', the standard deviation in a column `std.dev' and the sample size in a column `sampleSize'.}
  \item{data.re}{Relative effect data.
A data frame defining the arms of each study, containing the columns `study' and `treatment', where `treatment' must refer to an existing treatment ID if treatments were specified.
The column `diff' specifies the mean difference between the current arm and the baseline arm; set `diff=NA' for the baseline arm.
The column `std.err' specifies the standard error of the mean difference (for non-baseline arms).
For trials with more than two arms, specify the standard error of the mean of the baseline arm in `std.err', as this determines the covariance of the differences.
}
  \item{treatments}{Optional.
A data frame with columns `id' and `description' defining the treatments or a vector giving the treatment IDs.}
  \item{description}{Optional. Short description of the network.}

  \item{network}{An object of the S3 class \code{mtc.network}.}
  \item{file}{Path to the file to read (\code{read.mtc.network}) or write (\code{write.mtc.network}).
For \code{write.mtc.network}, if \code{file=""}, the output is printed to standard output.  If it is \code{file="|cmd"}, the output is piped to the command given by \code{cmd}. See \code{base::cat} for further details.}
}
\value{
For \code{mtc.network} and \code{read.mtc.network}, an object of the class \code{mtc.network} which is a list containing:
  \item{description}{A short description of the network}
  \item{treatments}{A data frame describing the treatments}
  \item{data}{A data frame containing the network data}
These are cleaned up and standardized versions of the arguments provided, or generated defaults for `treatments' if the argument was omitted.
}
\author{Gert van Valkenhoef, Joël Kuiper}

\seealso{
\code{\link{read.mtc.network}}
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
