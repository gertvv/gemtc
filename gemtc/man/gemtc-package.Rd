\name{gemtc-package}
\alias{mtc}
\alias{gemtc}
\alias{gemtc-package}
\docType{package} 

\title{GeMTC: Network meta-analysis in R}
\description{
Network meta-analysis R package based on the Java GeMTC library from \url{http://drugis.org/gemtc}.
}
\details{
Network meta-analysis, or mixed treatment comparison (MTC) is a technique to meta-analyze networks of trials comparing two or more treatments at the same time (Lu & Ades, 2004).
Using a Bayesian hierarchical model, all direct and indirect comparisons are taken into account to arrive at a single, integrated, estimate of the effect of all included treatments based on all included studies.

This package allows the automated generation of consistency and inconsistency models that can be run using JAGS (using the rjags package), OpenBUGS (using the BRugs package) or WinBUGS (using the R2WinBUGS package).
Furthermore it allows interoperability with GeMTC files that were created by the GeMTC GUI or exported through \href{http://drugis.org/addis}{ADDIS}.
The source for GeMTC is available under GPL3 on \href{https://github.com/gertvv/mtc}{Github}.

See \url{http://drugis.org/gemtc} for more information.
}

\section{Exchangeability and (in)consistency}{ 
The key assumption in meta-analysis is that the included studies are exchangeable, meaning that they measure the same underlying relative effects.
This assumption can be violated in many ways, for example because some studies were carried out in a different population, or because some studies had a longer duration.
When the exchangeability assumption is violated in a pair-wise meta-analysis this results in heterogeneity, i.e. differences between studies within a comparison.

In network meta-analysis, the exchangeability assumption can also be violated between comparisons.
Inconsistency occurs when a treatment C has a different effect when it is compared with A or B, i.e. studies comparing A and C are systematically different from studies comparing B and C.
Thus, inconsistency may also be present when a series of pair-wise meta-analyses are conducted, but can only be detected in a network meta-analysis, and then only when there are closed loops in the evidence structure. 

Statistical models can be used to detect inconsistency (Lu & Ades, 2006), but careful consideration of the characteristics of the studies is even more important to ensure valid results from a network meta-analysis.

If there is no relevant inconsistency in the evidence, a consistency model can be used to draw conclusions about the relative effects of the included treatments and their ranking.
}

\author{Gert van Valkenhoef}

\references{
G. van Valkenhoef, G. Lu, B. de Brock, H. Hillege, A.E. Ades and N.J. Welton (2012), \emph{Automating network meta-analysis},
Research Synthesis Methods, in press.
[\href{http://dx.doi.org/10.1002/jrsm.1054}{doi:10.1002/jrsm.1054}]

G. van Valkenhoef, T. Tervonen, B. de Brock and H. Hillege (2012),
\emph{Algorithmic Parameterization of Mixed Treatment Comparisons},
Statistics and Computing, 22(5): 1099-1111.
[\href{http://dx.doi.org/10.1007/s11222-011-9281-9}{doi:10.1007/s11222-011-9281-9}]

G. Lu and A.E. Ades (2004), \emph{Combination of direct and indirect evidence in mixed treatment comparisons},
Statistics in Medicine, 23(20):3105-3124.
[\href{http://dx.doi.org/10.1002/sim.1875}{doi:10.1002/sim.1875}]

G. Lu and A.E. Ades (2006), \emph{Assessing evidence inconsistency in mixed treatment comparisons},
Journal of the American Statistical Association, 101(474): 447-459.
[\href{http://dx.doi.org/10.1198/016214505000001302}{doi:10.1198/016214505000001302}]
}

\seealso{
\code{\link{mtc.network}},
\code{\link{mtc.model}},
\code{\link{mtc.run}}
}

\examples{
# Load the example network and generate a consistency model:
file <- system.file("extdata/luades-thrombolytic.gemtc", package="gemtc")
network <- read.mtc.network(file) 
model <- mtc.model(network, type="consistency")

# Load pre-generated samples instead of runing the model:
\dontrun{results <- mtc.run(model)}
file.samples <- system.file("extdata/luades-thrombolytic.samples.gz", package="gemtc")
results <- read.mtc.result.samples(file.samples, model, "rjags")

# Print a basic statistical summary of the results:
summary(results)
}
