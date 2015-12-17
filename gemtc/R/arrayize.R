#'From a named vector with "BUGS format" names (v[1], m[2,3]), create a list of vectors and matrices
arrayize <- function(x) {
  # parse expressions into array indexes
  exprs <- parse(text=names(x))
  assgn <- lapply(exprs, function(expr) {
    if (class(expr) == "name") {
      list(name=as.character(expr), index=c())
    } else if (expr[[1]] == "[") {
      list(name=as.character(expr[[2]]), index=sapply(3:length(expr), function(i) { expr[[i]] }))
    } else {
      stop("Unrecognized expression")
    }
  })

  # find the unique variables and their dimension
  vars <- list()
  for (a in assgn) {
    name <- a[['name']]
    index <- a[['index']]
    if (!is.null(vars[[name]])) {
      stopifnot(length(index) == length(vars[[name]]))
      vars[[name]] <- pmax(vars[[name]], index)
    } else {
      vars[[name]] <- index
    }
  }

  # allocate the variables
  vars <- lapply(vars, function(dim) {
    if (length(dim) == 0) {
      NA
    } else if (length(dim) == 1) {
      rep(NA, dim[1])
    } else if (length(dim) == 2) {
      matrix(NA, nrow=dim[1], ncol=dim[2])
    } else {
      stop("higher dimensional objects not supported")
    }
  })

  # assign values
  for (i in 1:length(x)) {
    name <- assgn[[i]][['name']]
    index <- assgn[[i]][['index']]
    if (length(index) == 0) {
      vars[[name]] <- unname(x[i])
    } else if (length(index) == 1) {
      vars[[name]][index[1]] <- unname(x[i])
    } else if (length(index) == 2) {
      vars[[name]][index[1], index[2]] <- unname(x[i])
    }
  }

  vars[order(names(vars))]
}
