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

add.group <- function(columns, ci.data, layout.row) {
	nc <- length(columns)
	for (row in 1:length(ci.data$labels)) {
		for (col in columns) {
			if (!is.null(ci.data$labels[[row]][[col]])){
				pushViewport(viewport(layout.pos.row=layout.row, layout.pos.col=2 * which(columns == col) - 1))
				grid.draw(ci.data$labels[[row]][[col]])
				popViewport()
			}
		}
		if (!is.null(ci.data$ci.plot[[row]])) {
			pushViewport(viewport(layout.pos.row=layout.row, layout.pos.col=2*nc+1))
			grid.draw(ci.data$ci.plot[[row]])
			popViewport()
			pushViewport(viewport(layout.pos.row=layout.row, layout.pos.col=2*nc+3))
			grid.draw(ci.data$ci.label[[row]])
			popViewport()
		}
		layout.row <- layout.row + 1
	}
	layout.row
}

grob.ci <- function(pe, ci.l, ci.u, xrange, style) {
	grob.pe <- NULL
	if(style$pe.style == "circle") { 
		grob.pe <- circleGrob(x=unit(pe, "native"), y=0.5, r=unit(0.2, "snpc"), gp=gpar(col="black"))
	} else { # Default is square
		grob.pe <- rectGrob(x=unit(pe, "native"), y=0.5, width=unit(0.2, "snpc"), height=unit(0.2, "snpc"), gp=gpar(fill="black",col="black"))
	}
	
	build.arrow <- function(ends) { arrow(ends=ends, length=unit(0.5, "snpc")) }

	arrow <- if (ci.l < xrange[[1]] && ci.u > xrange[[2]]) build.arrow("both") 
					else if(ci.l < xrange[[1]]) build.arrow("first")  
					else if (ci.u > xrange[[2]]) build.arrow("last")
					else NULL
	
	line <- linesGrob(x=unit(c(max(ci.l, xrange[[1]]), min(ci.u, xrange[[2]])), "native"), arrow=arrow, y=0.5) 

	ciGrob <- gTree(children=gList(
		line,
		grob.pe
	))
	ciGrob$vp <- viewport(xscale=xrange)
	ciGrob
}

text.style	<- function(styles) {
	function(text, style) { 
		ff <- if (is.na(styles[style,]$font.weight)) "plain" else styles[style,]$font.weight
		textGrob(text, x=unit(0, "npc"), just="left", gp=gpar(fontface=ff))
	}
}

