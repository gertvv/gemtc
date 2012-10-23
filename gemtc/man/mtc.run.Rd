\encoding{utf8}
\name{mtc.run}
\alias{mtc.run}
\alias{summary.mtc.result}
\alias{plot.mtc.result}
\alias{forest.mtc.result}
\alias{print.mtc.result}
\alias{as.mcmc.list.mtc.result}

%- Also NEED an '\alias' for EACH other topic documented here.
\title{Running an MTC Model and obtaining results}
\description{ The function \code{mtc.run} is used to generate samples from a object
of type \code{mtc.model} using a MCMC sampler. The resulting \code{mtc.results} object can
be used for further analysis of the dataset.	}
\usage{
mtc.run(model, sampler = NA, n.adapt = 5000, n.iter = 20000, thin = 1)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
	\item{model}{A object of S3 class \code{mtc.model} describing a consistency or inconsistency model}
	\item{sampler}{
		A string literal indicating which sampler to use.
		Allowed values are \code{'BUGS'} using either \code{'BRugs'} or \code{'R2WinBUGS'}, 
		\code{'JAGS'} using \code{'rjags'} or \code{'YADAS'}. 
		When none supplied it will try BUGS, JAGS and YADAS, in that order}
	\item{n.adapt}{Amount of adapt or tuning iterations}
	\item{n.iter}{Amount of MCMC simulation iterations}
	\item{thin}{Thinning factor}
}
\value{
An object of class \code{mtc.result}. This is a list with the following elements: 
\item{samples}{The samples resulting from running the MCMC model} 
\item{model}{A reference to the model used to produce the samples}
\item{sampler}{The sampler used to produce the samples}


The object can be coerced to an \code{mcmc.list} from the \code{coda} package by the generic S3 method 
\code{as.mcmc} which is used internally to generate the summaries.
}


\author{Gert van Valkenhoef, Joël Kuiper}
%% ~Make other sections like Warning with \section{Warning }{....} ~
\section{Model convergence}{Convergence of the model can be assessed using the
Brooks-Gelman-Rubin method (Brooks & Gelman, 1998) This method compares within-chain and between-chain
variance to calculate the Potential Scale Reduction Factor (PSRF).	This method
is available through calling the \code{gelman.diag} or \code{gelman.plot} function from
the \code{coda} package on the \code{mtc.result} object.	A PSRF close to one indicates
approximate convergence has been reached.}

\seealso{\code{\link{mtc.model}}}

\references{
Brooks, S.P. and Gelman, A. (1998), \emph{General methods for monitoring convergence of iterative simulations}, Journal of Computational and Graphical Statistics, 7(4): 434-455. [\href{http://www.jstor.org/stable/1390675}{JSTOR 1390675}]
}
\examples{
	file <- system.file("extdata/luades-thrombolytic.gemtc", package="gemtc")
	network <- read.mtc.network(file)
	model <- mtc.model(network)
	results <- mtc.run(model, "YADAS", n.iter=10000, n.adapt=5000, thin=10)
	summary(results)
	## Iterations = 5001:25000
	## Thinning interval = 1 
	## Number of chains = 4 
	## Sample size per chain = 20000 
	##
	## 1. Empirical mean and standard deviation for each variable,
	##	 plus standard error of the mean:
	##
	##				Mean		 SD  Naive SE Time-series SE
	## d.A.B 0.4937 0.4073 0.0014399			 0.005377
	## d.A.C 0.8381 0.2439 0.0008623			 0.004275
	## d.A.D 1.1038 0.4467 0.0015793			 0.007219
	## sd.d  0.8490 0.1877 0.0006637			 0.004570
	##
	## 2. Quantiles for each variable:
	##
	##				 2.5%		 25%		50%		 75% 97.5%
	## d.A.B -0.3015 0.2290 0.4887 0.7539 1.319
	## d.A.C	0.3804 0.6762 0.8280 0.9936 1.338
	## d.A.D	0.2591 0.8082 1.0880 1.3882 2.027
	## sd.d		0.5523 0.7154 0.8280 0.9547 1.282
	
	forest(results) # Shows a forest plot
	plot(results) # Shows density plot of the samples
}
