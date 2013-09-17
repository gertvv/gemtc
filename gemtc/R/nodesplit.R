# Change-of-Baseline matrix
# old: vector of old arms, first element the baseline
# new: vector of new arms, first element the baseline
cob.matrix <- function(old, new) {
  stopifnot(length(old) >= length(new))
  n <- length(old) - 1
  m <- length(new) - 1
  b <- matrix(0, nrow=m, ncol=n)
  for (i in 1:m) {
    for (j in 1:n) {
      if (old[j + 1] == new[i + 1]) b[i, j] <- 1
      if (old[j + 1] == new[1]) b[i, j] <- -1
    }
  }
  b
}

nodesplit.rewrite.data.ab <- function(data, t1, t2) {
  t1 <- as.character(t1)
  t2 <- as.character(t2)
  studies <- unique(data$study)
  per.study <- lapply(studies, function(study) {
    study.data <- data[data$study == study, ]
    study.data$study <- as.character(study.data$study)
    has.both <- all(c(t1, t2) %in% study.data$treatment)
    if (nrow(study.data) == 3 && has.both) {
      study.data <- study.data[study.data$treatment %in% c(t1, t2), ]
    } else if (nrow(study.data) > 3 && has.both) {
      sel <- study.data$treatment %in% c(t1, t2)
      study.data$study[sel] <- paste(study, '*', sep='')
      study.data$study[!sel] <- paste(study, '**', sep='')
    }
    study.data
  })
  data <- do.call(rbind, per.study)
  data$study <- as.factor(data$study)
  data
}

nodesplit.rewrite.data.re <- function(data, t1, t2) {
  t1 <- as.character(t1)
  t2 <- as.character(t2)
  cob <- function(study, data, ts) {
    old <- as.character(data$treatment)
    new <- c(ts, old[!(old %in% ts)])
    n <- length(old) - 1
    m <- length(ts)
    b <- cob.matrix(old, new)
    mean <- c(NA, b %*% data$mean[-1])[1:m]

    cov.m <- matrix(data$std.err[1], nrow=n, ncol=n)
    diag(cov.m) <- data$std.err[-1]
    cov.m <- b %*% cov.m %*% t(b)
    std.err <- c(cov.m[1, 2], diag(cov.m))[1:m]

    treatment <- factor(ts, levels=levels(data$treatment))
    data.frame(study=rep(study, m),
               treatment=treatment,
               mean=mean,
               std.err=std.err)
  }
  studies <- unique(data$study)
  per.study <- lapply(studies, function(study) {
    study.data <- data[data$study == study, ]
    study.data$study <- as.character(study.data$study)
    has.both <- all(c(t1, t2) %in% study.data$treatment)
    if (nrow(study.data) == 3 && has.both) {
      study.data <- cob(study, study.data, c(t1, t2))
    } else if (nrow(study.data) > 3 && has.both) {
      ts <- as.character(study.data$treatment)
      ts <- ts[!(ts %in% c(t1, t2))]
      study.data <- rbind(cob(paste(study, '*', sep=''), study.data, c(t1, t2)),
                          cob(paste(study, '**', sep=''), study.data, ts))
    }
    study.data
  })
  data <- do.call(rbind, per.study)
  data$study <- as.factor(data$study)
  print(data)
  data
}
