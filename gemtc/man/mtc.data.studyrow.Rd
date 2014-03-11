\encoding{utf8}
\name{mtc.data.studyrow}
\alias{mtc.data.studyrow}

\title{Convert one-study-per-row datasets}
\description{Converts datasets in the one-study-per-row format to one-arm-per-row format used by GeMTC}
\usage{
mtc.data.studyrow(data,
  armVars=c('treatment'='t', 'responders'='r', 'sampleSize'='n'),
  nArmsVar='na',
  studyVars=c(),
  studyNames=1:nrow(data),
  treatmentNames=NA,
  patterns=c('\%s..', '\%s..\%d.'))
}
\arguments{
  \item{data}{Data in one-study-per-row format.}
  \item{armVars}{Vector of per-arm variables. The name of each component will be the column name in the resulting dataset. The column name in the source dataset is derived from the value of each component.}
  \item{nArmsVar}{Variable holding the number of arms for each study.}
  \item{studyVars}{Vector of per-study variables. The name of each component will be the column name in the resulting dataset. The column name in the source dataset is derived from the value of each component.}
  \item{studyNames}{Vector of study names.}
  \item{treatmentNames}{Vector of treatment names.}
  \item{patterns}{Patterns to generate column names in the source dataset. The first is for per-study variables, the second for per-arm variables.}
}
\value{A data table with the requested columns.}
\details{
Maps the one-study-per-row format that is widely used and convenient for BUGS models to the one-arm-per-row format used by GeMTC.
As the primary purpose is to input datasets from BUGS models, the defaults work for the standard BUGS data table format.
In most cases, it should be possible to just copy/paste the BUGS data table (without the final 'END') and \code{read.table} it into R, then apply \code{mtc.data.studyrow}.
In many cases, the resulting table can be processed directly by \code{\link{mtc.network}}.
}
\author{Gert van Valkenhoef}
\seealso{\code{\link{mtc.network}}}
\examples{
## Example taken from the NICE DSU TSD series in Evidence Synthesis, #2
## Dopamine agonists for the treatment of Parkinson's

# Read the bugs-formatted data
data.src <- read.table(textConnection('
t[,1]	t[,2]	t[,3]	y[,1]	y[,2]	y[,3]	se[,1]	se[,2]	se[,3]	na[]
1	3	NA	-1.22	-1.53	NA	0.504	0.439	NA	2
1	2	NA	-0.7	-2.4	NA	0.282	0.258	NA	2
1	2	4	-0.3	-2.6	-1.2	0.505	0.510	0.478	3
3	4	NA	-0.24	-0.59	NA	0.265	0.354	NA	2
3	4	NA	-0.73	-0.18	NA	0.335	0.442	NA	2
4	5	NA	-2.2	-2.5	NA	0.197	0.190	NA	2
4	5	NA	-1.8	-2.1	NA	0.200	0.250	NA	2'), header=TRUE)

# Convert the data, setting treatment names
data <- mtc.data.studyrow(data.src,
  armVars=c('treatment'='t', 'mean'='y', 'std.err'='se'),
  treatmentNames=c('Placebo', 'DA1', 'DA2', 'DA3', 'DA4'))

# Check that the data are correct
print(data)

# Create a network
network <- mtc.network(data)
}
