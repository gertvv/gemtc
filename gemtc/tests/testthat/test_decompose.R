context("Multi-arm trial decomposition")

test_that("Fixed effects MA recovers variances", {
  # All pair-wise relative effects and their variances
  mu <- matrix(c(0.000000, -1.090708, -0.131780, 1.0907081, 0.0000000, 0.9589281, 0.1317800, -0.9589281, 0.0000000), nrow=3)
  V <- matrix(c(0.0000000, 0.1627065, 0.2316605, 0.1627065, 0.0000000, 0.1604229, 0.2316605, 0.1604229, 0.0000000), nrow=3)
  # Decomposed variances
  v <- decompose.variance(V)

  expect_that(diag(v), equals(rep(0, 3)))
  expect_that(t(v), equals(v))

  fe.3arm <- function(t1, t2, t3) {
    mu.dir <- mu[t1, t2]
    mu.ind <- mu[t1, t3] + mu[t3, t2]
    V.dir <- v[t1, t2]
    V.ind <- v[t1, t3] + v[t3, t2]
    V.pool <- 1 / (1/V.dir + 1/V.ind)
    mu.pool <- (1/V.dir * mu.dir + 1/V.ind * mu.ind) * V.pool
    list(mu=mu.pool, V=V.pool)
  }

  d12 <- fe.3arm(1, 2, 3)
  d13 <- fe.3arm(1, 3, 2)
  d23 <- fe.3arm(2, 3, 1)
  expect_that(c(d12$mu, d13$mu, d23$mu), equals(c(mu[1,2], mu[1,3], mu[2,3]), tolerance=1E-7))
  expect_that(c(d12$V, d13$V, d23$V), equals(c(V[1,2], V[1,3], V[2,3]), tolerance=1E-7))
});
