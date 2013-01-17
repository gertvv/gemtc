library(gemtc)

if (!exists("result")) {
	network <- read.mtc.network('~/Downloads/langford.gemtc')
	model <- mtc.model(network)
	result <- mtc.run(model)
}

source('blobbogram.R')

ts <- network$treatments$id
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

			md <- as.numeric(r2['mean'] - r1['mean'])
			var.1 <- (r1['std.dev'] / sqrt(r1['sampleSize']))^2
			var.2 <- (r2['std.dev'] / sqrt(r2['sampleSize']))^2
			se <- as.numeric(sqrt(var.1 + var.2))
			
			data$pe <- c(data$pe, md)
			data$ci.l <- c(data$ci.l, md - 1.96 * se)
			data$ci.u <- c(data$ci.u, md + 1.96 * se)
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
blobbogram(data, group.labels=group.labels, ci.label="Median (95% CrI)")
