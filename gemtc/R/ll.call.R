ll.call <- function(fnName, model, ...) {
  fn <- paste(fnName, model[['likelihood']], model[['link']], sep=".")
  do.call(fn, list(...))
}

ll.defined <- function(model) {
  fns <- c('mtc.arm.mle', 'mtc.rel.mle', 'mtc.code.likelihood',
    'scale.log', 'scale.name', 'scale.limit.inits',
    'required.columns.ab', 'validate.data')
  fns <- paste(fns, model[['likelihood']], model[['link']], sep=".")
  all(sapply(fns, function(fn) { exists(fn, mode='function') }))
}
