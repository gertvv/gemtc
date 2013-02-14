context("mtc.model.graph")

test_that("Vertices agree between mtc.comparisons and the model tree", {
    network <- read.mtc.network(system.file("extdata/luades-thrombolytic.gemtc", package='gemtc'))
    tree <- mtc.model(network)$tree
    graph <- mtc.network.graph(network)
    expect_that(V(tree)$name, equals(V(graph)$name))
    expect_that(V(mtc.model.graph(model))$name, equals(V(graph)$name))
})
