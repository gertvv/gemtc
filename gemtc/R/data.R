arm.index.matrix <- function(network) {
    data <- network[['data']]
	data.re <- network[['data.re']]
	all.studies <- factor(c(as.character(data$study), as.character(data.re$study)))
    studies <- levels(all.studies)
    n <- max(sapply(studies, function(study) { sum(all.studies == study) }))
    t(sapply(studies, function(study) {
        v <- which(all.studies == study)
        length(v) <- n
        v
    }))
}

mtc.model.data <- function(model) {
    data.ab <- model$network[['data']]
    data.re <- model$network[['data.re']]
	data <- data.frame(
		study=c(data.ab$study, data.re$study),
		treatment=c(data.ab$treatment, data.re$treatment)
	)
	nrow.ab <- if (!is.null(data.ab)) nrow(data.ab) else 0
	nrow.re <- if (!is.null(data.re)) nrow(data.re) else 0
	if (!is.null(data.ab) && 'responders' %in% colnames(data.ab)) {
		data$r <- NA
		data$n <- NA
		data$r[1:nrow.ab] <- data.ab$responders
		data$n[1:nrow.ab] <- data.ab$sampleSize
	} else if (!is.null(data.ab) && 'mean' %in% colnames(data.ab)) {
		data$m <- NA
		data$e <- NA
		data$m[1:nrow.ab] <- data.ab$mean
		data$e[1:nrow.ab] <- data.ab$std.dev / sqrt(data.ab$sampleSize)
	}
	if (!is.null(data.re)) {
		if (!('m' %in% colnames(data))) {
			data$m <- NA
			data$e <- NA
		}
		data$m[(nrow.ab + 1):(nrow.ab + nrow.re)] <- data.re$diff
		data$e[(nrow.ab + 1):(nrow.ab + nrow.re)] <- data.re$std.err
	}

    studies.ab <- levels(data.ab$study)
	studies.re <- levels(data.re$study)
	studies <- c(studies.ab, studies.re)
	study.arms <- c(as.character(data.ab$study), as.character(data.re$study))
    na <- sapply(studies, function(study) { sum(study.arms == study) })
	na.re <- if (length(studies.re) > 0 ) na[(length(studies.ab)+1):length(studies)] else c()
    s.mat <- arm.index.matrix(model$network)

    model.data <- list(
        ns.a = length(studies.ab),
		ns.r2 = sum(na.re == 2),
		ns.rm = sum(na.re > 2),
        ns = length(studies),
        na = na,
        t = matrix(as.numeric(data$treatment[s.mat]), nrow=nrow(s.mat)),
        om.scale = model$om.scale)

    if ('r' %in% colnames(data)) {
        model.data <- c(model.data, list(
            r = matrix(data$r[s.mat], nrow=nrow(s.mat)),
            n = matrix(data$n[s.mat], nrow=nrow(s.mat))
        ))
    }
	if ('m' %in% colnames(data)) {
        model.data <- c(model.data, list(
            m = matrix(data$m[s.mat], nrow=nrow(s.mat)),
            e = matrix(data$e[s.mat], nrow=nrow(s.mat))
        ))
    }

    model.data
}
