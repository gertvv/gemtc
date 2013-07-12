all.pair.matrix <- function(m) {
    do.call(rbind, lapply(1:(m - 1), function(i) {
        do.call(rbind, lapply((i + 1):m, function(j) {
            c(i, j)
        }))
    }))
}

filter.network <- function(network, filter, filter.ab=filter, filter.re=filter) {
    data.ab <- if (!is.null(network[['data']])) {
        network[['data']][apply(network[['data']], 1, filter.ab), ]
    }
    data.re <- if (!is.null(network[['data.re']])) {
        network[['data.re']][apply(network[['data.re']], 1, filter.re), ]
    }
    mtc.network(data=data.ab, data.re=data.re)
}

# Decompose trials based on a consistency model and study.samples
decompose.trials <- function(result) {
    decompose.study <- function(samples) {
        na <- ncol(samples) + 1
        samples <- cbind(0, samples)
        mu <- sapply(1:na, function(i) {
            sapply(1:na, function(j) {
                mean(samples[ ,i] - samples[ ,j])
            })
        })
        # Effective variances
        V <- sapply(1:na, function(i) {
            sapply(1:na, function(j) {
                var(samples[ ,i] - samples[ ,j])
            })
        })

        J <- matrix(1/na, ncol=na, nrow=na)
        # Pseudo-inverse Laplacian
        Lt <- -0.5 * (V - (1 / na) * (V %*% J + J %*% V) + (1 / na^2) * (J %*% V %*% J))
        # Laplacian
        L <- solve(Lt - J) + J

        prec <- -L
        diag(prec) <- 0
        se <- sqrt(1/prec)

        pairs <- all.pair.matrix(na)
        list(
            mu = apply(pairs, 1, function(p) { mu[p[1], p[2]] }),
            se = apply(pairs, 1, function(p) { se[p[1], p[2]] })
        )
    }

    study.samples <- as.matrix(result$samples)
    studies <- unique(mtc.merge.data(result$model$network)$study)
    

    decomposed <- lapply(1:length(studies), function(i) {
        study <- mtc.study.design(result$model$network, studies[i])
        study <- study[!is.na(study)]
        na <- length(study)
        colIndexes <- grep(paste("delta[", i, ",", sep=""), colnames(study.samples), fixed=TRUE)
        if (na > 2) {
            data <- decompose.study(
                study.samples[, colIndexes])
            ts <- matrix(study[all.pair.matrix(na)], ncol=2)
            list(m=data$mu, e=data$se, t=ts)
        } else {
            samples <- study.samples[ , colIndexes]
            list(m=mean(samples), e=sd(samples), t=matrix(study, nrow=1))
        }
    })

    # (mu1, prec1): posterior parameters
    mu1 <- lapply(decomposed, function(x) { x$m })
    sigma1 <- lapply(decomposed, function(x) { x$e })
    #prec1 <- 1/sigma1^2

    # Factor out the priors
    #prec <- prec1 - prec0
    #mu <- (mu1 * prec1 - mu0 * prec0) * 1/prec
    #sigma <- sqrt(1/prec)

    ts <- lapply(decomposed, function(x) { x$t })

    studyNames <- mtc.studies.list(result$model$network)$values
    names(ts) <- studyNames
    names(mu1) <- studyNames
    names(sigma1) <- studyNames

    list(t=ts, m=mu1, e=sigma1)
}

# decomposes the given network's multi-arm trials into
# a series of (approximately) equivalent two-arm trials
decompose.network <- function(network, result=NULL, likelihood=NULL, link=NULL) {
    # find all multi-arm trials
    data <- mtc.merge.data(network)
    studies <- unique(data$study)
    studies <- studies[sapply(studies, function(study) { sum(data$study == study) > 2 })]

    if (is.null(result)) {
        ma.network <- filter.network(network, function(row) { row['study'] %in% studies })
        model <- mtc.model(ma.network, type='use', likelihood=likelihood, link=link)
        result <- mtc.run(model)
    }

    data <- decompose.trials(result)
    data.re <- do.call(rbind, lapply(studies, function(study) {
        do.call(rbind, lapply(1:length(data$m[[study]]), function(j) {
            ts <- data$t[[study]][j,]
            m <- data$m[[study]][j]
            e <- data$e[[study]][j]
            rbind(
                data.frame(study=paste(study, ts[1], ts[2], sep="__"), treatment=ts[1], diff=NA, std.err=NA),
                data.frame(study=paste(study, ts[1], ts[2], sep="__"), treatment=ts[2], diff=m, std.err=e)
            )
        }))
    }))

    ta.network <- filter.network(network, function(row) { !(row['study'] %in% studies) })
    mtc.network(data=ta.network[['data']], data.re=rbind(ta.network[['data.re']], data.re))
}

