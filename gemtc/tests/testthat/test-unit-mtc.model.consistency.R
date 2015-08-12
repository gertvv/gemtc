context("mtc.model.consistency")

test_that("func.param.matrix was implemented correctly", {
  model <- list(
    'type'='consistency',
    'tree'=igraph::make_graph(c('A','B','A','C','B','D')))

  expect_equal(matrix(0, nrow=3, dimnames=list(NULL, 'd.A.A')),
               mtc.model.call('func.param.matrix', model, t1='A', t2='A'))
  expect_equal(matrix(c(1,0,0), nrow=3, dimnames=list(NULL, 'd.A.B')),
               mtc.model.call('func.param.matrix', model, t1='A', t2='B'))
  expect_equal(matrix(c(1,0,0,0,1,0), nrow=3, dimnames=list(NULL, c('d.A.B', 'd.A.C'))),
               mtc.model.call('func.param.matrix', model, t1='A', t2=c('B', 'C')))
  expect_equal(matrix(c(-1,0,0), nrow=3, dimnames=list(NULL, c('d.B.A'))),
               mtc.model.call('func.param.matrix', model, t1='B', t2='A'))
  expect_equal(matrix(c(1,0,1), nrow=3, dimnames=list(NULL, c('d.A.D'))),
               mtc.model.call('func.param.matrix', model, t1='A', t2='D'))
})
