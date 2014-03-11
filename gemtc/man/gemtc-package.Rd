\name{gemtc-package}
\alias{mtc}
\alias{gemtc}
\alias{gemtc-package}
\docType{package} 

\title{GeMTC: Network meta-analysis in R}
\description{
An R package for performing network meta-analyses (mixed treatment comparisons).
}
\details{
Network meta-analysis, or mixed treatment comparison (MTC) is a technique to meta-analyze networks of trials comparing two or more treatments at the same time [Dias et al. 2013].
Using a Bayesian hierarchical model, all direct and indirect comparisons are taken into account to arrive at a single consistent estimate of the effect of all included treatments based on all included studies.

This package allows the automated generation of network meta-analysis models [van Valkenhoef et al. 2012] that can be run using JAGS (using the rjags package), OpenBUGS (using the BRugs package) or WinBUGS (using the R2WinBUGS package).
Note that there is a known issue with BUGS that prevents some models from running, though the remaining models appear to run correctly.
It is highly recommended to use JAGS rather than BUGS.

This package is interoperable with GeMTC files that were created by the GeMTC GUI or exported from \href{http://drugis.org/addis}{ADDIS}.
The source for GeMTC is available under the GPL-3 on \href{https://github.com/gertvv/gemtc}{Github}.

See \url{http://drugis.org/gemtc} for more information.
}

\author{Gert van Valkenhoef}

\references{
S. Dias, N.J. Welton, D.M. Caldwell, and A.E. Ades (2010),
\emph{Checking consistency in mixed treatment comparison meta-analysis},
Statistics in Medicine 29(7-8, Sp. Iss. SI):932-944.\cr
[\href{http://dx.doi.org/10.1002/sim.3767}{doi:10.1002/sim.3767}]

S. Dias, A.J. Sutton, A.E. Ades, and N.J. Welton (2013a),
\emph{A Generalized Linear Modeling Framework for Pairwise and Network Meta-analysis of Randomized Controlled Trials},
Medical Decision Making 33(5):607-617.
[\href{http://dx.doi.org/10.1177/0272989X12458724}{doi:10.1177/0272989X12458724}]

S. Dias, N.J. Welton, A.J. Sutton, D.M. Caldwell, G. Lu, and A.E. Ades (2013b),
\emph{Inconsistency in Networks of Evidence Based on Randomized Controlled Trials},
Medical Decision Making 33(5):641-656.
[\href{http://dx.doi.org/10.1177/0272989X12455847}{doi:10.1177/0272989X12455847}]

R.M. Turner, J. Davey, M.J. Clarke, S.G. Thompson, J.P.T. Higgins (2012),
\emph{Predicting the extent of heterogeneity in meta-analysis, using empirical data from the Cochrane Database of Systematic Reviews},
International Journal of Epidemiology 41(3):818-827.
[\href{http://dx.doi.org/10.1093/ije/dys041}{doi:10.1093/ije/dys041}]


G. van Valkenhoef, G. Lu, B. de Brock, H. Hillege, A.E. Ades, and N.J. Welton (2012),
\emph{Automating network meta-analysis},
Research Synthesis Methods 3(4):285-299.
[\href{http://dx.doi.org/10.1002/jrsm.1054}{doi:10.1002/jrsm.1054}]

G. van Valkenhoef, S. Dias, A.E. Ades, and N.J. Welton (2014a),
\emph{Automated generation of node-splitting models for the assessment of inconsistency in network meta-analysis},
draft manuscript.

G. van Valkenhoef et al. (2014b),
\emph{Modeling inconsistency as heterogeneity in network meta-analysis},
draft manuscript.
}
\seealso{
\code{\link{mtc.network}},
\code{\link{mtc.model}},
\code{\link{mtc.run}}
}

\examples{
# Load the example network and generate a consistency model:
file <- system.file("extdata/luades-smoking.gemtc", package="gemtc")
network <- read.mtc.network(file) 
model <- mtc.model(network, type="consistency")

# Load pre-generated samples instead of runing the model:
\dontrun{results <- mtc.run(model, thin=10)}
results <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))

# Print a basic statistical summary of the results:
summary(results)
## Iterations = 5010:25000
## Thinning interval = 10 
## Number of chains = 4 
## Sample size per chain = 2000 
## 
## 1. Empirical mean and standard deviation for each variable,
##    plus standard error of the mean:
## 
##         Mean     SD Naive SE Time-series SE
## d.A.B 0.4965 0.4081 0.004563       0.004989
## d.A.C 0.8359 0.2433 0.002720       0.003147
## d.A.D 1.1088 0.4355 0.004869       0.005280
## sd.d  0.8465 0.1913 0.002139       0.002965
## 
## 2. Quantiles for each variable:
## 
##          2.5%    25%    50%    75% 97.5%
## d.A.B -0.2985 0.2312 0.4910 0.7530 1.341
## d.A.C  0.3878 0.6720 0.8273 0.9867 1.353
## d.A.D  0.2692 0.8197 1.0983 1.3824 2.006
## sd.d   0.5509 0.7119 0.8180 0.9542 1.283
}
