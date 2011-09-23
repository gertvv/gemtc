\name{mtc}
\alias{mtc}
\title{Mixed Treatment Comparisons in R}
\description{
	Mixed Treatment Comparisons R package based on the Java MTC library from http://drugis.org/mtc and JAGS.
}
\usage{
  network <- mtcNetwork("file.gemtc")
  model <- mtcModel(network)
  data <- mtcJags(model, 3000, 2000)
}
\details{
  To be filled in ...
}
\value{
  ...
}
\author{Gert van Valkenhoef}
\examples{
	fileName <- system.file("extdata/luades-smoking.gemtc", package="mtc")
	network <- mtcNetwork(fileName)
	model <- mtcModel(network)
	data <- mtcJags(model, 1000, 2000)
	data <- append.derived(data, model$jags$analysis$deriv)
	summary(data)
}
\keyword{mtc}
