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

# Calculate the node-split P value for split node d.t1.t2
nodeSplitP <- function(data, t1, t2) {
	dir <- paste("d.", t1, ".", t2, ".dir", sep="")
	ind <- paste("d.", t1, ".", t2, ".ind", sep="")

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
		list(node=str, psrf=max(gelman.diag(data)$psrf), P=p)
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
