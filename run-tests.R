args <- commandArgs(trailingOnly=TRUE)

library(gemtc, lib.loc=args[1])
library(testthat)
setwd(paste0(args[1], '/gemtc/tests'))
if (length(args) > 2 && args[3] == "powerAdjust") {
  powerAdjustMode <- TRUE
}
test_check('gemtc', filter=args[2])
