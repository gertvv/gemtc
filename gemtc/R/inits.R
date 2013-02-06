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

	t1 <- as.treatment.factor(t1, model$network)
	t2 <- as.treatment.factor(t2, model$network)

	sel1 <- data$treatment == t1
	sel2 <- data$treatment == t2
	studies <- intersect(unique(data$study[sel1]), unique(data$study[sel2]))

	study.mle <- sapply(studies, function(study) {
		do.call(fn, list(data[data$study == study & (sel1 | sel2), ]))
	})

	if (!is.matrix(study.mle)) {
		study.mle <- matrix(study.mle, nrow=2, row, dimnames=list(c('mean', 'sd')))
	}

	meta <- metagen(study.mle['mean', ], study.mle['sd', ])

	rnorm(model$n.chain, meta$TE.random, model$factor * meta$seTE.random)
}

# Initial values for random effects standard deviation from prior
mtc.init.std.dev <- function(model) {
	runif(model$n.chain, 0, model$om.scale)
}

# Generate initial values for all relevant parameters
mtc.init <- function(model) {
	data <- model$network$data
	s.mat <- arm.index.matrix(model$network)
	studies <- unique(data$study)

	# Generate initial values for each parameter
	mu <- sapply(studies, function(study) {
		mtc.init.baseline.effect(model, study, data$treatment[s.mat[study, 1]])
	})
	delta <- lapply(studies, function(study) {
		sapply(1:ncol(s.mat), function(i) {
			if (i == 1 || is.na(s.mat[study, i])) rep(NA, model$n.chain)
			else mtc.init.relative.effect(model, study, data$treatment[s.mat[study, 1]], data$treatment[s.mat[study, i]])
		})
	})
	params <- mtc.basic.parameters(model)
	d <- sapply(E(model$tree), function(e) {
		v <- get.edge(model$tree, e)
		mtc.init.pooled.effect(model, v[1], v[2])
	})
	sd.d <- mtc.init.std.dev(model)

	# Separate the initial values per chain
	lapply(1:model$n.chain, function(chain) {
		c(list(
			mu = mu[chain, ],
			delta = t(sapply(delta, function(x) { x[chain, ] })),
			sd.d = sd.d[chain]
		), sapply(params, function(p) { d[chain, which(params == p)] }))
	})
}
