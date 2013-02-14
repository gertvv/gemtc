\encoding{utf8}
\name{mtc.network}
\alias{mtc.network}
\title{Create an mtc.network}
\description{Creates an object of class \code{mtc.network}}
\usage{
mtc.network(data, description, treatments)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{description}{Optional. Short description of the network.}
  \item{treatments}{Optional.
A data frame with columns `id' and `description' defining the treatments or a vector giving the treatment IDs.
Alternatively, a list where each element is a labeled vector defining a single treatment.}
  \item{data}{
A data frame defining the arms of each study, containing the columns `study' and `treatment', where `treatment' must refer to an existing treatment ID if treatments were specified.
To specify a network with dichotomous data, the number of responders should be given in a column `responders' and the sample size in a column `sampleSize'.
To specify a network with continuous data, the mean should be given in a column `mean', the standard deviation in a column `std.dev' and the sample size in a column `sampleSize'.
Alternatively, a list where each element is a labeled vector defining a single arm of a study.}
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

data <- read.table(textConnection('
	study  treatment  mean   std.dev  sampleSize
	01     A          -1.12  0.6      15
	01     B          -1.55  0.5      16
	02     A          -0.8   0.7      33
	02     B          -1.1   0.5      31'), header=TRUE)
network <- mtc.network(data)
print(network)
}
