# Unrelated study effects model
mtc.model.use <- function(model) {
  model[['linearModel']] <- 'random'

  model[['data']] <- mtc.model.data(model)
  model[['data']][['nt']] <- NULL
  model[['data']][['t']] <- NULL
  model[['inits']] <- lapply(mtc.init(model), function(inits) {
    inits[-which(names(inits) == "sd.d")]
  })

  model[['code']] <- mtc.model.code(model, c(), '', template='gemtc.model.use.template.txt')

  monitors <- inits.to.monitors(model[['inits']][[1]])
  model[['monitors']] <- list(
    available=monitors,
    enabled=monitors[grep('^delta\\[', monitors)]
  )

    class(model) <- "mtc.model"

  model
}

mtc.model.name.use <- function(model) {
  "unrelated study effects"
}
