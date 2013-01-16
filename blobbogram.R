data <- read.table(textConnection('
id         group pe   ci.l ci.u style    value.A  value.B 
"Study 1"  1     0.35 0.08 0.92 "normal" "2/46"   "7/46" 
"Study 2"  1     0.43 0.15 1.14 "normal" "4/50"   "8/49" 
"Study 3"  2     0.31 0.07 0.74 "normal" "2/97"   "10/100"
"Study 4"  2     0.86 0.34 2.90 "normal" "9/104"  "6/105" 
"Study 5"  2     0.33 0.10 0.72 "normal" "4/74"   "14/74" 
"Study 6"  2     0.47 0.23 0.91 "normal" "11/120" "22/129"
"Pooled"   NA    0.42 0.15 1.04 "pooled" NA       NA 
'), header=TRUE)
data$pe <- log(data$pe)
data$ci.l <- log(data$ci.l)
data$ci.u <- log(data$ci.u)

library(grid)

blobbogram <- function(data, id.label='Study', ci.label="Mean (95% CI)",
	left.label=NULL, right.label=NULL,
	log.scale=FALSE, xlim=NULL, styles=NULL,
	grouped=('group' %in% colnames(data)), group.labels=NULL,
	columns=NULL, column.labels=NULL,
	column.groups, column.group.labels=NULL) {

	# Add default ('id') column
	columns <- c('id', columns)
	column.labels <- c(id.label, column.labels)

	# Scale transform and its inverse
	scale.trf <- if (log.scale) exp else identity
	scale.inv <- if (log.scale) log else identity

	# Create labels
	labeltext <- rbind(column.labels, as.matrix(data[,columns]))
	rownames(labeltext) <- NULL
	labels <- apply(labeltext, c(1,2), function(x) { if (!is.na(x)) textGrob(x) })
	if (grouped) {
		group.labels <- lapply(column.group.labels, function(x) { if (!is.na(x)) textGrob(x) })
	}

	# Create CI labels
	ci.labels <- lapply(1:nrow(data), function(i) {
		text <- paste(scale.trf(data$pe[i]), " (", scale.trf(data$ci.l[i]), ", ", scale.trf(data$ci.u[i]), ")", sep="")
		textGrob(text)
	})

	# Calculate column widths
	colgap <- unit(3, "mm")
	colwidth <- do.call(unit.c, apply(labels, 2, function(col) {
		col <- col[!sapply(col, is.null)]
		unit.c(max(unit(rep(1, length(col)), "grobwidth", col)), colgap)
	}))

	# Adjust column widths so group labels fit
	if (grouped) {
		groups <- names(column.group.labels)
		if (is.null(groups)) {
			groups <- 1:length(column.group.labels)
		}
		for (group in groups) {
			gl <- group.labels[group]
			select <- column.groups == group
			for (i in which(select)) {
				colwidth[[2 * i + 1]] = max(unit(1.0 / sum(select), "grobwidth", gl), colwidth[2 * i + 1])
			}
		}
	}

	graphwidth <- unit(5, "cm")
	ci.colwidth <- max(unit(rep(1, length(ci.labels)), "grobwidth", ci.labels))
	colwidth <- unit.c(colwidth, graphwidth, colgap, ci.colwidth)

	nc <- ncol(labels)
	nr <- nrow(labels)

	# Initialize plot and layout
	plot.new()
	row.offset <- if (grouped) 2 else 1
	layout <- grid.layout(nr + row.offset + 1, nc * 2 + 3, widths=colwidth, heights=unit(c(rep(1, nr + row.offset - 1), 0.5, 1), "lines"))
	pushViewport(viewport(layout=layout))

	# Draw labels (left-hand side)
	if (grouped) {
		for (group in groups) {
			label <- textGrob(column.group.labels[group])
			pushViewport(viewport(layout.pos.row=1, layout.pos.col=which(column.groups == group) * 2 + 1))
			grid.draw(label)
			popViewport()
		}
	}
	for(row in 1:nr){
		for(col in 1:nc){
			if (!is.null(labels[row, col][[1]])){
				pushViewport(viewport(layout.pos.row=row.offset + row - 1, layout.pos.col=2 * col - 1))
				grid.draw(labels[row, col][[1]])
				popViewport()
			}
		}
	}

	# CI column label
	pushViewport(viewport(layout.pos.row=row.offset, layout.pos.col=2 * nc + 1))
	grid.draw(textGrob(ci.label))
	popViewport()

	# Round to a single significant digit, according to round.fun
	nice <- function(x, round.fun) {
		x <- scale.trf(x)
		p <- 10^floor(log10(abs(x)))
		scale.inv(round.fun(x / p) * p)
	}

	# Calculate plot range
	xrange <- if (is.null(xlim)) {
		c(nice(min(data$ci.l,na.rm=TRUE), floor), nice(max(data$ci.u,na.rm=TRUE), ceiling))
	} else {
		xlim
	}

	# Plot CIs
	for (i in 1:nrow(data)) {
		pushViewport(viewport(layout.pos.row=i + row.offset, layout.pos.col=2*nc+1, xscale=xrange))
		grid.lines(x=unit(c(data$ci.l[i], data$ci.u[i]), "native"), y=0.5) #, arrow=arrow(ends=ends, length=unit(0.05, "inches")), gp=gpar(col=col$lines))
		grid.rect(x=unit(data$pe[i], "native"), y=0.5, width=unit(0.2, "snpc"), height=unit(0.2, "snpc"), gp=gpar(fill="black",col="black"))
		popViewport()
		pushViewport(viewport(layout.pos.row=i + row.offset, layout.pos.col=2*nc+3, xscale=xrange))
		grid.draw(ci.labels[i][[1]])
		popViewport()
	}

	# No-effect line
	pushViewport(viewport(layout.pos.col=2*nc+1, layout.pos.row=row.offset+(1:nr), xscale=xrange))
	grid.lines(x=unit(c(0, 0), "native"), y=unit(c(0, 1), "npc"))
	popViewport()

	# Axis and ticks
	pushViewport(viewport(layout.pos.col=2*nc+1, layout.pos.row=nr+row.offset, xscale=xrange))
	grid.lines(x=unit(c(0, 1), "npc"), y=unit(1, "npc"))
	grid.lines(x=unit(0, "npc"), y=unit(c(0, 1), "npc"))
	grid.lines(x=unit(1, "npc"), y=unit(c(0, 1), "npc"))
	popViewport()

	# Tick labels
	pushViewport(viewport(layout.pos.col=2*nc+1, layout.pos.row=nr+row.offset+1, xscale=xrange))
	grid.draw(textGrob(scale.trf(0), just="center", x=unit(0, "native")))
	grid.draw(textGrob(scale.trf(xrange[1]), just="center", x=unit(0, "npc")))
	grid.draw(textGrob(scale.trf(xrange[2]), just="center", x=unit(1, "npc")))
	popViewport()
}

blobbogram(data,
	columns=c('value.A', 'value.B'), column.labels=c('r/n', 'r/n'),
	column.groups=c(1, 2), column.group.labels=c('Intervention', 'Control'),
	id.label="Trial", ci.label="Odds Ratio (95% CrI)", log.scale=TRUE)

data[['group']] <- NULL

stop()

blobbogram(data,
	columns=c('value.A', 'value.B'), column.labels=c('r/n', 'r/n'),
	id.label="Trial", ci.label="Odds Ratio (95% CrI)", log.scale=TRUE)
