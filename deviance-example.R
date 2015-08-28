data <- read.table(textConnection('
study treatment responders sampleSize
1 1 3 39
1 2 3 38
2 1 14 116
2 2 7 114
3 1 11 93
3 2 5 69
4 1 127 1520
4 2 102 1533
5 1 27 365
5 2 28 355
6 1 6 52
6 2 4 59
7 1 152 939
7 2 98 945
8 1 48 471
8 2 60 632
9 1 37 282
9 2 25 278
10 1 188 1921
10 2 138 1916
11 1 52 583
11 2 64 873
12 1 47 266
12 2 45 263
13 1 16 293
13 2 9 291
14 1 45 883
14 2 57 858
15 1 31 147
15 2 25 154
16 1 38 213
16 2 33 207
17 1 12 122
17 2 28 251
18 1 6 154
18 2 8 151
19 1 3 134
19 2 6 174
20 1 40 218
20 2 32 209
21 1 43 364
21 2 27 391
22 1 39 674
22 2 22 680'), header=T)

network <- mtc.network(data.ab = data)
#network <- read.mtc.network(system.file('extdata/luades-smoking.gemtc', package='gemtc'))
#network <- mtc.network(data.re=read.table('gemtc/tests/data/parkinson-diff.data.txt', header=TRUE))
#network <- mtc.network(data.ab=read.table('gemtc/tests/data/parkinson-shared.data-ab.txt', header=TRUE),
#                       data.re=read.table('gemtc/tests/data/parkinson-shared.data-re.txt', header=TRUE))
#model <- mtc.model(network, linearModel='fixed', likelihood='normal', link='identity')
#model <- mtc.model(network, linearModel='random', likelihood='normal', link='identity')
model <- mtc.model(network, linearModel='fixed')

armBasedSeq <- if (model$data$ns.a > 0) 1:model$data$ns.a else c()
contrastSeq <- if (model$data$ns.r2 > 0 || model$data$ns.rm > 0) (1:(model$data$ns.r2 + model$data$ns.rm)) + model$data$ns.a else c()
dev.params <- do.call(c, c(
  lapply(armBasedSeq, function(i) { paste0("dev[", i, ",", 1:model$data$na[i], "]") }),
  lapply(contrastSeq, function(i) { paste0("dev[", i, ",", 1, "]") })))

fpname <- c("binom"="rhat", "normal"="theta", "poisson"="theta")[model$likelihood]
fitted.params.ab <- do.call(c, lapply(armBasedSeq, function(i) { paste0(fpname, "[", i, ",", 1:model$data$na[i], "]") }))
fitted.params.re <- do.call(c, lapply(contrastSeq, function(i) { paste0("delta[", i, ",", 2:model$data$na[i], "]") }))

model$monitors$enabled <- c(model$monitors$enabled, dev.params, fitted.params.ab, fitted.params.re)

result <- mtc.run(model)

deviance <- as.matrix(result$samples[, dev.params])
devbar <- apply(deviance, 2, mean)
Dbar <- sum(devbar)

fit.ab <- if (model$data$ns.a > 0) {
  fitted <- as.matrix(result$samples[, fitted.params.ab])
  apply(fitted, 2, mean)
}
fit.re <- if(model$data$ns.r2 > 0 || model$data$ns.rm > 0) {
  fitted <- as.matrix(result$samples[, fitted.params.re])
  apply(fitted, 2, mean)
}
rm(fitted)

fixna <- function(x, v) {
  x[is.na(x)] <- v
  x
}

devfit.binom <- function(data, rfit) {
  r <- as.vector(t(data$r))
  r <- r[!is.na(r)]

  n <- as.vector(t(data$n))
  n <- n[!is.na(n)]

  2 * (fixna(r * log(r / rfit), 0) + fixna((n - r) * log((n - r) / (n - rfit)), 0))
}

devfit.normal <- function(data, mfit) {
  m <- as.vector(t(data$m[1:data$ns.a,,drop=FALSE]))
  m <- m[!is.na(m)]
  e <- as.vector(t(data$e[1:data$ns.a,,drop=FALSE]))
  e <- e[!is.na(e)]
  (m - mfit)^2 / e^2
}

devfit.re <- function(data, mfit) {
  s <- if (data$ns.r2 > 0 || data$ns.rm > 0) (1:(data$ns.r2 + data$ns.rm)) + data$ns.a else c()
  sapply(s, function(i) {
    na <- data$na[i]
    start <- sum(data$na[(data$ns.a + 1):i] - 1) - na + 2
    ifit <- mfit[start:(start + na - 2)]

    cov <- if (!is.na(data$e[i, 1])) data$e[i, 1]^2 else 0
    Sigma <- matrix(cov, nrow=(na-1), ncol=(na-1))
    diag(Sigma) <- data$e[i, 2:na]^2
    Omega <- solve(Sigma)
    m <- data$m[i, 2:na]

    mdiff <- m - ifit

    t(mdiff) %*% Omega %*% mdiff
  })
}

devfit.fn <- list("binom"=devfit.binom, "normal"=devfit.normal)[[model$likelihood]]
devfit <- unlist(c(devfit.fn(model$data, fit.ab), devfit.re(model$data, fit.re)))
leverage <- devbar - devfit
pD <- sum(leverage)

DIC <- Dbar + pD

## Below not generalized for multi-arm data

w <- sign(r - rfit) * sqrt(devbar)

plot(w, leverage, xlim=c(-3,3), ylim=c(0, 4.5))
x <- seq(from=-3, to=3, by=0.05)
for (c in 1:4) { lines(x, c - x^2) }
