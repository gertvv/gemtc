\encoding{utf8}
\name{certolizumab}
\alias{certolizumab}
\title{Certolizumab Pegol (CZP) for Rheumatoid Arthritis}
\description{A dataset of 12 trials investigating 6 treatments and placebo for rheumatoid arthritis. The main outcome is the number of patients who improved by at least 50\% on the American College of Rheumatology scale (ACR50) at 6 Months. A covariate is present for the mean disease duration at baseline (years).}
\format{A network meta-regression dataset containing 24 rows of arm-based data (responders and sample size).}
\source{
S. Dias, A.J. Sutton, N.J. Welton, and A.E. Ades (2013b),
\emph{Heterogeneity - Subgroups, Meta-Regression, Bias, and Bias-Adjustment},
Medical Decision Making 33(5):618-640. \cr
[\href{https://doi.org/10.1177/0272989X13485157}{doi:10.1177/0272989X13485157}]
}
\examples{
# Run RE regression model with informative heterogeneity prior

regressor <- list(coefficient='shared',
                  variable='diseaseDuration',
                  control='Placebo')

# sd ~ half-Normal(mean=0, sd=0.32)
hy.prior <- mtc.hy.prior(type="std.dev", distr="dhnorm", 0, 9.77)

model <- mtc.model(certolizumab,
                   type="regression",
                   regressor=regressor,
                   hy.prior=hy.prior)

\dontrun{
result <- mtc.run(model)}
}
