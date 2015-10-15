#' @include mtc.model.consistency.R

mtc.model.regression <- function(model, regressor='x') {
  style.tree <- function(tree) {
    tree <- set.edge.attribute(tree, 'arrow.mode', value=2)
    tree <- set.edge.attribute(tree, 'color', value='black')
    tree <- set.edge.attribute(tree, 'lty', value=1)
    tree
  }
  model[['tree']] <-
    style.tree(minimum.diameter.spanning.tree(mtc.network.graph(model[['network']])))

  model[['data']] <- mtc.model.data(model)
  model[['data']][['x']] <- regressorData(model[['network']], regressor)
  model[['inits']] <- mtc.init(model)

  model[['code']] <- mtc.model.code(model, mtc.basic.parameters(model), consistency.relative.effect.matrix(model),
                                    linearModel='delta[i, k] + (beta[t[i, k]] - beta[t[i, 1]]) * x[i]',
                                    regressionPriors='\n# Regression priors\nbeta[1] <- 0\nfor (k in 2:nt) {\n  beta[k] <- B\n}\nB ~ dnorm(0, prior.prec)\n')

  monitors <- inits.to.monitors(model[['inits']][[1]])
  model[['monitors']] <- list(
    available=monitors,
    enabled=c(monitors[grep('^d\\.', monitors)], monitors[grep('^sd.d$', monitors)], 'B')
  )

  class(model) <- "mtc.model"

  model
}

regressorData <- function(network, regressor) {
  data <- mtc.merge.data(network)
  studies <- unique(data[['study']])
  unname(sapply(studies, function(study) {
    sel <- network[['data.ab']][['study']] == study
    if (any(sel)) {
      network[['data.ab']][[regressor]][sel][1]
    } else {
      sel <- network[['data.re']][['study']] == study
      network[['data.re']][[regressor]][sel][1]
    }
  }))
}

mtc.model.name.regression <- function(model) {
  "meta-regression"
}

func.param.matrix.regression <- function(model, t1, t2) {
  tree.relative.effect(model[['tree']], t1, t2)
}
