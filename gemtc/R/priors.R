# Guess the measurement scale based on differences observed in the data set
guess.scale <- function(model) {
	fn <- paste("mtc.rel.mle", model$likelihood, model$link, sep=".")
	data <- model$network$data

	max(sapply(unique(data$study), function(study) {
		pairs <- mtc.treatment.pairs(mtc.study.design(model$network, study))
		max(sapply(1:nrow(pairs), function(i) {
			sel1 <- data$treatment == pairs$t1[i]
			sel2 <- data$treatment == pairs$t2[i]
			mle <- do.call(fn, list(data[data$study == study & (sel1 | sel2), ]))
			abs(mle['mean'])
		}))
	}))
}
