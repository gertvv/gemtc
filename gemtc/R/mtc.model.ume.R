# Unrelated mean effects model
mtc.model.ume <- function(model) {

}

mtc.model.name.ume <- function(model) {
	"unrelated mean effects"
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
