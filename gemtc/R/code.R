mtc.model.code <- function(model, params, relEffectMatrix, template='gemtc.model.template.txt') {
    template <- read.template(template)

    arm.code <- read.template('gemtc.armeffect.likelihood.txt')
    template <- template.block.sub(template, 'armeffect', arm.code)
    rel.code <- read.template('gemtc.releffect.likelihood.txt')
    template <- template.block.sub(template, 'releffect', rel.code)

    lik.code <- do.call(paste("mtc.code.likelihood", model$likelihood, model$link, sep="."), list())
    template <- template.block.sub(template, 'likelihood', lik.code)

    network <- model$network

    template <- template.block.sub(template, 'relativeEffectMatrix', relEffectMatrix)

    # Generate parameter priors
    priors <- paste(params, "~", "dnorm(0, prior.prec)", collapse="\n")
    template <- template.block.sub(template, 'relativeEffectPriors', priors)

    template
}
