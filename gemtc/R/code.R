mtc.model.code <- function(model, params, relEffectMatrix) {
    fileName <- system.file('gemtc.model.template.txt', package='gemtc')
    template <- readChar(fileName, file.info(fileName)$size)

    lik.code <- do.call(paste("mtc.code.likelihood", model$likelihood, model$link, sep="."), list())
    fileName <- system.file('gemtc.releffect.likelihood.txt', package='gemtc')
    rel.code <- readChar(fileName, file.info(fileName)$size)
    template <- sub('$likelihood$', lik.code, template, fixed=TRUE)
    template <- sub('$releffect$', rel.code, template, fixed=TRUE)

    network <- model$network

    template <- sub('$relativeEffectMatrix$', relEffectMatrix, template, fixed=TRUE)

    # Generate parameter priors
    priors <- paste(params, "~", "dnorm(0, prior.prec)", collapse="\n")
    template <- sub('$relativeEffectPriors$', priors, template, fixed=TRUE)

    template
}
