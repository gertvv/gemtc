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
