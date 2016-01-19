#' @include template.R

mtc.model.code <- function(model, params, relEffectMatrix, template='gemtc.model.template.txt',
                           linearModel='delta[i, k]', regressionPriors='') {
  powerAdjust <- !is.null(model[['powerAdjust']]) && !is.na(model[['powerAdjust']])

  template <- read.template(template)

  if (length(model[['data']][['studies.a']]) > 0) {
    arm.code <- read.template('gemtc.armeffect.likelihood.txt')
    template <- template.block.sub(template, 'armeffect', arm.code)
    lik.code <- do.call(paste("mtc.code.likelihood", model[['likelihood']], model[['link']], sep="."), list(powerAdjust=powerAdjust))
    template <- template.block.sub(template, 'likelihood', lik.code)
  } else {
    template <- template.block.sub(template, 'armeffect', '## OMITTED')
  }

  if (length(model[['data']][['studies.r2']]) > 0) {
    rel.code <-
      if (powerAdjust) read.template('gemtc.releffect.likelihood.power.r2.txt')
      else read.template('gemtc.releffect.likelihood.r2.txt')
    template <- template.block.sub(template, 'releffect.r2', rel.code)
  } else {
    template <- template.block.sub(template, 'releffect.r2', '## OMITTED')
  }

  if (length(model[['data']][['studies.rm']]) > 0) {
    rel.code <-
      if (powerAdjust) read.template('gemtc.releffect.likelihood.power.rm.txt')
      else read.template('gemtc.releffect.likelihood.rm.txt')
    template <- template.block.sub(template, 'releffect.rm', rel.code)
  } else {
    template <- template.block.sub(template, 'releffect.rm', '## OMITTED')
  }

  template <- template.block.sub(template, 'armLinearModel', paste0('mu[i] + ', linearModel))
  template <- template.block.sub(template, 'relLinearModel', linearModel)

  hyModel <- if (model[['linearModel']] == "fixed") {
    read.template('gemtc.fixedeffect.txt')
  } else {
    read.template('gemtc.randomeffects.txt')
  }
  template <- template.block.sub(template, 'heterogeneityModel', hyModel)

  # substitute in heterogeneity prior
  template <- template.block.sub(template, 'hy.prior', as.character(model[['hy.prior']]))

  template <- template.block.sub(template, 'relativeEffectMatrix', relEffectMatrix)

  if (length(model[['data']][['studies.a']]) > 0) {
    sbPriors <- ll.call('study.baseline.priors', model)
    template <- template.block.sub(template, 'studyBaselinePriors', sbPriors)
  } else {
    template <- template.block.sub(template, 'studyBaselinePriors', '## OMITTED')
  }

  # Generate parameter priors
  priors <- paste(params, "~", "dnorm(0, prior.prec)", collapse="\n")
  template <- template.block.sub(template, 'relativeEffectPriors', priors)

  template <- template.block.sub(template, 'regressionPriors', regressionPriors)

  template
}
