mtc.data.studyrow <- function(data,
    armVars=c('treatment'='t', 'responders'='r', 'sampleSize'='n'),
    nArmsVar='na', studyVars=c(),
    studyNames=1:nrow(data), treatmentNames=NA,
    patterns=c('%s..', '%s..%d.')) {

  nArmsCol <- sprintf(patterns[1], nArmsVar)
  studyCols <- sprintf(patterns[1], studyVars)

  out <- do.call(rbind, lapply(1:nrow(data), function(i) {
    row <- data[i,]
    na <- row[nArmsCol]
    studyEntries <- row[studyCols]
    names(studyEntries) <- names(studyVars)
    do.call(rbind, lapply(1:unlist(na), function(j) {
      armCols <- sprintf(patterns[2], armVars, j)
      armEntries <- row[armCols]
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

# Example: TSD2-3aRE_Bi_cloglog.odc
studyData <- read.table('studyrow.txt', header=TRUE)
studies <- c('MRC-E', 'EWPH', 'SHEP', 'HAPPHY', 'ALLHAT', 'INSIGHT', 'ANBP-2', 'ALPINE', 'FEVER', 'DREAM', 'HOPE', 'PEACE', 'CHARM', 'SCOPE', 'AASK', 'STOP-2', 'ASCOT', 'NORDIL', 'INVEST', 'CAPPP', 'LIFE', 'VALUE')
treatments <- c('Diuretic', 'Placebo', 'BetaB', 'CCB', 'ACEi', 'ARB')
data <- mtc.data.studyrow(studyData, studyVars=c('time'='time'), studyNames=studies, treatmentNames=treatments)
network <- mtc.network(data.ab=data)
model <- mtc.model(network, likelihood='binom', link='cloglog')
result <- mtc.run(model)
print(summary(relative.effect(result, t1='Diuretic')))

#  node  mean  sd  MC error 2.5%  median  97.5% start sample
#  d[2]  -0.2887 0.08888 3.992E-4  -0.4737 -0.2853 -0.1225 50001 300000   --> d.Diuretic.Placebo
#  d[3]  -0.0744 0.08797 3.766E-4  -0.2514 -0.07342  0.09628 50001 300000 --> d.Diuretic.BetaB
#  d[4]  -0.2413 0.0842  3.308E-4  -0.4089 -0.2408 -0.07573  50001 300000 --> d.Diuretic.CCB
#  d[5]  -0.4023 0.08557 3.484E-4  -0.5791 -0.3997 -0.2403 50001 300000   --> d.Diuretic.ACEi
#  d[6]  -0.4744 0.1106  4.619E-4  -0.7042 -0.4709 -0.2652 50001 300000   --> d.Diuretic.ARB
