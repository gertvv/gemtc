library(grid)

get.row.groups <- function(data, group.labels) {
	row.old <- 1
	if (is.factor(data$group)) {
		data$group <- as.character(data$group)
	}
	groups <- rle(data$group)
	lapply(1:length(groups$lengths), function(i) {
		v <- groups$values[i]
		l <- groups$lengths[i]
		i0 <- if (i == 1) 1 else sum(groups$lengths[1:max(1, (i - 1))]) + 1
		list(label=group.labels[v], data=data[i0:(i0 + l - 1),])
	})
}

add.group.label <- function(label, layout.row) {
	pushViewport(viewport(layout.pos.row=layout.row, layout.pos.col=1))
	if (!is.null(label)) {
		grid.draw(label)
	}
	popViewport()
	layout.row + 1
}

add.group <- function(columns, data, labels, ci.labels, layout.row, xrange) {
	nc <- length(columns)
	for (row in 1:nrow(data)) {
		for (col in columns) {
			if (!is.null(labels[[row]][[col]])){
				pushViewport(viewport(layout.pos.row=layout.row, layout.pos.col=2 * which(columns == col) - 1))
				grid.draw(labels[[row]][[col]])
				popViewport()
			}
		}

		if (!is.na(data$pe[row])) {
			ciGrob <- gTree(children=gList(
				linesGrob(x=unit(c(data$ci.l[row], data$ci.u[row]), "native"), y=0.5), #, arrow=arrow(ends=ends, length=unit(0.05, "inches")), gp=gpar(col=col$lines))
				rectGrob(x=unit(data$pe[row], "native"), y=0.5, width=unit(0.2, "snpc"), height=unit(0.2, "snpc"), gp=gpar(fill="black",col="black"))
				
			))
			ciGrob$vp <- viewport(xscale=xrange)
			pushViewport(viewport(layout.pos.row=layout.row, layout.pos.col=2*nc+1))
			grid.draw(ciGrob)
			popViewport()
			pushViewport(viewport(layout.pos.row=layout.row, layout.pos.col=2*nc+3))
			grid.draw(ci.labels[[row]])
			popViewport()
		}

		layout.row <- layout.row + 1
	}
	layout.row
}

draw.page <- function(data, colwidth, data.labels, ci.label, ci.labels, grouped, groupHeight, columns, column.groups, column.group.labels, header.labels, xrange, scale.trf, scale.inv) {
	columns.grouped <- !is.null(column.groups)
	row.offset <- if (columns.grouped) 2 else 1

	# Initialize plot and layout
	dataheight <- do.call(unit.c, lapply(data, groupHeight))
	rowheight <- unit.c(unit(rep(1, row.offset), "lines"), dataheight, unit(c(0.5, 1), "lines"))
	layout <- grid.layout(length(rowheight), length(colwidth), widths=colwidth, heights=rowheight)
	pushViewport(viewport(layout=layout))

	# Draw column labels (left-hand side)
	if (columns.grouped) {
		groups <- names(column.group.labels)
		for (group in groups) {
			label <- textGrob(column.group.labels[group])
			pushViewport(viewport(layout.pos.row=1, layout.pos.col=which(column.groups == group) * 2 + 1))
			grid.draw(label)
			popViewport()
		}
	}
	for (i in 1:length(header.labels)) {
		pushViewport(viewport(layout.pos.row=row.offset, layout.pos.col=2 * i - 1))
		grid.draw(header.labels[[i]])
		popViewport()
	}

	# CI column label
	pushViewport(viewport(layout.pos.row=row.offset, layout.pos.col=length(colwidth)))
	grid.draw(textGrob(ci.label))
	popViewport()

	# Main content
	nc <- length(columns)
	layout.row <- row.offset + 1
	for (grp in 1:length(data)) {
		if (grouped) {
			layout.row <- add.group.label(if (!is.na(data[[grp]]$label)) textGrob(data[[grp]]$label), layout.row)
		}
		layout.row <- add.group(columns, data[[grp]]$data, data.labels[[grp]], ci.labels[[grp]], layout.row, xrange)
	}
	nr <- layout.row

	# No-effect line
	pushViewport(viewport(layout.pos.col=2*nc+1, layout.pos.row=(row.offset+1):(nr), xscale=xrange))
	grid.lines(x=unit(c(0, 0), "native"), y=unit(c(0, 1), "npc"))
	popViewport()

	# Axis and ticks
	pushViewport(viewport(layout.pos.col=2*nc+1, layout.pos.row=nr, xscale=xrange))
	grid.lines(x=unit(c(0, 1), "npc"), y=unit(1, "npc"))
	grid.lines(x=unit(0, "npc"), y=unit(c(0, 1), "npc"))
	grid.lines(x=unit(1, "npc"), y=unit(c(0, 1), "npc"))
	popViewport()

	# Tick labels
	pushViewport(viewport(layout.pos.col=2*nc+1, layout.pos.row=nr+1, xscale=xrange))
	grid.draw(textGrob(scale.trf(0), just="center", x=unit(0, "native")))
	grid.draw(textGrob(scale.trf(xrange[1]), just="center", x=unit(0, "npc")))
	grid.draw(textGrob(scale.trf(xrange[2]), just="center", x=unit(1, "npc")))
	popViewport()
}

