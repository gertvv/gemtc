ll.call <- function(fn, model, ...) {
    fn <- paste(fn, model$likelihood, model$link, sep=".")
	do.call(fn, list(...))
}

ll.defined <- function(model) {
	fns <- c('mtc.arm.mle', 'mtc.rel.mle', 'mtc.code.likelihood',
		'scale.log', 'scale.name')
	fns <- paste(fns, model$likelihood, model$link, sep=".")
	all(exists(fns, mode='function'))
}
