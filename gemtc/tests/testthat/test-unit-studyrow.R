context("mtc.data.studyrow")

studyrow_file <- function(name) {
  paste0("../data/studyrow/", name)
}

test_that("TSD2 example 1 (dichotomous data, binomial/logit)", {
  treatments <- c('Control', 'BetaB')
  data <- mtc.data.studyrow(read.table(studyrow_file('tsd2-1.data.txt'), header=TRUE), treatmentNames=treatments)
  expect_that(data, equals(dget(studyrow_file('tsd2-1.out.txt'))))
})

test_that("TSD2 example 2 (count data, poisson/log)", {
  treatments <- c('control', 'diet')
  studies <- c('DART', 'London Corn/Olive', 'London Low Fat', 'Minnesota Coronary', 'MRC Soya', 'Oslo Diet-Heart', 'STARS', 'Sydney Diet-Heart', 'Veterans Administration', 'Veterans Diet & Skin CA')
  data <- mtc.data.studyrow(
    read.table(studyrow_file('tsd2-2.data.txt'), header=TRUE),
    armVars=c('treatment'='t', 'responders'='r', 'exposure'='E'),
    treatmentNames=treatments, studyNames=studies)

  # Needs check.attributes=FALSE because locales may sort upper/lower case differently
  # This leads to differences in levels(data$studies)
  expect_that(data, equals(dget(studyrow_file('tsd2-2.out.txt')), check.attributes=FALSE))
})

test_that("TSD2 example 3 (count data, binomial/cloglog)", {
  studies <- c('MRC-E', 'EWPH', 'SHEP', 'HAPPHY', 'ALLHAT', 'INSIGHT', 'ANBP-2', 'ALPINE', 'FEVER', 'DREAM', 'HOPE', 'PEACE', 'CHARM', 'SCOPE', 'AASK', 'STOP-2', 'ASCOT', 'NORDIL', 'INVEST', 'CAPPP', 'LIFE', 'VALUE')
  treatments <- c('Diuretic', 'Placebo', 'BetaB', 'CCB', 'ACEi', 'ARB')
  data <- mtc.data.studyrow(
    read.table(studyrow_file('tsd2-3.data.txt'), header=TRUE),
    studyVars=c('time'='time'),
    studyNames=studies, treatmentNames=treatments)
  expect_that(data, equals(dget(studyrow_file('tsd2-3.out.txt'))))
})

## TSD2 example 4 (competing risks, multinomial/log) not supported

test_that("TSD2 example 5 (continuous data, normal/identity)", {
  data <- mtc.data.studyrow(
    read.table(studyrow_file('tsd2-5.data.txt'), header=TRUE),
    armVars=c('treatment'='t', 'mean'='y', 'std.err'='se'))
  expect_that(data, equals(dget(studyrow_file('tsd2-5.out.txt'))))
})

## TSD2 example 6 (categorical data, binomial/probit) not supported

test_that("TSD2 example 7 (relative effect data)", {
  data <- mtc.data.studyrow(
    read.table(studyrow_file('tsd2-7.data.txt'), header=TRUE),
    armVars=c('treatment'='t', 'diff'='y', 'std.err'='se'),
    studyVars=c('var'='V'))
  expect_that(data, equals(dget(studyrow_file('tsd2-7.out.txt'))))
})

test_that("TSD2 example 8 (mixed data)", {
  data1 <- mtc.data.studyrow(
    read.table(studyrow_file('tsd2-8.data1.txt'), header=TRUE),
    armVars=c('treatment'='t.a', 'mean'='y.a', 'std.err'='se.a'),
    nArmsVar='na.a')
  expect_that(data1, equals(dget(studyrow_file('tsd2-8.out1.txt'))))

  data2 <- mtc.data.studyrow(
    read.table(studyrow_file('tsd2-8.data2.txt'), header=TRUE),
    armVars=c('treatment'='t', 'diff'='y', 'std.err'='se'),
    nArmsVar='na',
    studyNames=4:7)
  expect_that(data2, equals(dget(studyrow_file('tsd2-8.out2.txt'))))
})
