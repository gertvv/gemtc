context("relative.effect.table")

test_that("it works for the smoking example", {
  smoking_result <- dget(system.file("extdata/luades-smoking.samples.gz", package="gemtc"))
  smoking_table <- relative.effect.table(smoking_result)

  # Check dimnames
  expect_that(rownames(smoking_table), equals(c("A", "B", "C", "D")))
  expect_that(colnames(smoking_table), equals(c("A", "B", "C", "D")))

  # Check that the diagonal contains NA
  expect_that(diag(smoking_table[,,2]), is_equivalent_to(as.numeric(rep(NA, 4))))

  # Check that off-diagonal entries contain the quantiles
  q <- function(q1, q2, q3) { c("2.5%"=q1, "50%"=q2, "97.5%"=q3) }
  expect_that(smoking_table[1,2,], equals(q(-0.29846826342809, 0.490982134423406, 1.34066639613713)))
  expect_that(smoking_table[1,3,], equals(q(0.387798548149361, 0.827333271108623, 1.3530539185826)))
  expect_that(smoking_table[1,4,], equals(q(0.269236199821778, 1.09825953831406, 2.00604408009687)))
  expect_that(smoking_table[2,3,], equals(q(-0.480946561958643, 0.341121326321452, 1.17021895949004)))
  expect_that(smoking_table[2,4,], equals(q(-0.308278534656184, 0.604352628567083, 1.57902423190838)))
  expect_that(smoking_table[3,4,], equals(q(-0.532179016795455, 0.261896715203374, 1.11556777887809)))
  expect_that(smoking_table[2,1,], equals(q(-1.34066639613713, -0.490982134423406, 0.29846826342809)))

  expect_that(attr(smoking_table, "model"), equals(smoking_result[['model']]))
  expect_that(smoking_table, is_a("mtc.relative.effect.table"))
})
