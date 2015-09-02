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

# Binom/logit (blockers)
#network <- mtc.network(data.ab = data)
#model <- mtc.model(network, linearModel='fixed')

# Binom/logit (smoking)
#network <- read.mtc.network(system.file('extdata/luades-smoking.gemtc', package='gemtc'))
#model <- mtc.model(network, linearModel='fixed')

# Relative effect data
#network <- mtc.network(data.re=read.table('gemtc/tests/data/parkinson-diff.data.txt', header=TRUE))
#model <- mtc.model(network, linearModel='fixed', likelihood='normal', link='identity')
#model <- mtc.model(network, linearModel='random', likelihood='normal', link='identity')

# Mixed data
network <- mtc.network(data.ab=read.table('gemtc/tests/data/parkinson-shared.data-ab.txt', header=TRUE),
                       data.re=read.table('gemtc/tests/data/parkinson-shared.data-re.txt', header=TRUE))
model <- mtc.model(network, linearModel='fixed', likelihood='normal', link='identity')
#model <- mtc.model(network, linearModel='random', likelihood='normal', link='identity')


# Poisson/log (dietary fat, 2b)
#network <- mtc.network(dget('gemtc/tests/data/fat-survival.data.txt'))
# ... edit
#model <- mtc.model(network, linearModel='fixed', likelihood='poisson', link='log')

result <- mtc.run(model)

summary(result)

print(result$deviance)

## Below not generalized for multi-arm data

# w <- sign(r - rfit) * sqrt(devbar)

# plot(w, leverage, xlim=c(-3,3), ylim=c(0, 4.5))
# x <- seq(from=-3, to=3, by=0.05)
# for (c in 1:4) { lines(x, c - x^2) }

## residual deviance plot
if (model$data$ns.r2 + model$data$ns.rm == 0) {
  tpl <- gemtc:::arm.index.matrix(model[['network']])
  study <- matrix(rep(1:nrow(tpl), times=ncol(tpl)), nrow=nrow(tpl), ncol=ncol(tpl))
  study <- t(study)[t(!is.na(tpl))]
  devbar <- t(result$deviance$dev.ab)[t(!is.na(tpl))]
  title <- "Per-arm residual deviance"
  xlab <- "Arm"
} else {
  nd <- model$data$na
  nd[-(1:model$data$ns.a)] <- nd[-(1:model$data$ns.a)] - 1
  devbar <- c(apply(result$deviance$dev.ab, 1, sum, na.rm=TRUE), result$deviance$dev.re) / nd
  study <- 1:length(devbar)
  title <- "Per-study mean per-datapoint residual deviance"
  xlab <- "Study"
}

plot(devbar, ylim=c(0,max(devbar, na.rm=TRUE)),
     ylab="Residual deviance", xlab=xlab,
     main=title, pch=c(1, 22)[(study%%2)+1])

for (i in 1:length(devbar)) {
  lines(c(i, i), c(0, devbar[i]))
}

# w <- sign(r - rfit) * sqrt(devbar)

# plot(w, leverage, xlim=c(-3,3), ylim=c(0, 4.5))
# x <- seq(from=-3, to=3, by=0.05)
# for (c in 1:4) { lines(x, c - x^2) }
fit.ab <- apply(result$deviance$fit.ab, 1, sum, na.rm=TRUE)
dev.ab <- apply(result$deviance$dev.ab, 1, sum, na.rm=TRUE)
lev.ab <- dev.ab - fit.ab
fit.re <- result$deviance$fit.re
dev.re <- result$deviance$dev.re
lev.re <- dev.re - fit.re
nd <- model$data$na
nd[-(1:model$data$ns.a)] <- nd[-(1:model$data$ns.a)] - 1
w <- sqrt(c(dev.ab, dev.re) / nd)
lev <- c(lev.ab, lev.re) / nd

plot(w, lev, xlim=c(0, max(c(w, 2.5))), ylim=c(0, max(c(lev, 4))),
     xlab="Square root of residual deviance", ylab="Leverage",
     main="Leverage versus residual deviance")
mtext("Per-study mean per-datapoint contribution")

x <- seq(from=0, to=3, by=0.05)
for (c in 1:4) { lines(x, c - x^2) }
