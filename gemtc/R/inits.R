# Initial values for study-level absolute treatment effects based on (adjusted) MLE
mtc.init.baseline.effect <- function(model, study, treatment) {
	fn <- paste("mtc.arm.mle", model$likelihood, model$link, sep=".")
	data <- model$network$data

	mle <- do.call(fn, list(data[data$study == study & data$treatment == treatment, ]))
	rnorm(model$n.chain, mle['mean'], model$factor * mle['sd'])
}

# Initial values for study-level relative effects based on (adjusted) MLE
mtc.init.relative.effect <- function(model, study, t1, t2) {
	fn <- paste("mtc.rel.mle", model$likelihood, model$link, sep=".")
	data <- model$network$data

	mle <- do.call(fn, list(data[data$study == study & (data$treatment == t1 | data$treatment == t2), ]))
	rnorm(model$n.chain, mle['mean'], model$factor * mle['sd'])
}

# Initial values for pooled effect (basic parameter) based on
# inverse-variance random effects meta-analysis (package meta)
mtc.init.pooled.effect <- function(model, t1, t2) {
	fn <- paste("mtc.rel.mle", model$likelihood, model$link, sep=".")
	data <- model$network$data

	sel1 <- data$treatment == t1
	sel2 <- data$treatment == t2
	studies <- intersect(unique(data$study[sel1]), unique(data$study[sel2]))

	study.mle <- sapply(studies, function(study) {
		do.call(fn, list(data[data$study == study & (sel1 | sel2), ]))
	})

	meta <- metagen(study.mle['mean', ], study.mle['sd', ])

	rnorm(model$n.chain, meta$TE.random, model$factor * meta$seTE.random)
}

# Initial values for random effects standard deviation from prior
mtc.init.std.dev <- function(model) {
	runif(model$n.chain, 0, model$om.scale)
}
