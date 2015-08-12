context("mtc.model.nodesplit")

test_that("func.param.matrix was implemented correctly", {
  model <- list(
    'type'='nodesplit',
    'tree.indirect'=igraph::make_graph(c('D', 'A', 'D', 'B', 'D', 'C', 'D', 'E')),
    'split'=list(t1='A', t2='C'))

  expect_equal(matrix(c(0,0,0,0,1), nrow=5, dimnames=list(NULL, 'd.A.C')),
               mtc.model.call('func.param.matrix', model, t1='A', t2='C'))
  expect_equal(matrix(c(0,0,0,0,-1), nrow=5, dimnames=list(NULL, 'd.C.A')),
               mtc.model.call('func.param.matrix', model, t1='C', t2='A'))
  expect_equal(matrix(c(0,0,0,0,0), nrow=5, dimnames=list(NULL, 'd.A.A')),
               mtc.model.call('func.param.matrix', model, t1='A', t2='A'))
  expect_equal(matrix(c(-1,1,0,0,0), nrow=5, dimnames=list(NULL, 'd.A.B')),
               mtc.model.call('func.param.matrix', model, t1='A', t2='B'))
})
