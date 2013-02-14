\encoding{utf8}
\name{blobbogram}
\alias{blobbogram}
\title{Plot a blobbogram (AKA forest plot)}
\description{
\code{blobbogram} is a flexible function for creating blobbograms (forest plots), making no specific assumptions about the data being plotted. It supports column and row grouping as well as pagination.
}
\usage{
blobbogram(data, id.label="Study", ci.label="Mean (95\% CI)",
    left.label=NULL, right.label=NULL,
    log.scale=FALSE, xlim=NULL, styles=NULL,
    grouped=TRUE, group.labels=NULL,
    columns=NULL, column.labels=NULL,
    column.groups=NULL, column.group.labels=NULL,
    ask=dev.interactive(orNone=TRUE))
}
\arguments{
  \item{data}{A data frame containing one row for each confidence interval to be visualized.
    The data format is described below.}
  \item{ci.label}{Label to show above the confidence intervals.}
  \item{left.label}{Label to show on the left-hand side of the no-difference line.}
  \item{right.label}{Label to show on the right-hand side of the no-difference line.}
  \item{log.scale}{If TRUE, the confidence intervals are given on a log scale, and axis labels will be \code{exp()} transformed.}
  \item{xlim}{The scale limits of the plot, if the confidence interval exceeds these limits an arrow will be shown at the limit. If unspecified, limits will be chosen that encompass all confidence intervals.}
  \item{styles}{A data frame describing the different row styles. By default, the styles "normal", "pooled" and "group" are defined.}
  \item{grouped}{If TRUE, and group.labels are specified, rows will be grouped according to the "group" column given in the data argument.}
  \item{group.labels}{Vector of group labels.}
  \item{columns}{Additional user-defined columns to be shown (names of columns given in the data argument).}
  \item{column.labels}{A vector of labels for the user-defined columns.}
  \item{column.groups}{Column groups, a numeric vector specifying the column group for each column.}
  \item{column.group.labels}{A vector of labels for the column groups.}
  \item{ask}{If TRUE, a prompt will be displayed before generating the next page of a multi-page plot.}
}
\details{
The \code{blobbogram} function creates a blobbogram (forest plot) from the given data (point estimates and confidence intervals) and meta-data (labels, column specifications, column groups, row groups, styles) using the \code{grid} package.
If the plot would not fit the device's graphics region, the content is broken up into multiple plots generated in sequence (pagination).

The \code{data} argument is given as a data frame containing the following columns:
\itemize{
  \item{id: identifier (label) for this row.}
  \item{group (optional): row group this row belongs to (indexes into the \code{group.labels} argument).}
  \item{pe: point estimate.}
  \item{ci.l: lower confidence interval limit.}
  \item{ci.u: upper confidence interval limit.}
  \item{style: the style to apply to this row (defined in the \code{styles} argument).}
}
Additional user-defined columns can be specified using the \code{columns} and \code{column.labels} arguments.

The \code{styles} argument is given as a data frame containing the following columns:
\itemize{
  \item{style: name of the style.}
  \item{weight: font weight.}
  \item{pe.style: symbol to draw for the point estimate ("circle" or "square", currently).}
}
}
\value{
None.
}
\note{
This method should not be considered stable.
We intend to generalize it further and possibly provide it in a separate package.
The interface may change at any time.
}
\author{Gert van Valkenhoef, Joël Kuiper}
%% ~Make other sections like Warning with \section{Warning }{....} ~

\seealso{
\code{\link{meta::forest}}, \code{\link{grid::Grid}}
}
\examples{
data <- read.table(textConnection('
id               group pe       ci.l ci.u style      value.A    value.B 
"Study 1"  1         0.35 0.08 0.92 "normal" "2/46"     "7/46" 
"Study 2"  1         0.43 0.15 1.14 "normal" "4/50"     "8/49" 
"Study 3"  2         0.31 0.07 0.74 "normal" "2/97"     "10/100"
"Study 4"  2         0.86 0.34 2.90 "normal" "9/104"    "6/105" 
"Study 5"  2         0.33 0.10 0.72 "normal" "4/74"     "14/74" 
"Study 6"  2         0.47 0.23 0.91 "normal" "11/120" "22/129"
"Pooled"     NA      0.42 0.15 1.04 "pooled" NA             NA 
'), header=TRUE)
data$pe <- log(data$pe)
data$ci.l <- log(data$ci.l)
data$ci.u <- log(data$ci.u)

blobbogram(data, group.labels=c('GROUP 1', 'GROUP 2'),
    columns=c('value.A', 'value.B'), column.labels=c('r/n', 'r/n'),
    column.groups=c(1, 2), grouped=TRUE, column.group.labels=c('Intervention', 'Control'),
    id.label="Trial", ci.label="Odds Ratio (95\% CrI)", log.scale=TRUE)
}
