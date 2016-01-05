\encoding{utf8}
\name{relative.effect.table}
\alias{relative.effect.table}
\alias{forest.mtc.relative.effect.table}
\alias{print.mtc.relative.effect.table}
\alias{as.data.frame.mtc.relative.effect.table}

\title{Table of relative effects}
\description{Generates a table of the relative effects of all pairs of treatments. Unlike \code{\link{relative.effect}}, this method stores summaries only, not raw samples.}
\usage{
relative.effect.table(result, covariate=NA)

\method{print}{mtc.relative.effect.table}(x, ...)
\method{forest}{mtc.relative.effect.table}(x, t1, use.description=FALSE, ...)
\method{as.data.frame}{mtc.relative.effect.table}(x, ...)
}
\arguments{
  \item{result}{An object of S3 class \code{mtc.result} to derive the relative effects from.}
  \item{covariate}{(Regression analyses only) Value of the covariate at which to compute relative effects.}
  \item{x}{An object of S3 class \code{mtc.relative.effect.table}.}
  \item{t1}{Baseline treatment for the Forest plot.}
  \item{use.description}{Display treatment descriptions instead of treatment IDs.}
  \item{...}{Additional arguments.}
}
\value{
  Returns an \code{mtc.relative.effect.table} object containing the quantiles of the calculated relative effects of all pair-wise comparisons among the treatments.

  The result will be pretty printed as an n-by-n table of relative treatment effects.
  It can also be used to produce Forest plots against any arbitrary baseline.
  Finally, the \code{as.data.frame} generic method makes it possible to export the table for use in Excel or other spreadsheet software, using the core R methods \code{write.csv} or \code{write.csv2}.
}
\author{Gert van Valkenhoef}
\seealso{
  \code{\link{relative.effect}}
}
\examples{
model <- mtc.model(smoking)
# To save computation time we load the samples instead of running the model
\dontrun{results <- mtc.run(model)}
results <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))

# Creates a forest plot of the relative effects
tbl <- relative.effect.table(results)

# Print the n*n table
print(tbl)

# Plot effect relative to treatment "C"
forest(tbl, "C")

# Write to CSV (e.g. to import to Excel, then use in a Word table)
write.csv(tbl, "smoking-effects.csv")
# Note: use write.csv2 for Western European locales
}
