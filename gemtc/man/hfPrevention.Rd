\encoding{utf8}
\name{hfPrevention}
\alias{hfPrevention}
\title{Statins versus placebo in primary and secondary prevention of heart failure}
\description{A dataset of 19 trials comparing statins versus placebo or usual care for cholesterol lowering. The main outcome is the number of deaths. Trials are either primary prevention (no previous heart disease; secondary = 0) or secondary prevention (previous heart disease; secondary = 1).}
\format{A meta-regression dataset containing 38 rows of arm-based data (responders and sample size).}
\source{
S. Dias, A.J. Sutton, N.J. Welton, and A.E. Ades (2013b),
\emph{Heterogeneity - Subgroups, Meta-Regression, Bias, and Bias-Adjustment},
Medical Decision Making 33(5):618-640. \cr
[\href{https://doi.org/10.1177/0272989X13485157}{doi:10.1177/0272989X13485157}]
}
\examples{
# Build a model similar to Program 1(a) from Dias et al. (2013b):
regressor <- list(coefficient='shared',
                  variable='secondary',
                  control='control')

model <- mtc.model(hfPrevention,
                   type="regression",
                   regressor=regressor,
                   hy.prior=mtc.hy.prior("std.dev", "dunif", 0, 5))

\dontrun{
result <- mtc.run(model)}
}
