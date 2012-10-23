\name{mtc}
\alias{mtc}
\alias{mtc-package}
\docType{package} 

\title{Mixed Treatment Comparisons in R}
\description{
	Mixed Treatment Comparisons R package based on the Java MTC library from \url{http://drugis.org/mtc}.
}
\details{ Mixed Treatment Comparison (MTC), or Network Meta-Analysis is
a technique to meta-analyze more than two drugs at the same time. Using a full
Bayesian evidence network, all indirect comparisons are taken into account to
arrive at a single, integrated, estimate of the effect of all included
treatments based on all included studies.

This package allows the automated generation of MTC consistency and
inconsistency models that can be run using JAGS, OpenBUGS, WinBUGS or YADAS. see
\url{http://drugis.org/mtc} for more information.  }

\section{Inconsistency models}{ 
In network meta-analysis, because of the more
complex evidence structure, we can assess inconsistency of evidence, in addition
to heterogeneity within a comparison. Whereas heterogeneity represents
between-study variation in the measured relative effect of a pair of treatments,
inconsistency can only occur when a treatment C has a different effect when it
is compared with A or B (i.e., studies comparing A and C are systematically
different from studies comparing B and C). Thus, inconsistency may even occur
with normal meta-analysis, but can only be detected using a network
meta-analysis, and then only when there are closed loops in the evidence
structure. 

For more information about assessing inconsistency, see G. Lu and A.
E. Ades (2006).}

\section{Consistency models}{ If there is no relevant inconsistency in the
  evidence, a consistency model can be used to draw conclusions about the
  relative effect of the included treatments. 

Using normal meta-analysis, we could only get a subset of the confidence
intervals for relative effects we derive using network meta-analysis. Network
meta-analysis gives a consistent, integrated picture of the relative effects.
However, given such a consistent set of relative effect estimates, it may still
be difficult to draw conclusions on a potentially large set of treatments. 

Luckily, the Bayesian approach allows us to do even more with the data, and can
be used to estimate the probability that, given the priors and the data, each of
the treatments is the best, the second best, etc. This can be given by a rank
probability plot. Rank probabilities sum to one, both within a rank over
treatments and within a treatment over ranks.  }

\author{Gert van Valkenhoef}

\examples{
	file <- system.file("extdata/luades-smoking.gemtc", package="gemtc")
	network <- read.mtc.network(file) 
	model <- mtc.model(network) # when no argument supplied mtc.model generates a consistency model
	results <- mtc.run(model) 
	summary(results)
}
\references{
van Valkenhoef, G., et.al. (2012) \emph{Automating network meta-analysis} [\href{http://dx.doi.org/10.1002/jrsm.1054}{doi:10.1002/jrsm.1054}]


Lu, G. and Ades A.E. (2006), \emph{Assessing evidence inconsistency in mixed treatment comparisons},
Journal of the American Statistical Association, 101(474): 447-459.
 [\href{http://dx.doi.org/10.1198/016214505000001302}{doi:10.1198/016214505000001302}]
}
\seealso{\code{\link{mtc.model}}, \code{\link{mtc.run}}}
