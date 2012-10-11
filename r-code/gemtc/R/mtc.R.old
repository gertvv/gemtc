library(coda)

## mtc.network class methods
print.mtc.network <- function(x, ...) {
	cat("MTC dataset: ", x$description, "\n", sep="")
}

summary.mtc.network <- function(x, ...) {
	x
}

plot.mtc.network <- function(x, ...) {
	# FIXME: graph of treatments w/ comparisons as edges?
}

print.mtc.model <- function(x, ...) {
	cat("MTC ", x$type, " model: ", x$description, "\n", sep="")
}

# Read CODA output produced by JAGS
read.mtc <- function(stem) {
	nChains <- length(list.files(pattern=paste(stem, "chain*", sep="")))
    as.mcmc.list(lapply(1:nChains, function(i) {
        read.coda(
            paste(stem, "chain", i, ".txt", sep=""),
            paste(stem, "index.txt", sep=""))
    }))
}

# Get all data for the relevant parameter, merging the chains
get.parameter <- function(data, param) {
	unlist(lapply(data, function(chain) { chain[,param] }))
}

split.dir <- function(t1, t2) {
	paste("d.", t1, ".", t2, ".dir", sep="")
}

split.ind <- function(t1, t2) {
	paste("d.", t1, ".", t2, ".ind", sep="")
}

# Calculate the node-split P value for split node d.t1.t2
nodeSplitP <- function(data, t1, t2) {
	dir <- split.dir(t1, t2)
	ind <- split.ind(t1, t2)

	cnt <- function(chain) { chain[,dir] > chain[,ind] }

	nChains <- length(data)
	nSamples <- dim(data[[1]])[1]
	f <- sum(unlist(lapply(data, cnt)))/(nChains*nSamples)
	2 * min(f, 1 - f)
}

# Give node-split P value and convergence diagnostics for all *.splt CODA
# outputs found in the current directory.
nodeSplitSummary <- function(stem) {
	pattern <- paste(stem, "\\.splt\\.(.*)index\\.txt", sep="")
	contrasts <- strsplit(sub(pattern, "\\1", list.files(pattern=pattern)), split="\\.")

	lapply(contrasts, function(x) {
		str <- paste(x[1], x[2], sep=".")
		data <- read.mtc(paste(stem, ".splt.", str, sep=""))
		p <- nodeSplitP(data, x[1], x[2])
		dir <- quantile(get.parameter(data, split.dir(x[1], x[2])), probs=c(0.5, 0.025, 0.975))
		ind <- quantile(get.parameter(data, split.ind(x[1], x[2])), probs=c(0.5, 0.025, 0.975))
		list(node=paste("d.", str, sep=""), psrf=max(gelman.diag(data)$psrf), P=p, dir=dir, ind=ind)
	})
}

# Takes MCMC output data (mcmc.list) and a list of derivation functions, and
# appends the derived vectors to the data.
append.derived <- function(data, deriv) {
	derivChain <- function(chain, deriv) {
		nSamples <- dim(chain)[1]
		derived <- lapply(names(deriv), function(name) { deriv[[name]](chain) })
		y <- array(unlist(derived), dim=c(nSamples, length(deriv)))
		colnames(y) <- names(deriv)
		as.mcmc(cbind(chain, y))
	}
	as.mcmc.list(lapply(data, function(chain) { derivChain(chain, deriv) }))
}

# Identity function
id <- function(x) { x }

format.numbers <- function(x, transform=id) {
	sprintf("%0.2f", transform(x))
}

# Get formatted quantiles
format.quantile <- function (data, probs=c(0.5, 0.025, 0.975), transform=id) {
    sprintf("%0.2f", transform(quantile(data, probs)))
}

# Get formatted relative effects
re.format <- function(data, transform) {
	xs <- format.quantile(data, transform=transform)
    paste(xs[1], " (", xs[2], ", ", xs[3], ")", sep="")
}
# Get formatted relative effects
re.formatMean <- function(data, transform) {
    xs <- format.quantile(data, c(0.5), transform)
    xs[1]
}
# Get formatted relative effects
re.formatCI <- function(data, transform) {
    xs <- format.quantile(data, c(0.025, 0.975), transform)
    paste("(", xs[1], ", ", xs[2], ")", sep="")
}

# Get the treatment comparison due to this parameter
get.treatments <- function(param) {
		parts <- unlist(strsplit(param, "\\."))
		if (parts[1] == "d") parts[c(-1)]
		else c()
}

# Get the treatments compared in this dataset
find.treatments <- function(data) {
	x <- unlist(lapply(colnames(data[[1]]), get.treatments))
	levels(as.factor(x))
}

# Takes MTC data (with all derived values) and formats a LaTeX summary table.
# By default it is assumed the data are log odds-ratios. To print linear scale
# summary tables, set transform=id.
# surround.tex gives an example of how to embed the table in your LaTeX
# document.
write.summaryTable <- function(data, file="", transform=exp, treatments=find.treatments(data)) {
	formatEffect <- function(t1, t2, i) {
		param <- paste("d", t1, t2, sep=".")
		myData <- NULL
		if (sum(colnames(data[[1]]) == param) == 0) {
			param <- paste("d", t2, t1, sep=".")
			myData <- -unlist(lapply(data, function(chain) { chain[,param] }))
		} else {
			myData <- unlist(lapply(data, function(chain) { chain[,param] }))
		}
		if (i == 1) re.formatMean(myData, transform)
		else paste("\\tiny{", re.formatCI(myData, transform), "}", sep="")
	}
	formatCell <- function(t1, t2, i) {
		if (t1 == t2) {
			if (i == 1) paste("\\multirow{2}{*}{", t1, "}", sep="")
			else ""
		}
		else formatEffect(t1, t2, i)
	}
	formatRowX <- function(t1, i) {
		paste(lapply(treatments, function(t2) { formatCell(t1, t2, i) }), collapse=" & ")
	}
	formatRow <- function(t1) {
		paste(formatRowX(t1, 1), formatRowX(t1, 2), "", sep="\\\\\n")
	}
	colSpec <- paste(lapply(treatments, function(x) { "c" }), collapse="|")
	tabStart <- paste("\\begin{tabular}{", colSpec, "}", sep="")
	tabEnd <- "\\end{tabular}"
	tabContent <- paste(lapply(treatments, formatRow), collapse="\\hline\n")
	cat(paste(tabStart, tabContent, tabEnd, sep="\n"), file=file)
}

write.nodeSplitSummaryTable <- function(stem) {
	formatRow <- function(row) {
		# node, psrf, P
		t <- get.treatments(row$node)
		psrf <- sprintf("%0.2f", row$psrf)
		p <- sprintf("%0.2f", row$P)
		dir <- format.numbers(row$dir)
		dir <- paste(dir[1], " (", dir[2], ", ", dir[3], ")", sep="", collapse="")
		ind <- format.numbers(row$ind)
		ind <- paste(ind[1], " (", ind[2], ", ", ind[3], ")", sep="", collapse="")
		paste(t[1], t[2], psrf, dir, ind, p, sep=" & ")
		
	}
	data <- nodeSplitSummary(stem)
	file <- paste(stem, "splt", "tex", sep=".")
	tabStart <- "\\begin{tabular}{ll|r|r|r|r}"
	tabHead <- "X & Y & PSRF & direct & indirect & P\\\\\n\\hline\n"
	tabContent <- paste(lapply(data, formatRow), collapse="\\\\\n")
	tabEnd <- "\\end{tabular}"
	cat(paste(tabStart, tabHead, tabContent, tabEnd, sep="\n"), file=file)
}
