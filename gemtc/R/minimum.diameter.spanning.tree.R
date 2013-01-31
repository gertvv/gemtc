edge.length <- function(g, e) {
	w <- get.edge.attribute(g, 'weight', e)
	if (is.null(w)) 1 else w
}

minimum.diameter.spanning.tree <- function(graph) {
	l <- function(edge) { edge.length(graph, edge) }

	f <- absolute.one.center(graph)
	dc <- f['t'] - 0.5 * l(f['e'])
	v <- get.edge(graph, f['e'])
	if (dc < 0 || (dc == 0 && v[1] < v[2])) {
		edgelist <- v
	} else {
		edgelist <- rev(v)
	}

	d <- shortest.paths(graph, mode="all")
	t1 <- f['t']
	t2 <- l(f['e']) - t1
	v.rem <- V(g)[!(1:length(V(g)) %in% v)]
	pairs <- sapply(v.rem, function(u) {
		d <- c(t1 + d[v[1], u], t2 + d[v[2], u])
		if (d[1] < d[2] || (d[1] == d[2] && v[1] < v[2])) {
			c(v[1], u)
		} else {
			c(v[2], u)
		}
	})

	h <- graph.empty()
	h <- h + vertex(V(g))
	for (p in get.shortest.paths(graph, pairs[1, ], pairs[2, ], mode="all")) {
		if (length(p) == 2) {
			h <- h + edge(p) # bug in one-edge paths?
		} else {
			h <- h + path(p)
		}
	}
	simplify(h)
}

# Find the absolute 1-center of an undirected graph.
# 
# Let e = (u,v) be an edge of the graph, and l(e) the length of e.
# Let x(e) be a point along the edge, with t = t(x(e)) \in [0, l(e)] the distance of x(e) from u.
# Then, for any point x on G, d(v, x) is the length of the shortest path in G between the vertex v and point x.
# Define F(x) = \max_{v \in V} d(v, x), and x* = \arg\min_{x on G} F(x).
# x* is the absolute 1-center of G and F(x*) is the absolute 1-radius of G.
#
# Algorithm from <a href="http://www.jstor.org/pss/2100910">Kariv and Hakimi (1979), SIAM J Appl Math 37 (3): 513-538</a>.
# 
# (Note: in the paper, the weight of an edge is referred to as its length l(e), and vertices can also have weights w(v).
# Only the vertex-unweighted algorithm is implemented. Edge-weighted graphs are supported, however.)
absolute.one.center <- function(g) {
	f <- sapply(E(g), function(edge) { local.center(g, edge) })
	v <- sapply(E(g), function(edge) { get.edge(g, edge) })
	i <- order(f['r',], v[1,], v[2,])[1]
	f[, i]
}

