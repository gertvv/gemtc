source('smoking.analysis.R')
tbl <- data.frame(mean=sapply(data, mean), sd=sapply(data, sd))
latex(round(tbl, digits=3), title="parameter", file='smoking.tbl.tex')
