context("mtc.nr.comparisons")

test_that('mtc.nr.comparisons with a single study', {
  data <- data.frame(cbind(c('s02', 's02'), c('A', 'B')))
  colnames(data) <- c('study', 'treatment')
  network <- mtc.network(data)
  comparisons <- mtc.nr.comparisons(network)
  expect_identical(as.matrix(data.frame(t1='A', t2='B', nr=1)), as.matrix(comparisons))
})

test_that('mtc.nr.comparisons with 2 studies', {
  data <- data.frame(cbind(c('s02', 's02'), c('A', 'B')))
  data <- rbind(data, data.frame(cbind(c('s01', 's01'), c('A', 'B'))))  
  colnames(data) <- c('study', 'treatment')
  network <- mtc.network(data)
  comparisons <- mtc.nr.comparisons(network)
  expect_identical(as.matrix(data.frame(t1='A', t2='B', nr=2)), as.matrix(comparisons))
})

