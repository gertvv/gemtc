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
Network meta-analysis, or mixed treatment comparison (MTC) is a technique to meta-analyze networks of trials comparing two or more treatments at the same time [Dias et al. 2013a].
Using a Bayesian hierarchical model, all direct and indirect comparisons are taken into account to arrive at a single consistent estimate of the effect of all included treatments based on all included studies.

This package allows the automated generation of network meta-analysis models [van Valkenhoef et al. 2012], inclusing both fixed effect and random effects network meta-analysis, node-splitting models to identify inconsistency, and network meta-regression models.
Models are estimated using JAGS (through the \code{rjags} package).

The package can read GeMTC files that were created by the GeMTC GUI (now obsolete) or exported from \href{http://drugis.org/addis}{ADDIS} 1.x, if the optional package \code{XML} is installed.
The source for GeMTC is available under the GPL-3 on \href{https://github.com/gertvv/gemtc}{Github}.
}

\author{Gert van Valkenhoef}

\references{
S. Dias, N.J. Welton, D.M. Caldwell, and A.E. Ades (2010),
\emph{Checking consistency in mixed treatment comparison meta-analysis},
Statistics in Medicine 29(7-8, Sp. Iss. SI):932-944.\cr
[\href{https://doi.org/10.1002/sim.3767}{doi:10.1002/sim.3767}]

S. Dias, A.J. Sutton, A.E. Ades, and N.J. Welton (2013a),
\emph{A Generalized Linear Modeling Framework for Pairwise and Network Meta-analysis of Randomized Controlled Trials},
Medical Decision Making 33(5):607-617.
[\href{https://doi.org/10.1177/0272989X12458724}{doi:10.1177/0272989X12458724}]

S. Dias, A.J. Sutton, N.J. Welton, and A.E. Ades (2013b),
\emph{Heterogeneity - Subgroups, Meta-Regression, Bias, and Bias-Adjustment},
Medical Decision Making 33(5):618-640. \cr
[\href{https://doi.org/10.1177/0272989X13485157}{doi:10.1177/0272989X13485157}]

S. Dias, N.J. Welton, A.J. Sutton, D.M. Caldwell, G. Lu, and A.E. Ades (2013c),
\emph{Inconsistency in Networks of Evidence Based on Randomized Controlled Trials},
Medical Decision Making 33(5):641-656.
[\href{https://doi.org/10.1177/0272989X12455847}{doi:10.1177/0272989X12455847}]

A. Gelman, A. Jakulin, M. Grazia Pittau, Y.-S. Su (2008),
\emph{A weakly informative default prior distribution for logistic and other regression models},
The Annals of Applied Statistics 2(4):1360-1383.
[\href{https://doi.org/10.1214/08-AOAS191}{doi:10.1214/08-AOAS191}]

R.M. Turner, J. Davey, M.J. Clarke, S.G. Thompson, J.P.T. Higgins (2012),
\emph{Predicting the extent of heterogeneity in meta-analysis, using empirical data from the Cochrane Database of Systematic Reviews},
International Journal of Epidemiology 41(3):818-827.
[\href{https://doi.org/10.1093/ije/dys041}{doi:10.1093/ije/dys041}]

G. van Valkenhoef, G. Lu, B. de Brock, H. Hillege, A.E. Ades, and N.J. Welton (2012),
\emph{Automating network meta-analysis},
Research Synthesis Methods 3(4):285-299.
[\href{https://doi.org/10.1002/jrsm.1054}{doi:10.1002/jrsm.1054}]

G. van Valkenhoef, S. Dias, A.E. Ades, and N.J. Welton (2015),
\emph{Automated generation of node-splitting models for assessment of inconsistency in network meta-analysis},
Research Synthesis Methods, accepted manuscript.
[\href{https://doi.org/10.1002/jrsm.1167}{doi:10.1002/jrsm.1167}]

G. van Valkenhoef et al. (draft),
\emph{Modeling inconsistency as heterogeneity in network meta-analysis},
draft manuscript.

D.E. Warn, S.G. Thompson, and D.J. Spiegelhalter (2002),
\emph{Bayesian random effects meta-analysis of trials with binary outcomes: methods for the absolute risk difference and relative risk scales},
Statistics in Medicine 21(11):1601-1623.
[\href{https://doi.org/10.1002/sim.1189}{doi:10.1002/sim.1189}]

}
\seealso{
\code{\link{mtc.network}},
\code{\link{mtc.model}},
\code{\link{mtc.run}}
}

\examples{
# Load the example network and generate a consistency model:
model <- mtc.model(smoking, type="consistency")

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
