# Initial values for study-level absolute treatment effects based on (adjusted) MLE
mtc.init.baseline.effect <- function(model, study, treatment) {
    data <- model$network[['data']]
    mle <- ll.call("mtc.arm.mle", model,
        data[data$study == study & data$treatment == treatment, ])
    rnorm(model$n.chain, mle['mean'], model$var.scale * mle['sd'])
}

# Initial values for study-level relative effects based on (adjusted) MLE
mtc.init.relative.effect <- function(model, study, t1, t2) {
    data <- model$network[['data']]
	if (!is.null(data) && study %in% data$study) {
		mle <- ll.call("mtc.rel.mle", model,
			data[data$study == study &
				(data$treatment == t1 | data$treatment == t2), ])
	} else { # data.re -- assumes baseline is unaltered
		data <- model$network[['data.re']]
		data <- data[data$study == study & data$treatment == t2, ]
		mle <- c('mean'=data$diff, 'sd'=data$std.err)
	}
	rnorm(model$n.chain, mle['mean'], model$var.scale * mle['sd'])
}

# Initial values for pooled effect (basic parameter) based on
# inverse-variance random effects meta-analysis (package meta)
mtc.init.pooled.effect <- function(model, t1, t2) {

    t1 <- as.treatment.factor(t1, model$network)
    t2 <- as.treatment.factor(t2, model$network)
	pair <- data.frame(t1=t1, t2=t2)

	calc <- function(data, fun) {
		sel1 <- data$treatment == t1
		sel2 <- data$treatment == t2
		studies <- intersect(unique(data$study[sel1]), unique(data$study[sel2]))

		study.mle <- sapply(studies, function(study) {
			fun(data[data$study == study, ])
		})

		if (!is.matrix(study.mle)) {
			study.mle <- matrix(study.mle, nrow=2, row)
		}
		rownames(study.mle) <- c('mean', 'sd')

		study.mle
	}


	study.mle <- NULL
    data <- model$network[['data']]
	if (!is.null(data)) {
		study.mle <- calc(data, function(data) {
			rel.mle.ab(data, paste("mtc.rel.mle", model$likelihood, model$link, sep="."), pair)
		})
	}
    data.re <- model$network[['data.re']]
	if (!is.null(data.re)) {
		study.mle <- cbind(study.mle, calc(data.re, function(data) {
			rel.mle.re(data, pair)
		}))
	}

    meta <- meta::metagen(study.mle['mean', ], study.mle['sd', ])

    rnorm(model$n.chain, meta$TE.random, model$var.scale * meta$seTE.random)
}

# Initial values for random effects standard deviation from prior
mtc.init.std.dev <- function(model) {
    runif(model$n.chain, 0, model$om.scale)
}

# Generate initial values for all relevant parameters
mtc.init <- function(model) {
    data <- model$network[['data']]
    data.re <- model$network[['data.re']]
    s.mat <- arm.index.matrix(model$network)
    studies <- levels(data$study)

    # Generate initial values for each parameter
    mu <- sapply(studies, function(study) {
        mtc.init.baseline.effect(model, study, data$treatment[s.mat[study, 1]])
    })
	studies <- c(studies, levels(data.re$study))
	ts <- c(as.character(data$treatment), as.character(data.re$treatment))
    delta <- lapply(studies, function(study) {
        sapply(1:ncol(s.mat), function(i) {
            if (i == 1 || is.na(s.mat[study, i])) rep(NA, model$n.chain)
            else mtc.init.relative.effect(
                     model, study,
                     ts[s.mat[study, 1]],
                     ts[s.mat[study, i]])
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
        c(
		if (!is.null(data)) list(mu = mu[chain, ]) else list(),
		list(
            delta = t(sapply(delta, function(x) { x[chain, ] })),
            sd.d = sd.d[chain]
        ), sapply(params, function(p) { d[chain, which(params == p)] }))
    })
}