mtc.anohe <- function(network, likelihood=NULL, link=NULL, ...) {
    model.use <- mtc.model(network, type='use', likelihood=likelihood, link=link)
    result.use <- mtc.run(model.use, ...)

    network.decomp <- decompose.network(network, result=result.use, likelihood=likelihood, link=link)
    model.ume <- mtc.model(network.decomp, type='ume', likelihood=likelihood, link=link)
    result.ume <- mtc.run(model.ume, ...)

    model.cons <- mtc.model(network, type='consistency', likelihood=likelihood, link=link)
    result.cons <- mtc.run(model.cons, ...)

    result <- list(result.cons=result.cons, result.ume=result.ume, result.use=result.use)
    class(result) <- "mtc.anohe"
    result
}

print.mtc.anohe <- function(x, ...) {
	cat("Analysis of heterogeneity (mtc.anohe) object\n")
	for (name in names(x)) {
		cat(paste("$", name, ": ", class(x[[name]]), "\n", sep=""))
	}
}

plot.mtc.anohe <- function(x, ask=dev.interactive(orNone=TRUE), ...) {
	cat("Analysis of heterogeneity -- convergence plots\n")

	cat("Unrelated Study Effects (USE) model:\n")
	plot(x$result.use, ask=ask, ...)
	cat("Unrelated Mean Effects (UME) model: ")
	if (ask) {
		readline('Hit <Return> to see next plot:')
	}
	plot(x$result.ume, ask=ask, ...)
	cat("Consistency model: ")
	if (ask) {
		readline('Hit <Return> to see next plot:')
	}
	plot(x$result.cons, ask=ask, ...)
}

i.squared <- function (mu, se, x, df.adj=-1) {
    stopifnot(length(mu) == length(se))
    stopifnot(length(mu) == length(x))
    dev <- (mu - x)^2 # squared deviance
    q <- sum(dev * 1/se^2) # Cochran's Q
    100 * max(0, (q - length(mu) - df.adj) / q) # I^2
}

summary.mtc.anohe <- function(object, ...) {
	result.use <- object$result.use
	result.ume <- object$result.ume
	result.cons <- object$result.cons

    se <- decompose.trials(result.use)
    se$s <- lapply(names(se$e), function(s) { rep(s, length(se$e[[s]])) })
    se$s <- do.call(c, se$s)
    se$t <- do.call(rbind, se$t)
    se$m <- do.call(c, se$m)
    se$e <- do.call(c, se$e)
    studyEffects <- data.frame(study=se$s, t1=se$t[,1], t2=se$t[,2], pe=se$m, ci.l=se$m-1.96*se$e, ci.u=se$m+1.96*se$e, stringsAsFactors=FALSE)
	rownames(studyEffects) <- NULL

    ume.samples <- as.matrix(result.ume$samples)
    varNames <- colnames(ume.samples)
    varNames <- varNames[grep('^d\\.', varNames)]
    ume.samples <- ume.samples[,varNames]
    comps <- extract.comparisons(varNames)
    qs <- apply(ume.samples, 2, function(samples) { quantile(samples, c(0.025, 0.5, 0.975)) })
    pairEffects <- data.frame(t1=comps[,1], t2=comps[,2], pe=qs[2,], ci.l=qs[1,], ci.u=qs[3,])
	rownames(pairEffects) <- NULL

    cons.samples <- as.matrix(relative.effect(result.cons, t1=comps[,1], t2=comps[,2], preserve.extra=FALSE)$samples)
    qs <- apply(cons.samples, 2, function(samples) { quantile(samples, c(0.025, 0.5, 0.975)) })
    consEffects <- data.frame(t1=comps[,1], t2=comps[,2], pe=qs[2,], ci.l=qs[1,], ci.u=qs[3,])
	rownames(consEffects) <- NULL

	data <- studyEffects
    data$t1 <- as.character(data$t1)
    data$t2 <- as.character(data$t2)
    data$p <- sapply(1:nrow(data), function(i) {
        row <- data[i,]
        pairEffects$pe[pairEffects$t1 == row$t1 & pairEffects$t2 == row$t2]
    })
    data$c <- sapply(1:nrow(data), function(i) {
        row <- studyEffects[i,]
        consEffects$pe[consEffects$t1 == row$t1 & consEffects$t2 == row$t2]
    })
    data$se <- (data$ci.u - data$ci.l) / 3.92

    pairEffects <- pairEffects
    pairEffects$t1 <- as.character(pairEffects$t1)
    pairEffects$t2 <- as.character(pairEffects$t2)
    i2.pair <- apply(pairEffects, 1, function(row) {
        data2 <- data[data$t1 == row['t1'] & data$t2 == row['t2'],]
        if (nrow(data2) > 1) {
            i.squared(data2$pe, data2$se, data2[['p']])
        } else {
            NA
        }
    })
    i2.cons <- apply(pairEffects, 1, function(row) {
        data2 <- data[data$t1 == row['t1'] & data$t2 == row['t2'],]
        if (nrow(data2) > 1) {
            # FIXME: df depends on network
            i.squared(data2$pe, data2$se, data2[['c']])
        } else {
            NA
        }
    })

    i.sq <- data.frame(t1=pairEffects$t1, t2=pairEffects$t2, i2.pair=i2.pair, i2.cons=i2.cons)
    total <- list(i2.pair=i.squared(data$pe, data$se, data[['p']]), i2.cons=i.squared(data$pe, data$se, data[['c']]))
    result <- list(studyEffects=studyEffects, pairEffects=pairEffects, consEffects=consEffects, isquared.comp=i.sq, isquared.glob=total, cons.model=result.cons$model)
	class(result) <- 'mtc.anohe.summary'
	result
}

