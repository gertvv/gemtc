args <- commandArgs(trailingOnly=TRUE)

library(gemtc, lib.loc=args[1])
library(testthat)
setwd(paste0(args[1], '/gemtc/tests'))
test_check('gemtc', filter=args[2])
