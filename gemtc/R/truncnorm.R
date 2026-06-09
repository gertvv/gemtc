# The contents of this file have been adapted from version 1.0-9 of the {truncnorm} package, retrieved from:
#   https://github.com/olafmersmann/truncnorm/tree/28593c0b454bdbe860bc00e8e23690c12af89927
# The package is credited to
#   Olaf Mersmann, Heike Trautmann, Detlef Steuer and Bjorn Bornkamp
# and is released under the GNU General Public Licence v2 (or later).
# The package does not include a copy of that license, so it is not reproduced here.
# 
# The superficial changes that led to this particular file were performed by Miguel Lechon on behalf of
# Boehringer-Ingelheim Pharma GmbH & Co.KG. They do not deviate meaningfully from the original work, so no
# separate copyright is claimed.
#

##
## truncnorm.R - Interface to truncnorm.c
##
## Authors:
##  Heike Trautmann  <trautmann@statistik.uni-dortmund.de>
##  Detlef Steuer    <detlef.steuer@hsu-hamburg.de>
##  Olaf Mersmann    <olafm@statistik.uni-dortmund.de>
##

truncnorm__rtruncnorm <- function(n, a=-Inf, b=Inf, mean=0, sd=1) {
  stopifnot(length(a) > 0,
            length(b) > 0,
            length(mean) > 0,
            length(sd) > 0)
  if (length(n) > 1)
    n <- length(n)
  else if (!is.numeric(n))
    stop("non-numeric argument n.")
  else if (n == 0)
    return(NULL)
  .Call(C_do_rtruncnorm, as.integer(n),
        as.numeric(a), as.numeric(b), as.numeric(mean), as.numeric(sd))
}
