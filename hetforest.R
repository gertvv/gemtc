library(gemtc)

hetforest <- function(network, model, result, ...) {
	ts <- sort(unique(network$treatments$id))

	t1 <- unlist(lapply(ts[1:(length(ts)-1)], function(t) { rep(t, length(ts) - which(ts == t)) }))
	t2 <- unlist(lapply(ts[1:(length(ts)-1)], function(t) { ts[(which(ts == t) + 1):length(ts)] }))

	re.est <- summary(relative.effect(result, as.character(t1), as.character(t2)))

	data <- list(id=character(), group=character(), pe=c(), ci.l=c(), ci.u=c(), style=factor(levels=c('normal','pooled')))
	group.labels <- character()
	for (i in 1:length(t1)) {
		param <- paste('d', t1[i], t2[i], sep='.')
		group.labels[param] <- paste(t2[i], 'vs', t1[i])

		# find the studies that measure both t1 and t2
		sel1 <- network$data$treatment == t1[i]
		sel2 <- network$data$treatment == t2[i]
		studies <- intersect(unique(network$data$study[sel1]), unique(network$data$study[sel2]))
		sel3 <- network$data$study %in% studies

		# if there are studies, include in forest plot
		if (sum(sel3) > 0) {
			for (study in studies) {
				data$id <- c(data$id, study)
				data$group <- c(data$group, param)
				data$style <- c(data$style, 'normal')
				
				r1 <- network$data[sel1 & (network$data$study == study), ]
				r2 <- network$data[sel2 & (network$data$study == study), ]

				if ('mean' %in% colnames(network$data)) {
					md <- as.numeric(r2['mean'] - r1['mean'])
					var.1 <- (r1['std.dev'] / sqrt(r1['sampleSize']))^2
					var.2 <- (r2['std.dev'] / sqrt(r2['sampleSize']))^2
					se <- as.numeric(sqrt(var.1 + var.2))

					data$pe <- c(data$pe, md)
					data$ci.l <- c(data$ci.l, md - 1.96 * se)
					data$ci.u <- c(data$ci.u, md + 1.96 * se)
				} else {
					if (r1['responders'] != 0 || r2['responders'] != 0) {
						s1 <- r1['responders']
						f1 <- r1['sampleSize'] - s1
						s2 <- r2['responders']
						f2 <- r2['sampleSize'] - s2
						if (s1 == 0 || s2 == 0 || f1 == 0 || f2 == 0) {
							s1 <- s1 + 0.5
							s2 <- s2 + 0.5
							f1 <- f1 + 0.5
							f2 <- f2 + 0.5

							data$id[length(data$id)] <- paste(data$id[length(data$id)], "*")
						}
						md <- as.numeric(log((s2/f2)/(s1/f1)))
						se <- as.numeric(1/s1 + 1/f1 + 1/s2 + 1/f2)

						data$pe <- c(data$pe, md)
						data$ci.l <- c(data$ci.l, md - 1.96 * se)
						data$ci.u <- c(data$ci.u, md + 1.96 * se)
					} else {
						data$pe <- c(data$pe, NA)
						data$ci.l <- c(data$ci.l, NA)
						data$ci.u <- c(data$ci.u, NA)
					}
				}
				
			}
			data$id <- c(data$id, 'Pooled')
			data$group <- c(data$group, param)
			data$style <- c(data$style, 'pooled')
			data$pe <- c(data$pe, re.est$quantiles[param, 3])
			data$ci.l <- c(data$ci.l, re.est$quantiles[param, 1])
			data$ci.u <- c(data$ci.u, re.est$quantiles[param, 5])
		}
	}
	data <- as.data.frame(data)
	blobbogram(data, group.labels=group.labels, ci.label="Median (95% CrI)", log.scale='responders' %in% colnames(network$data), ...)
}