blobbogram <- function(data, id.label='Study', ci.label="Mean (95% CI)",
	left.label=NULL, right.label=NULL,
	log.scale=FALSE, xlim=NULL, styles=NULL,
	grouped=NULL, group.labels=NULL,
	columns=NULL, column.labels=NULL,
	column.groups=NULL, column.group.labels=NULL) {

	grouped <- ('group' %in% colnames(data))

	if (is.null(styles)) {
		styles <- data.frame(
			style=c('normal', 'pooled', 'group'),
			font.weight=c('plain', 'plain', 'bold'),
			row.height=c(1, 1, 1.5),
			pe.style=c('circle', 'square', NA),
			pe.scale=c(FALSE, FALSE, NA))
		rownames(styles) <- styles$style
	}

	# Rewrite input: split into groups
	data <- get.row.groups(data, group.labels)

	columns.grouped <- !is.null(column.groups)

	# Add default ('id') column
	columns <- c('id', columns)
	column.labels <- c(id.label, column.labels)

	# Scale transform and its inverse
	scale.trf <- if (log.scale) exp else identity
	scale.inv <- if (log.scale) log else identity

	# FIXME: alignment, etc.
	rowToGrobs <- function(row) {
		lapply(row, function(x) { if (!is.na(x)) textGrob(x, x=unit(0, "npc"), just="left") })
	}

	# Create labels
	header.labels <- rowToGrobs(column.labels)
	data.labels <- lapply(data, function(datagrp) { apply(datagrp$data, 1, rowToGrobs) })
	if (columns.grouped) {
		group.labels <- rowToGrobs(column.group.labels)
	}

	# Create CI labels
	ci.labels <- lapply(data, function(datagrp) {
		lapply(1:nrow(datagrp$data), function(i) {
			fmt <- lapply(datagrp$data[i, c('pe', 'ci.l', 'ci.u')], function(x) { formatC(scale.trf(x), format='f') })
			text <- paste(fmt$pe, " (", fmt$ci.l, ", ", fmt$ci.u, ")", sep="")
			textGrob(text)
		})
	})

	# Calculate column widths
	colgap <- unit(3, "mm")
	colwidth <- do.call(unit.c, lapply(columns, function(col) {
		col <- c(header.labels[col], do.call(c, sapply(data.labels, function(grp) { sapply(grp, function(row) { row[col] }) })))
		col <- col[!sapply(col, is.null)]
		unit.c(max(unit(rep(1, length(col)), "grobwidth", col)), colgap)
	}))

	# Adjust column widths so group labels fit
	if (columns.grouped) {
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
	all.ci.labels <- do.call(c, ci.labels)
	ci.colwidth <- max(unit(rep(1, length(all.ci.labels)), "grobwidth", all.ci.labels))
	colwidth <- unit.c(colwidth, graphwidth, colgap, ci.colwidth)

	# Round to a single significant digit, according to round.fun
	nice <- function(x, round.fun) {
		x <- scale.trf(x)
		p <- 10^floor(log10(abs(x)))
		scale.inv(round.fun(x / p) * p)
	}

	# Calculate plot range
	xrange <- if (is.null(xlim)) {
		ci.l <- do.call(c, lapply(data, function(datagrp) { datagrp$data[, 'ci.l']}))
		ci.u <- do.call(c, lapply(data, function(datagrp) { datagrp$data[, 'ci.u']}))
		c(nice(min(ci.l,na.rm=TRUE), floor), nice(max(ci.u,na.rm=TRUE), ceiling))
	} else {
		xlim
	}

	groupHeight <- function(grp) {
		if (grouped) unit(c(styles['group', 'row.height'], styles[grp$data$style, 'row.height']), "lines")
		else unit(styles[grp$data$style, 'row.height'], "lines")
	}

	# divide data into pages
	height <- sum(unit.c(unit(rep(1, if (columns.grouped) 2 else 1), "lines"), unit(c(0.5, 1), "lines")))
	space <- 1.0 - convertY(height, "npc", valueOnly=TRUE)

	pages <- list(c())
	height <- 0
	for (i in 1:length(data)) {
		myHeight <- convertY(sum(groupHeight(data[[i]])), "npc", valueOnly=TRUE)
		if (myHeight > space) {
			stop("PAGE WONT FIT")
		}
		if (height + myHeight > space) { # new page
			height <- myHeight
			pages <- c(pages, c(i))
		} else { # append to this page
			height <- height + myHeight
			pages[[length(pages)]] <- c(pages[[length(pages)]], i)
		}
	}

	# Now plot each group
	for (i in 1:length(pages)) {
		if (i > 1) grid.newpage()
		page <- pages[[i]]
		draw.page(data[page], colwidth, data.labels[page], ci.label, ci.labels[page], grouped, groupHeight, columns, column.groups, column.group.labels, header.labels, xrange, scale.trf, scale.inv)
	}
}

if (TRUE) {
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

	blobbogram(data, group.labels=c('GROEP 1', 'GROEP 2'),
		columns=c('value.A', 'value.B'), column.labels=c('r/n', 'r/n'),
		column.groups=c(1, 2), column.group.labels=c('Intervention', 'Control'),
		id.label="Trial", ci.label="Odds Ratio (95% CrI)", log.scale=TRUE)

#	grid.newpage()
#	blobbogram(data,
#		columns=c('value.A', 'value.B'), column.labels=c('r/n', 'r/n'),
#		id.label="Trial", ci.label="Odds Ratio (95% CrI)", log.scale=TRUE,
#		grouped=FALSE)
}
