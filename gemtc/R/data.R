arm.index.matrix <- function(network) {
  studies <- mtc.studies.list(network)
  all.studies <- inverse.rle(studies)
  n <- max(studies[['lengths']])
  t(sapply(studies[['values']], function(study) {
    v <- which(all.studies == study)
    length(v) <- n
    v
  }))
}

mtc.model.data <- function(model) {
  data.ab <- model[['network']][['data.ab']]
  data.re <- model[['network']][['data.re']]

  columns.ab <- ll.call('required.columns.ab', model)
  if (!(is.null(data.ab) || all(columns.ab %in% colnames(data.ab)))) {
    stop(paste(
      'likelihood =', model[['likelihood']],
      'link =', model[['link']], 'requires columns:', paste(columns.ab, collapse=', '), 'on data'))
  }
  columns.re <- c('m'='diff', 'e'='std.err')
  if (!(is.null(data.re) || all(columns.re %in% colnames(data.re)))) {
    stop(paste(
      'likelihood =', model[['likelihood']],
      'link =', model[['link']], 'requires columns:', paste(columns.ab, collapse=', '), 'on data.re'))
   }

  data <- data.frame(
    t=c(data.ab[['treatment']], data.re[['treatment']])
  )
  nrow.ab <- if (!is.null(data.ab)) nrow(data.ab) else 0
  nrow.re <- if (!is.null(data.re)) nrow(data.re) else 0
  if (nrow.ab > 0) {
    for (column in names(columns.ab)) {
      data[[column]] <- NA
      coldata <- data.ab[[columns.ab[column]]]
      if (any(is.na(coldata))) {
        stop(paste('data.ab contains NAs in column "', columns.ab[column], '"', sep=""))
      }
      data[[column]][1:nrow.ab] <- data.ab[[columns.ab[column]]]
    }
  }
  if (nrow.re > 0) {
    for (column in names(columns.re)) {
      if (!(column %in% colnames(data))) {
        data[[column]] <- NA
      }
      data[[column]][(nrow.ab + 1):(nrow.ab + nrow.re)] <- data.re[[columns.re[column]]]
    }
    mtc.validate.data.re(data.re)
  }

  studies.ab <- rle(as.character(data.ab[['study']]))[['values']]
  studies.re <- rle(as.character(data.re[['study']]))[['values']]
  studies <- c(studies.ab, studies.re)
  study.arms <- c(as.character(data.ab[['study']]), as.character(data.re[['study']]))
  na <- sapply(studies, function(study) { sum(study.arms == study) })
  na.re <- if (length(studies.re) > 0 ) na[(length(studies.ab)+1):length(studies)] else c()
  s.mat <- arm.index.matrix(model[['network']])

  model.data <- lapply(data, function(column) { matrix(column[s.mat], nrow=nrow(s.mat)) })
  names(model.data) <- colnames(data)

  ns.a <- length(studies.ab)
  ns.r2 <- sum(na.re == 2)
  ns.rm <- sum(na.re > 2)
  model.data <- c(model.data, list(
    na = unname(na),
    nt = nrow(model[['network']][['treatments']]),
    om.scale = model[['om.scale']]))

  powerAdjust <- model[['powerAdjust']]
  if (!is.null(powerAdjust) && !is.na(powerAdjust)) {
    studyData <- model[['network']][['studies']]
    alpha <- studyData[[powerAdjust]]
    names(alpha) <- studyData[['study']]
    alpha <- alpha[as.character(studies)]
    model.data <- c(model.data, list(alpha=alpha))

    if (model[['likelihood']] != 'normal') {
      model.data <- c(model.data, list(zero=matrix(0, ncol=max(na), nrow=length(studies.ab))))
    }
  } else {
    alpha <- rep(1, length(studies))
  }

  studies.a <- if (ns.a > 0) 1:ns.a else numeric()
  studies.a <- studies.a[alpha[studies.a] > 0]
  studies.r2 <- if (ns.r2 > 0) ns.a + (1:ns.r2) else numeric()
  studies.r2 <- studies.r2[alpha[studies.r2] > 0]
  studies.rm <- if (ns.rm > 0) ns.a + ns.r2 + (1:ns.rm) else numeric()
  studies.rm <- studies.rm[alpha[studies.rm] > 0]
  # conditional assigns necessary until JAGS accepts zero-length vectors
  if (length(studies.a) > 0) {
    model.data[['studies.a']] <- studies.a
  }
  if (length(studies.r2) > 0) {
    model.data[['studies.r2']] <- studies.r2
  }
  if (length(studies.rm) > 0) {
    model.data[['studies.rm']] <- studies.rm
  }
  model.data[['studies']] <- c(studies.a, studies.r2, studies.rm)

  model.data
}
