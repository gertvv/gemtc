library(igraph)

## Test cases for minimum diameter spanning tree

source('minimum.diameter.spanning.tree.R')

# Simple line graph with 5 vertices, 4 edges
n <- 5
g <- graph.edgelist(matrix(c(1:(n-1), 2:n), ncol=2))
stopifnot(local.center(g, 1) == c('e' = 1, 't' = 1, 'r' = 3))
stopifnot(local.center(g, 2) == c('e' = 2, 't' = 1, 'r' = 2))
stopifnot(local.center(g, 3) == c('e' = 3, 't' = 0, 'r' = 2))
stopifnot(local.center(g, 4) == c('e' = 4, 't' = 0, 'r' = 3))
stopifnot(absolute.one.center(g) == c('e' = 2, 't' = 1, 'r' = 2))

# With edge lengths (weights)
g <- set.edge.attribute(g, 'weight', value=c(1, 1, 1, 0.5))
stopifnot(local.center(g, 2) == c('e' = 2, 't' = 0.75, 'r' = 1.75))
stopifnot(local.center(g, 3) == c('e' = 3, 't' = 0, 'r' = 2))
stopifnot(absolute.one.center(g) == c('e' = 2, 't' = 1, 'r' = 2))

# Simple line graph with 6 vertices, 5 edges
n <- 6
g <- graph.edgelist(matrix(c(1:(n-1), 2:n), ncol=2))
stopifnot(local.center(g, 1) == c('e' = 1, 't' = 1, 'r' = 4))
stopifnot(local.center(g, 2) == c('e' = 2, 't' = 1, 'r' = 3))
stopifnot(local.center(g, 3) == c('e' = 3, 't' = 0.5, 'r' = 2.5))
stopifnot(local.center(g, 4) == c('e' = 4, 't' = 0, 'r' = 3))


# Slightly less simple graph
g <- graph.edgelist(rbind(c(1, 2), c(1, 3), c(1, 4), c(4, 5)))
stopifnot(local.center(g, 1) == c('e' = 1, 't' = 0, 'r' = 2))
stopifnot(local.center(g, 2) == c('e' = 2, 't' = 0, 'r' = 2))
stopifnot(local.center(g, 3) == c('e' = 3, 't' = 0.5, 'r' = 1.5))
stopifnot(local.center(g, 4) == c('e' = 4, 't' = 0, 'r' = 2))

# Graph with a cycle
g <- graph.edgelist(rbind(c(1, 2), c(1, 3), c(1, 4), c(1, 5), c(4, 5)))
stopifnot(local.center(g, 1) == c('e' = 1, 't' = 0, 'r' = 1))
stopifnot(local.center(g, 2) == c('e' = 2, 't' = 0, 'r' = 1))
stopifnot(local.center(g, 3) == c('e' = 3, 't' = 0, 'r' = 1))
stopifnot(local.center(g, 4) == c('e' = 4, 't' = 0, 'r' = 1))
stopifnot(local.center(g, 5) == c('e' = 5, 't' = 0, 'r' = 2))



# Complex real network (luades-thrombolytic)
v <- c("ASPAC", "AtPA", "Ret", "SK", "SKtPA", "Ten", "tPA", "UK")
e <- rbind(c("ASPAC", "AtPA"), c("ASPAC", "tPA"), c("ASPAC", "SK"), c("AtPA", "Ten"), c("AtPA", "SKtPA"), c("AtPA", "Ret"), c("AtPA", "SK"), c("Ret", "SK"), c("SK", "UK"), c("SK", "SKtPA"), c("SK", "tPA"), c("tPA", "UK"))
g <- graph.edgelist(e)
E(g)$arrow.mode <- 0

plot(g, vertex.label=v) # FIXME: order of vertex labels does not match order of vertices

h <- minimum.diameter.spanning.tree(g)

plot(h, vertex.label=v)
