template.block.sub <- function(template, var, val) {
  while (length(grep(paste0('$', var, '$'), template, fixed=TRUE)) > 0) {
    matches <- regexec(paste("([ \t]*)[^\\$\n]*\\$", var, "\\$",sep=""), template)[[1]]
    start <- matches[2]
    len <- attr(matches, "match.length")[2]
    indent <- substr(template, start, start + len - 1)
    val <- paste(strsplit(val, "\n")[[1]], collapse=paste("\n", indent, sep=""))
    template <- sub(paste0('$', var, '$'), val, template, fixed=TRUE)
  }
  template
}

read.template <- function(file.name) {
    fileName <- system.file(file.name, package='gemtc')
    readChar(fileName, file.info(fileName)[['size']])
}
