# Unrelated study effects model
mtc.model.use <- function(model) {
	network <- model$network

	model$code <- mtc.model.code(model, c(), '', template='gemtc.model.use.template.txt')

	model$data <- mtc.model.data(model)
	model$data$nt <- NULL
	model$data$t <- NULL
	model$inits <- lapply(mtc.init(model), function(inits) {
		list(
			mu=inits$mu,
			delta=inits$delta)
	})

	model$monitors <- list()
	delta <- model$inits[[1]]$delta
	model$monitors$enabled <- unlist(lapply(1:nrow(delta), function(i) {
		lapply(2:ncol(delta), function(j) {
			if (!is.na(delta[i, j])) {
				paste("delta[", i, ", ", j, "]", sep="")
			}
		})
	}))
	model$monitors$available <- c(
		model$monitors$enabled,
		paste("mu[", 1:length(model$inits[[1]]$mu), "]", sep="")
	)

    class(model) <- "mtc.model"

	model
}

mtc.model.name.use <- function(model) {
	"unrelated study effects"
}
