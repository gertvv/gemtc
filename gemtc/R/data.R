arm.index.matrix <- function(network) {
	data <- network$data
	studies <- unique(data$study)
	n <- max(sapply(studies, function(study) { sum(data$study == study) }))
	t(sapply(studies, function(study) {
		v <- which(data$study == study)
		length(v) <- n
		v
	}))
}

mtc.model.data <- function(model) {
	data <- model$network$data
	
	studies <- unique(data$study)
	na <- sapply(studies, function(study) { sum(data$study == study) })
	s.mat <- arm.index.matrix(model$network)

	model.data <- list(
		ns = length(studies),
		na = na,
		t = matrix(as.numeric(data$treatment[s.mat]), nrow=nrow(s.mat)),
		om.scale = model$om.scale)

	if ('responders' %in% colnames(data)) {
		model.data <- c(model.data, list(
			r = matrix(data$responders[s.mat], nrow=nrow(s.mat)),
			n = matrix(data$sampleSize[s.mat], nrow=nrow(s.mat))
		))
	} else if ('mean' %in% colnames(data)) {
		model.data <- c(model.data, list(
			m = matrix(data$mean[s.mat], nrow=nrow(s.mat)),
			e = matrix(data$std.dev[s.mat] / sqrt(data$sampleSize[s.mat]), nrow=nrow(s.mat))
		))
	}

	model.data
}
