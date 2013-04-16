# Unrelated mean effects model
mtc.model.ume <- function(model) {
	network <- model$network

	arms <- mtc.merge.data(network)$study
	na <- sapply(unique(arms), function(study) { sum(arms == study) })
	if (any(na > 2)) {
		warning("The Unrelated Mean Effects model does not handle multi-arm trials correctly.")
	}

	model$graph <- graph.create(
		network$treatments$id,
		mtc.comparisons.baseline(network),
		arrow.mode=2, color='black', lty=1)

	model$code <- mtc.model.code(model, mtc.basic.parameters(model), sparse.relative.effect.matrix(model))

	model$data <- mtc.model.data(model)
	model$data$nt <- NULL
	model$inits <- mtc.init(model)

	monitors <- inits.to.monitors(model$inits[[1]])
	model$monitors <- list(
		available=monitors,
		enabled=c(monitors[grep('^d\\.', monitors)], 'sd.d')
	)

    class(model) <- "mtc.model"

	model
}

mtc.model.name.ume <- function(model) {
	"unrelated mean effects"
}

sparse.relative.effect.matrix <- function(model) {
	ts <- model$network$treatments$id
	nt <- length(ts)
	x <- unlist(lapply(1:nt, function(i) {
		lapply(1:nt, function(j) {
			if (model$graph[i, j]) {
				paste("d[", i, ", ", j, "] <- d.", ts[i], ".", ts[j], sep="")
			}
		})
	}))
	paste(x, collapse="\n")
}

mtc.comparisons.baseline <- function(network) {
	baseline.pairs <- function(treatments) {
		n <- length(treatments)
		t1 <- rep(treatments[1], n - 1)
		t2 <- treatments[2:n]
		data.frame(t1=t1, t2=t2)
	}
    data <- mtc.merge.data(network)

    # Identify the unique "designs" (treatment combinations)
    design <- function(study) { mtc.study.design(network, study) }
    designs <- unique(lapply(levels(data$study), design))

    # Generate all pair-wise comparisons from each "design"
    comparisons <- do.call(rbind, lapply(designs, baseline.pairs))

    # Ensure the output comparisons are unique and always in the same order
    comparisons <- unique(comparisons)
    comparisons <- comparisons[order(comparisons$t1, comparisons$t2), ]
    row.names(comparisons) <- NULL
    comparisons$t1 <- as.treatment.factor(comparisons$t1, network)
    comparisons$t2 <- as.treatment.factor(comparisons$t2, network)
    comparisons
}