draw.page <- function(ci.data, colwidth, rowheights, ci.label, grouped, columns, column.groups, column.group.labels, header.labels, text.fn, xrange, scale.trf, scale.inv) {
	columns.grouped <- !is.null(column.groups)
	row.offset <- if (columns.grouped) 2 else 1

	# Initialize plot and layout
	rowheight <- unit.c(unit(rep(1, row.offset), "lines"), rowheights, unit(c(0.5, 1), "lines"))
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
	groupNames <- names(ci.data)
	for (grp in 1:length(ci.data)) {
		if (grouped) {
			layout.row <- add.group.label(if (!is.na(groupNames[[grp]])) text.fn(groupNames[[grp]], 'group'), layout.row)
		}
		layout.row <- add.group(columns, ci.data[[grp]], layout.row)
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
	grouped=TRUE, group.labels=NULL,
	columns=NULL, column.labels=NULL,
	column.groups=NULL, column.group.labels=NULL,
	ask=dev.interactive(orNone=TRUE)) {

	grouped <- !is.null(group.labels) && isTRUE(grouped)

	if (is.null(styles)) {
		styles <- data.frame(
			style=c('normal', 'pooled', 'group'),
			font.weight=c('plain', 'plain', 'bold'),
			row.height=c(1, 1, 1.5),
			pe.style=c('circle', 'square', NA),
			pe.scale=c(FALSE, FALSE, NA))
		rownames(styles) <- styles$style
	}
	text.fn <- text.style(styles)

	# Rewrite input: split into groups
	data <- get.row.groups(data, group.labels)

	columns.grouped <- !is.null(column.groups)

	# Add default ('id') column
	columns <- c('id', columns)
	column.labels <- c(id.label, column.labels)

	# Scale transform and its inverse
	scale.trf <- if (log.scale) exp else identity
	scale.inv <- if (log.scale) log else identity

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
		c(min(nice(min(ci.l,na.rm=TRUE), floor), 0), max(nice(max(ci.u,na.rm=TRUE), ceiling), 0))
	} else {
		xlim
	}

	rowToGrobs <- function(row) {
		lapply(row, function(x) { 
				if (!is.na(x)) text.fn(x, row['style']) 
			})
	}

	header.labels <- rowToGrobs(column.labels)
	forest.data <- lapply(data, function(datagrp) { 
			# Create labels
			labels <- apply(datagrp$data, 1, rowToGrobs)
		
			ci.data <- lapply(1:nrow(datagrp$data), function(i) { 
				# Create CI plots
				fmt <- datagrp$data[i, c('pe', 'ci.l', 'ci.u')]
				ci <- grob.ci(fmt$pe, fmt$ci.l, fmt$ci.u, xrange, styles[datagrp$data[i, 'style'],])
	
				# Create CI interval labels (right side) 
				fmt <- lapply(fmt, function(x) { formatC(scale.trf(x), format='f') })
				text <- paste(fmt$pe, " (", fmt$ci.l, ", ", fmt$ci.u, ")", sep="")
				label <-  text.fn(text, datagrp$data[i,'style'])

				list(ci=ci, label=label)
			})
			ci.plot <- lapply(ci.data, function(x) { x$ci })
			ci.label <- lapply(ci.data, function(x) { x$label })

			list(labels=labels, ci.plot=ci.plot, ci.label=ci.label)
		})
	names(forest.data) <- lapply(data, function(grp) { grp$label })

	if (columns.grouped) {
		group.labels <- rowToGrobs(column.group.labels)
	}

	# Calculate column widths
	colgap <- unit(3, "mm")
	colwidth <- do.call(unit.c, lapply(columns, function(col) {
		col <- c(header.labels[col], do.call(c, lapply(forest.data, function(grp) { sapply(grp$labels, function(row) { row[col] }) })))
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
	all.ci.labels <- do.call(c, lapply(forest.data, function(x) { x$ci.label }) )
	
	ci.colwidth <- max(unit(rep(1, length(all.ci.labels)), "grobwidth", all.ci.labels))
	colwidth <- unit.c(colwidth, graphwidth, colgap, ci.colwidth)

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
		if (i > 1) {
			if (ask) {
				readline('Hit <Return> to see next plot:')
			}
			grid.newpage()
		}
		page <- pages[[i]]
		rowheights <- do.call(unit.c, lapply(data[page], groupHeight))
		draw.page(forest.data[page], colwidth, rowheights, ci.label, grouped, columns, column.groups, column.group.labels, header.labels, text.fn, xrange, scale.trf, scale.inv)
	}
}

if (FALSE) {
	data <- read.table(textConnection('
	id				 group pe		ci.l ci.u style		 value.A	value.B 
	"Study 1"  1		 0.35 0.08 0.92 "normal" "2/46"		"7/46" 
	"Study 2"  1		 0.43 0.15 1.14 "normal" "4/50"		"8/49" 
	"Study 3"  2		 0.31 0.07 0.74 "normal" "2/97"		"10/100"
	"Study 4"  2		 0.86 0.34 2.90 "normal" "9/104"	"6/105" 
	"Study 5"  2		 0.33 0.10 0.72 "normal" "4/74"		"14/74" 
	"Study 6"  2		 0.47 0.23 0.91 "normal" "11/120" "22/129"
	"Pooled"	 NA		 0.42 0.15 1.04 "pooled" NA				NA 
	'), header=TRUE)
	data$pe <- log(data$pe)
	data$ci.l <- log(data$ci.l)
	data$ci.u <- log(data$ci.u)

	data <- read.table(textConnection('
	id				 group pe		ci.l ci.u style		 value.A	value.B 
	"Study 1"  1		 20 -10 50 "normal" "2/46"		"7/46" 
	"Study 3"  2		 30 15 70 "normal" "11/120" "22/129"
	"Study 3"  2		 30 -15 70 "normal" "11/120" "22/129"
	"Study 3"  2		 15 10 20 "normal" "11/120" "22/129"
	'), header=TRUE)
	
	blobbogram(data, group.labels=c('GROEP 1', 'GROEP 2'),
		columns=c('value.A', 'value.B'), column.labels=c('r/n', 'r/n'),
		column.groups=c(1, 2), grouped=FALSE, xlim=c(0, 50), column.group.labels=c('Intervention', 'Control'),
		id.label="Trial", ci.label="Odds Ratio (95% CrI)", log.scale=FALSE)

}
