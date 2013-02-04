standardize.treatments <- function(treatments) {
	treatments$id <- as.factor(treatments$id)
	treatments$description <- as.character(treatments$description)
	rownames(treatments) <- treatments$id
	treatments[order(treatments$id), ]
}

standardize.data <- function(data, treatment.levels) {
	data$study <- as.factor(data$study)
	data$treatment <- factor(as.character(data$treatment), levels=treatment.levels)
	data
}

mtc.network <- function(data, description="Network", treatments=NULL) {
	# standardize the data
	if (!is.data.frame(data)) { 
		data <- as.data.frame(do.call(rbind, data))
	}
	rownames(data) <- seq(1:dim(data)[1])

	# standardize the treatments
	if (is.null(treatments)) {
		treatments <- unique(data$treatment)
	}
	if (is.list(treatments)) { 
		treatments <- as.data.frame(do.call(rbind, treatments))
	}
	if (is.character(treatments) || is.factor(treatments)) {
		treatments <- data.frame(id=treatments, description=treatments)
	}
	standardize.treatments(treatments)

	network <- list(
		description=description,
		treatments=treatments,
		data=standardize.data(data, levels(treatments$id))
	)

	mtc.network.validate(network)

	class(network) <- "mtc.network"
	network
}

mtc.network.validate <- function(network) { 
	# Check that there is some data
	stopifnot(nrow(network$treatments) > 0)  
	stopifnot(nrow(network$data) > 0)

	# Check that the treatments are correctly cross-referenced and have valid names
	stopifnot(all(network$data$treatment %in% network$treatments$id))
	stopifnot(all(network$treatments$id %in% network$data$treatment))
	idok <- regexpr("^[A-Za-z0-9_]+$", network$treatment$id) != -1
	if(!all(idok)) {
		stop(paste('Treatment name "',
			network$treatment$id[which(!idok)], '" invalid.\n',
			' Treatment names may only contain letters, digits, and underscore (_).'), sep='')
	}

	# Check that the data frame has a sensible combination of columns
	columns <- colnames(network$data)
	contColumns <- c('mean', 'std.dev', 'sampleSize')
	dichColumns <- c('responders', 'sampleSize')

	if (contColumns[1] %in% columns && dichColumns[1] %in% columns) {
		stop('Ambiguous whether data is continuous or dichotomous: both "mean" and "responders" present.')
	}

	if (contColumns[1] %in% columns && !all(contColumns %in% columns)) {
		stop(paste('Continuous data must contain columns:', paste(contColumns, collapse=', ')))
	}

	if (dichColumns[1] %in% columns && !all(dichColumns %in% columns)) {
		stop(paste('Dichotomous data must contain columns:', paste(dichColumns, collapse=', ')))
	}
}

as.treatment.factor <- function(x, network) {
	v <- network$treatments$id
	factor(x, levels=1:nlevels(v), labels=levels(v))
}

# Get all comparisons with direct evidence from the data set.
# Returns a (sorted) data frame with two columns (t1 and t2).
mtc.network.comparisons <- function(network) {
	data <- network$data

	# Identify the unique "designs" (treatment combinations)
	design <- function(study) { sort(data$treatment[data$study == study]) }
	designs <- unique(lapply(levels(data$study), design))

	# Generate all pair-wise comparisons from each "design"
	design.comparisons <- function(treatments) {
		n <- length(treatments)
		t1 <- do.call(c, lapply(1:(n-1), function(i) { rep(treatments[i], n - i) }))
		t2 <- do.call(c, lapply(1:(n-1), function(i) { treatments[(i+1):n] }))
		data.frame(t1=t1, t2=t2)
	}
	comparisons <- do.call(rbind, lapply(designs, design.comparisons))

	# Ensure the output comparisons are unique and always in the same order
	comparisons <- unique(comparisons)
	comparisons <- comparisons[order(comparisons$t1, comparisons$t2), ]
	row.names(comparisons) <- NULL
	comparisons$t1 <- as.treatment.factor(comparisons$t1, network)
	comparisons$t2 <- as.treatment.factor(comparisons$t2, network)
	comparisons
}

graph.create <- function(v, e, ...) {
	e <- t(matrix(c(e$t1, e$t2), ncol=2))
    g <- graph.empty()
    g <- g + vertex(v, label=levels(v))
    g <- g + edges(as.vector(e), ...)
    g
}

mtc.network.graph <- function(network) {
	comparisons <- mtc.network.comparisons(network)
    treatments <- network$treatments$id
	graph.create(treatments, comparisons, arrow.mode=0)
}

