context("Minimum diameter spanning tree")

# Simple line graph with 5 vertices, 4 edges
test_that("absolute.one.center of a 5-vertex line graph is on the middle vertex", {
	n <- 5
	g <- graph.edgelist(matrix(c(1:(n-1), 2:n), ncol=2))
	expect_that(local.center(g, 1), equals(c('e' = 1, 't' = 1, 'r' = 3)))
	expect_that(local.center(g, 2), equals(c('e' = 2, 't' = 1, 'r' = 2)))
	expect_that(local.center(g, 3), equals(c('e' = 3, 't' = 0, 'r' = 2)))
	expect_that(local.center(g, 4), equals(c('e' = 4, 't' = 0, 'r' = 3)))
	expect_that(absolute.one.center(g), equals(c('e' = 2, 't' = 1, 'r' = 2)))
})

# With edge lengths (weights)
test_that("absolute.one.center takes edge weights into account", {
	n <- 5
	g <- graph.edgelist(matrix(c(1:(n-1), 2:n), ncol=2))
	g <- set.edge.attribute(g, 'weight', value=c(1, 1, 1, 0.5))
	expect_that(local.center(g, 2), equals(c('e' = 2, 't' = 0.75, 'r' = 1.75)))
	expect_that(local.center(g, 3), equals(c('e' = 3, 't' = 0, 'r' = 2)))
	expect_that(absolute.one.center(g), equals(c('e' = 2, 't' = 0.75, 'r' = 1.75)))
})

# Simple line graph with 6 vertices, 5 edges
test_that("absolute.one.center of 6-vertex line graph is on the middle of the middle edge", {
	n <- 6
	g <- graph.edgelist(matrix(c(1:(n-1), 2:n), ncol=2))
	expect_that(local.center(g, 1), equals(c('e' = 1, 't' = 1, 'r' = 4)))
	expect_that(local.center(g, 2), equals(c('e' = 2, 't' = 1, 'r' = 3)))
	expect_that(local.center(g, 3), equals(c('e' = 3, 't' = 0.5, 'r' = 2.5)))
	expect_that(local.center(g, 4), equals(c('e' = 4, 't' = 0, 'r' = 3)))
	expect_that(absolute.one.center(g), equals(c('e' = 3, 't' = 0.5, 'r' = 2.5)))
})

# Slightly less simple graph
test_that("absolute.one.center of a non-line graph is correct", {
	g <- graph.edgelist(rbind(c(1, 2), c(1, 3), c(1, 4), c(4, 5)))
	expect_that(local.center(g, 1), equals(c('e' = 1, 't' = 0, 'r' = 2)))
	expect_that(local.center(g, 2), equals(c('e' = 2, 't' = 0, 'r' = 2)))
	expect_that(local.center(g, 3), equals(c('e' = 3, 't' = 0.5, 'r' = 1.5)))
	expect_that(local.center(g, 4), equals(c('e' = 4, 't' = 0, 'r' = 2)))
	expect_that(absolute.one.center(g), equals(c('e' = 3, 't' = 0.5, 'r' = 1.5)))
})

# Graph with a cycle
test_that("absolute.once.center of a cyclic graph is correct", {
	g <- graph.edgelist(rbind(c(1, 2), c(1, 3), c(1, 4), c(1, 5), c(4, 5)))
	expect_that(local.center(g, 1), equals(c('e' = 1, 't' = 0, 'r' = 1)))
	expect_that(local.center(g, 2), equals(c('e' = 2, 't' = 0, 'r' = 1)))
	expect_that(local.center(g, 3), equals(c('e' = 3, 't' = 0, 'r' = 1)))
	expect_that(local.center(g, 4), equals(c('e' = 4, 't' = 0, 'r' = 1)))
	expect_that(local.center(g, 5), equals(c('e' = 5, 't' = 0, 'r' = 2)))
	expect_that(absolute.one.center(g), equals(c('e' = 1, 't' = 0, 'r' = 1)))
})

# Graph with only one edge
test_that("absolute.once.center of one-edge graph is correct", {
	g <- graph.edgelist(rbind(c(1, 2)))
	expect_that(local.center(g, 1), equals(c('e' = 1, 't' = 0.5, 'r' = 0.5)))
	expect_that(absolute.one.center(g), equals(c('e' = 1, 't' = 0.5, 'r' = 0.5)))
})

if (FALSE) {
# Complex real network (luades-thrombolytic)
v <- c("ASPAC", "AtPA", "Ret", "SK", "SKtPA", "Ten", "tPA", "UK")
e <- rbind(c("ASPAC", "AtPA"), c("ASPAC", "tPA"), c("ASPAC", "SK"), c("AtPA", "Ten"), c("AtPA", "SKtPA"), c("AtPA", "Ret"), c("AtPA", "SK"), c("Ret", "SK"), c("SK", "UK"), c("SK", "SKtPA"), c("SK", "tPA"), c("tPA", "UK"))
g <- graph.edgelist(e)
E(g)$arrow.mode <- 0

plot(g, vertex.label=v) # FIXME: order of vertex labels does not match order of vertices

h <- minimum.diameter.spanning.tree(g)

plot(h, vertex.label=v)
}