# A few conventions:
#  - a point along an edge is represented by a pair x = (e, t) where e is the edge index and t is the
#    distance from the left vertex
#  - a center is a triple f = (e, t, r) where (e, t) is a point and r is the graph's radius around that
#    point
local.center <- function(graph, edge, second.order) {
	d <- shortest.paths(graph)

	# L(v): vertices ordered by distance from v
	L <- t(apply(d, 2, function(x) { order(-x, V(g)) }))

	# l(e): length of edge e
	l <- function(edge) { edge.length(graph, edge) }

	# left and right vertices of an edge
	vr <- function(edge) { get.edge(graph, edge)[1] }
	vs <- function(edge) { get.edge(graph, edge)[2] }

	# de(x, v): distance between point x and vertex v
	de <- function(x, v) {
		min(
			x['t'] + d[vr(x['e']), v],
			l(x['e']) - x['t'] + d[vs(x['e']), v]
		)
	}

	# Given an edge e = (e0, e1) and vertices u and v, find the distance t* \in
	# (0, l(e)) from e0 where D_e(u, t*) = D_e(v, t*), and D_e(u, t*) and
	# D_e(v, t*) have opposite signs (if it exists).
	intersect <- function(e, u, v) {
		e0 <- vr(e)
		e1 <- vs(e)
		lu <- d[e0, u]
		ru <- d[e1, u]
		lv <- d[e0, v]
		rv <- d[e1, v]

		if (lu == lv || ru == rv) { # they coincide or intersect only at the edge
			NA
		} else if (lu > lv && ru > rv) { # u dominates v
			NA
		} else if (lu < lv && ru < rv) { # v dominates u
			NA
		} else {
			t1 <- 0.5 * (rv - lu + l(e));
			t2 <- 0.5 * (ru - lv + l(e));
			if (t1 + lu <= l(e) - t1 + ru) {
				t1
			} else {
				t2
			}
		}
	}

	# Step 1: treatment of t = 0 and t = l(e)
	step1 <- function() {
		xr <- c('e' = edge, 't' = 0)
		xs <- c('e' = edge, 't' = l(edge))
		dr <- de(xr, L[vr(edge), 1])
		ds <- de(xs, L[vs(edge), 1])
		f <- if (dr <= ds) c(xr, 'r' = dr) else c(xs, 'r' = ds)

		if (L[vr(edge), 1] == L[vs(edge), 1]) f else step3(f, L[vr(edge), 1], 1)
	}

	# Step 3: treatment of vertices v s.t. D_e(v, 0) = D_e(v_1, 0)
	# f: The suspected center
	# vm: Vertex to consider 
	# i: Index of the last-treated vertex
	# Returns the local center
	step3 <- function(f, vm, i) {
		v <- L[vr(f['e']), i + 1]
		xr <- c(f['e'], 't' = 0)
		xs <- c(f['e'], 't' = l(f['e']))
		if (de(xs, v) != de(xs, vm)) {
			step4(f, vm, i + 1)
		} else if (de(xr, v) > de(xs, vm)) {
			step3(f, v, i + 1) # v_m <- v*
		} else {
			step3(f, vm, i + 1)
		}
	}

	step4 <- function(f, vm, i) {
		vbar <- vm
		if (i == nrow(d)) {
			step8(f, vbar)
		} else {
			vm <- L[vr(f['e']), i]
			step5(f, vm, vbar, i)
		}
	}

	# Step 5: find all vertices v s.t. D_e(v, 0) = D_e(v_i, 0) and find the corresponding v_m.
	step5 <- function(f, vm, vbar, i) {
		v <- L[vr(f['e']), i + 1]
		xr <- c(f['e'], 't' = 0)
		xs <- c(f['e'], 't' = l(f['e']))
		if (de(xr, v) != de(xr, vm)) {
			step6(f, vm, vbar, i + 1)
		} else if (de(xs, v) > de(xs, vm)) {
			step5(f, v, vbar, i + 1) # v_m <- v*
		} else {
			step5(f, vm, vbar, i + 1)
		}
	}

	# Step 6: treatment of the point (e, t_m).
	step6 <- function(f, vm, vbar, i) {
		tm <- intersect(f['e'], vm, vbar)

		if (is.na(tm)) {
			step7(f, vm, vbar, i)
		} else {
			xt <- c(f['e'], 't' = tm)
			ft <- c(xt, 'r' = de(xt, vm))
			if (ft['r'] < f['r']) {
				step7(ft, vm, vbar, i)
			} else {
				step7(f, vm, vbar, i)
			}
		}
	}

	# Step 7: proceed to the next vertex.
	step7 <- function(f, vm, vbar, i) {
		xs <- c(f['e'], 't' = l(f['e']))
		if (de(xs, vm) > de(xs, vbar)) {
			vbar <- vm
		}

		if (i == nrow(d)) {
			step8(f, vbar)
		} else {
			step5(f, L[vr(f['e']), i], vbar, i)
		}
	}

	step8 <- function(f, vbar) {
		vn <- L[vr(f['e']), nrow(d)]
		tm <- intersect(f['e'], vn, vbar)

		if (is.na(tm)) {
			f
		} else {
			xt <- c(f['e'], tm)
			ct <- c(xt, 'r' = de(xt, vn))
			if (ct['r'] < c['r']) {
				ct
			} else {
				c
			}
		}
	}

	step1()
}
