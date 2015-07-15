required.columns.counts <- function() {
  c('r'='responders', 'n'='sampleSize')
}

validate.data.counts <- function(data.ab) {
  stopifnot(all(data.ab[['sampleSize']] >= data.ab[['responders']]))
  stopifnot(all(data.ab[['sampleSize']] > 0))
  stopifnot(all(data.ab[['responders']] >= 0))
}

correction.counts <- function(data, correction.force, correction.type, correction.magnitude) {
  correction.need <-
    data[1,'responders'] == 0 || data[1,'responders'] == data[1,'sampleSize'] ||
    data[2,'responders'] == 0 || data[2,'responders'] == data[2,'sampleSize']

  groupRatio <- if (correction.type == "reciprocal") {
    data[1,'sampleSize'] / data[2,'sampleSize']
  } else {
    1
  }

  if (correction.force || correction.need) {
    correction.magnitude * c(groupRatio/(groupRatio+1), 1/(groupRatio+1))
  } else {
    c(0, 0)
  }
}
