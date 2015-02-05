context("mtc.study.treatment.matrix")

test_that('mtc.study.treatment.matrix with 1 study', {
  data <- data.frame(cbind(c('s02', 's02'), c('A', 'B')))
  colnames(data) <- c('study', 'treatment')
  network <- mtc.network(data)
  m <- mtc.study.treatment.matrix(network)
  expect_equal(1, nrow(m))
})

test_that('mtc.study.treatment.matrix with 2 studies', {
  data <- data.frame(cbind(c('s02', 's02'), c('A', 'B')))
  data <- rbind(data, data.frame(cbind(c('s01', 's01'), c('A', 'B'))))
  
  colnames(data) <- c('study', 'treatment')
  network <- mtc.network(data)
  m <- mtc.study.treatment.matrix(network)
  expect_equal(2, nrow(m))
})

       
