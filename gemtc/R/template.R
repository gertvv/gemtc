template.block.sub <- function(template, var, val) {
	matches <- regexec(paste("([ \t]*)[^\\$\n]*\\$", var, "\\$",sep=""), template)[[1]]
	start <- matches[2]
	len <- attr(matches, "match.length")[2]
	indent <- substr(template, start, start + len - 1)
	val <- paste(strsplit(val, "\n")[[1]], collapse=paste("\n", indent, sep=""))
	sub(paste('$', var, '$', sep=''), val, template, fixed=TRUE)
}
