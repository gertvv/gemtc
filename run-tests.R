args <- commandArgs(trailingOnly=TRUE)

library(gemtc, lib.loc=args[1])
library(testthat)
setwd(paste0(args[1], '/gemtc/tests'))
if (length(args) == 3) {
  gemtc.test.sampler <- args[3]
}
test_check('gemtc', filter=args[2])
