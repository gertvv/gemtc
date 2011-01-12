library(coda)

# Read CODA output produced by JAGS
read.mtc <- function(stem) {
	nChains <- length(list.files(pattern=paste(stem, "chain*", sep="")))
    as.mcmc.list(lapply(1:nChains, function(i) {
        read.coda(
            paste(stem, "chain", i, ".txt", sep=""),
            paste(stem, "index.txt", sep=""))
    }))
}

nodeSplitP <- function(data, t1, t2) {
	dir <- paste("d.", t1, ".", t2, ".dir", sep="")
	ind <- paste("d.", t1, ".", t2, ".ind", sep="")
	nChains <- length(data)
	nSamples <- dim(data[[1]])[1]

	cnt <- function(i) { data[[i]][,dir]>data[[i]][,ind] }

	f <- sum(unlist(lapply(1:nChains, cnt)))/(nChains*nSamples)
	2 * min(f, 1 - f)
}

nodeSplitSummary <- function(stem) {
	pattern <- paste(stem, "\\.splt\\.(.*)index\\.txt", sep="")
	contrasts <- strsplit(sub(pattern, "\\1", list.files(pattern=pattern)), split="\\.")

	lapply(contrasts, function(x) {
		str <- paste(x[1], x[2], sep=".")
		data <- read.mtc(paste(stem, ".splt.", str, sep=""))
		p <- nodeSplitP(data, x[1], x[2])
		list(node=str, psrf=max(gelman.diag(data)$psrf), P=p)
	})
}
