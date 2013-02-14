context("mtc.model.graph")

test_that("Vertices agree between mtc.comparisons and the model tree", {
    network <- read.mtc.network(system.file("extdata/luades-thrombolytic.gemtc", package='gemtc'))
    model <- mtc.model(network)
    graph <- mtc.network.graph(network)
    expect_that(V(model$tree)$name, equals(V(graph)$name))
    expect_that(V(mtc.model.graph(model))$name, equals(V(graph)$name))
})
