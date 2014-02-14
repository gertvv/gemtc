mtc.data.studyrow <- function(data,
    armVars=c('treatment'='t', 'responders'='r', 'sampleSize'='n'),
    nArmsVar='na', studyVars=c(),
    studyNames=1:nrow(data), treatmentNames=NA,
    patterns=c('%s..', '%s..%d.')) {

  colsOrNA <- function(row, cols) {
    rval <- rep(NA, length(cols))
    sel <- cols %in% colnames(row)
    rval[sel] <- row[cols[sel]]
    rval
  }

  nArmsCol <- sprintf(patterns[1], nArmsVar)
  studyCols <- sprintf(patterns[1], studyVars)

  out <- do.call(rbind, lapply(1:nrow(data), function(i) {
    row <- data[i,]
    na <- row[nArmsCol]
    studyEntries <- row[studyCols]
    names(studyEntries) <- names(studyVars)
    do.call(rbind, lapply(1:unlist(na), function(j) {
      armCols <- sprintf(patterns[2], armVars, j)
      armEntries <- colsOrNA(row, armCols)
      names(armEntries) <- names(armVars)
      c(list('study'=i), studyEntries, armEntries)
    }))
  }))

  colNames <- colnames(out)
  out <- lapply(colnames(out), function(col) {
    unlist(out[,col])
  })
  names(out) <- colNames

  out[['study']] <- studyNames[out[['study']]]
  if (all(!is.na(treatmentNames))) {
    out[['treatment']] <- treatmentNames[out[['treatment']]]
  }
  do.call(data.frame, out)
}
