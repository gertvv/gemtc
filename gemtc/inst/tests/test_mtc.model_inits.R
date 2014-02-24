
test_that("mtc.model.inits has correct shape", {
  data.ab <- read.table(textConnection('
study treatment mean std.err
1     A         10.5 0.18
1     B         15.3 0.17
2     B         15.7 0.12
2     C         18.3 0.15
3     B         13.1 0.19
3     C         14.2 0.20'), header=T)
  data.re <- read.table(textConnection('
study treatment diff std.err
4     A         NA   0.15
4     B         3.1  0.22
4     C         4.2  0.24
5     A         2.9  0.28
5     B         NA   NA
6     A         0.2  0.23
6     B         0.8  0.27
6     C         NA   0.16
7     A         NA   0.18
7     B         4.3  0.21
8     B         NA   NA
8     C         4.0  0.19'), header=T)
  network <- mtc.network(data.ab, data.re=data.re)
  model <- list(network=network, likelihood='normal', link='identity', om.scale=2.5, n.chain=4, var.scale=2.5, linearModel='random')
  inits <- mtc.init(model)
  whereNA <- rbind(
    c(NA, 1, NA), # 1
    c(NA, 1, NA), # 2
    c(NA, 1, NA), # 3
    c(NA, 1, NA), # 5
    c(NA, 1, NA), # 7
    c(NA, 1, NA), # 8
    c(NA, 1, 1),  # 4
    c(NA, 1, 1))  # 6

  expect_that(is.na(inits[[1]]$delta), equals(is.na(whereNA)))
  expect_that(is.na(inits[[2]]$delta), equals(is.na(whereNA)))
  expect_that(is.na(inits[[3]]$delta), equals(is.na(whereNA)))
  expect_that(is.na(inits[[4]]$delta), equals(is.na(whereNA)))
})
