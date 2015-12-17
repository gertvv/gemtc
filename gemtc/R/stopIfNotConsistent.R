#' Stop if the results were not derived from an internally consistent model type
stopIfNotConsistent <- function(result, method) {
  allowedTypes <- c('consistency', 'regression')
  if (!(tolower(result[['model']][['type']]) %in% allowedTypes)) stop(paste("Can only apply", method, "to models of the following types:", paste(allowedTypes, collapse=", ")))
}