print.mtc.anohe.summary <- function(x, ...) {
	cat("Analysis of heterogeneity\n")
	cat("=========================\n\n")
	cat("Per-comparison I-squared:\n")
	cat("-------------------------\n\n")
	print(x$isquared.comp)
	cat("\nGlobal I-squared:\n")
	cat("-------------------------\n\n")
	print(as.data.frame(x$isquared.glob))
}

plot.mtc.anohe.summary <- function(x, ...) {
	stats <- x
    data <- list(id=character(), group=character(), pe=c(), ci.l=c(), ci.u=c(), style=factor(levels=c('normal','pooled')))
    group.labels <- character()

	studyEffects <- stats$studyEffects
	pairEffects <- stats$pairEffects
	consEffects <- stats$consEffects

	appendEstimates <- function(data, rows) {
		data$pe <- c(data$pe, rows$pe)
		data$ci.l <- c(data$ci.l, rows$ci.l)
		data$ci.u <- c(data$ci.u, rows$ci.u)
		data
	}

	for (i in 1:nrow(pairEffects)) {
		t1 <- pairEffects$t1[i]
		t2 <- pairEffects$t2[i]

		param <- paste('d', t1, t2, sep='.')
		group.labels[param] <- paste(t2, 'vs', t1)

		# Study-level effects
		rows <- studyEffects[studyEffects$t1 == t1 & studyEffects$t2 == t2, ]
		data$id <- c(data$id, rows$study)
		data$group <- c(data$group, rep(param, nrow(rows)))
		data$style <- c(data$style, rep('normal', nrow(rows)))
		data <- appendEstimates(data, rows)

		# Pair-wise pooled effect
		rows <- pairEffects[i, ]
		data$id <- c(data$id, 'Pooled (pair-wise)')
		data$group <- c(data$group, param)
		data$style <- c(data$style, 'pooled')
		data <- appendEstimates(data, rows)

		# Network pooled effect
		rows <- consEffects[consEffects$t1 == t1 & consEffects$t2 == t2, ]
		data$id <- c(data$id, 'Pooled (network)')
		data$group <- c(data$group, param)
		data$style <- c(data$style, 'pooled')
		data <- appendEstimates(data, rows)
	}

	data <- as.data.frame(data)
	blobbogram(data, group.labels=group.labels,
		ci.label=paste(ll.call("scale.name", x$cons.model), "(95% CrI)"),
		log.scale=ll.call("scale.log", x$cons.model), ...)
}
