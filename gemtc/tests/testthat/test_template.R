context("template")

test_that("template.block.sub for a single line keeps indent", {
  template <- "model {\n\t$model$\n}\n"
  expect_that(template.block.sub(template, 'model', 'foo'), equals("model {\n\tfoo\n}\n"))
})

test_that("template.block.sub for multi-line keeps indent", {
  template <- "model {\n\t$model$\n}\n"
  expect_that(template.block.sub(template, 'model', 'foo\nbar'), equals("model {\n\tfoo\n\tbar\n}\n"))
})

test_that("template.block.sub keeps other stuff on the same line", {
  template <- "model {\n\tbaz($model$)\n}\n"
  expect_that(template.block.sub(template, 'model', 'foo\nbar'), equals("model {\n\tbaz(foo\n\tbar)\n}\n"))
})

test_that("template.block.sub keeps other stuff with white space on the same line", {
  template <- "model {\n\tbaz baz($model$)\n}\n"
  expect_that(template.block.sub(template, 'model', 'foo\nbar'), equals("model {\n\tbaz baz(foo\n\tbar)\n}\n"))
})
