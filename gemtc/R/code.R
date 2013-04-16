mtc.model.code <- function(model, params, relEffectMatrix) {
    fileName <- system.file('gemtc.model.template.txt', package='gemtc')
    template <- readChar(fileName, file.info(fileName)$size)

    lik.code <- do.call(paste("mtc.code.likelihood", model$likelihood, model$link, sep="."), list())
    fileName <- system.file('gemtc.releffect.likelihood.txt', package='gemtc')
    rel.code <- readChar(fileName, file.info(fileName)$size)
    template <- template.block.sub(template, 'likelihood', lik.code)
    template <- template.block.sub(template, 'releffect', rel.code)

    network <- model$network

    template <- template.block.sub(template, 'relativeEffectMatrix', relEffectMatrix)

    # Generate parameter priors
    priors <- paste(params, "~", "dnorm(0, prior.prec)", collapse="\n")
    template <- template.block.sub(template, 'relativeEffectPriors', priors)

    template
}
