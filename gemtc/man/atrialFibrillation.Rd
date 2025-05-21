\encoding{utf8}
\name{atrialFibrillation}
\alias{atrialFibrillation}
\title{Prevention of stroke in atrial fibrillation patients}
\description{A dataset of 25 trials investigating 17 treatments for stroke prevention in atrial fibrillation patients. The main outcome is the number of patients with a stroke, and a covariate captures the proportion of patients with a prior stroke.

Data are take from Table 1 of Cooper et al. (2009), with the following corrections applied: SPAF 3 and AFASAK 2 do not have a treatment 13 arm, and SPAF 1 does not contain treatment 5, but treatment 6. Thanks to prof. Cooper for providing the original analysis dataset.}
\format{A network meta-regression dataset containing 60 rows of arm-based data (responders and sample size).}
\source{
Cooper et al. (2009),
\emph{Adressing between-study heterogeneity and inconsistency in mixed treatment comparisons: Application to stroke prevention treatments in individuals with non-rheumatic atrial fibrillation},
Statistics in Medicine 28:1861-1881.
[\href{https://doi.org/10.1002/sim.3594}{doi:10.1002/sim.3594}]
}
\examples{
# Build a model similar to Model 4(b) from Cooper et al. (2009):
classes <- list("control"=c("01"),
                "anti-coagulant"=c("02","03","04","09"),
                "anti-platelet"=c("05","06","07","08","10","11","12","16","17"),
                "mixed"=c("13","14","15"))

regressor <- list(coefficient='shared',
                  variable='stroke',
                  classes=classes)

model <- mtc.model(atrialFibrillation,
                   type="regression",
                   regressor=regressor,
                   om.scale=10)

\dontrun{
result <- mtc.run(model)}
}
