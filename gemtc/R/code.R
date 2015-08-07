mtc.model.code <- function(model, params, relEffectMatrix, template='gemtc.model.template.txt') {
  template <- read.template(template)

  if (model[['data']][['ns.a']] > 0) {
    arm.code <- read.template('gemtc.armeffect.likelihood.txt')
    template <- template.block.sub(template, 'armeffect', arm.code)
    lik.code <- do.call(paste("mtc.code.likelihood", model[['likelihood']], model[['link']], sep="."), list())
    template <- template.block.sub(template, 'likelihood', lik.code)
  } else {
    template <- template.block.sub(template, 'armeffect', '## OMITTED')
  }

  if (model[['data']][['ns.r2']] > 0) {
    rel.code <- read.template('gemtc.releffect.likelihood.r2.txt')
    template <- template.block.sub(template, 'releffect.r2', rel.code)
  } else {
    template <- template.block.sub(template, 'releffect.r2', '## OMITTED')
  }

  if (model[['data']][['ns.rm']] > 0) {
    rel.code <- read.template('gemtc.releffect.likelihood.rm.txt')
    template <- template.block.sub(template, 'releffect.rm', rel.code)
  } else {
    template <- template.block.sub(template, 'releffect.rm', '## OMITTED')
  }

  mod.code <- if (model[['linearModel']] == "fixed") {
    read.template('gemtc.fixedeffect.txt')
  } else {
    read.template('gemtc.randomeffects.txt')
  }
  template <- template.block.sub(template, 'linearModel', mod.code)

  # substitute in heterogeneity prior
  template <- template.block.sub(template, 'hy.prior', as.character(model[['hy.prior']]))


  template <- template.block.sub(template, 'relativeEffectMatrix', relEffectMatrix)

  if (model[['data']][['ns.a']] > 0) {
    sbPriors <- ll.call('study.baseline.priors', model)
    template <- template.block.sub(template, 'studyBaselinePriors', sbPriors)
  } else {
    template <- template.block.sub(template, 'studyBaselinePriors', '## OMITTED')
  }

  # Generate parameter priors
  priors <- paste(params, "~", "dnorm(0, prior.prec)", collapse="\n")
  template <- template.block.sub(template, 'relativeEffectPriors', priors)

  template
}
