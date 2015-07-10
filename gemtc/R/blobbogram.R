get.row.groups <- function(data, group.labels) {
  if (is.factor(data[['group']])) {
    data[['group']] <- as.character(data[['group']])
  }
  groups <- rle(data[['group']])
  lapply(1:length(groups[['lengths']]), function(i) {
    v <- groups[['values']][i]
    l <- groups[['lengths']][i]
    i0 <- if (i == 1) 1 else sum(groups[['lengths']][1:max(1, (i - 1))]) + 1
    list(label=group.labels[v], data=data[i0:(i0 + l - 1), , drop=FALSE])
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
  for (row in 1:length(ci.data[['labels']])) {
    for (col in columns) {
      if (!is.null(ci.data[['labels']][[row]][[col]])){
        pushViewport(viewport(layout.pos.row=layout.row, layout.pos.col=2 * which(columns == col) - 1))
        grid.draw(ci.data[['labels']][[row]][[col]])
        popViewport()
      }
    }
    if (!is.null(ci.data[['ci.plot']][[row]])) {
      pushViewport(viewport(layout.pos.row=layout.row, layout.pos.col=2*nc+1))
      grid.draw(ci.data[['ci.plot']][[row]])
      popViewport()
      pushViewport(viewport(layout.pos.row=layout.row, layout.pos.col=2*nc+3))
      grid.draw(ci.data[['ci.label']][[row]])
      popViewport()
    }
    layout.row <- layout.row + 1
  }
  layout.row
}

grob.ci <- function(pe, ci.l, ci.u, xrange, style) {
  if (is.na(pe) || is.na(ci.l) || is.na(ci.u)) {
    return(grob())
  }

  grob.pe <- NULL
  if(style[['pe.style']] == "circle") {
    grob.pe <- circleGrob(x=unit(pe, "native"), y=0.5, r=unit(0.2, "snpc"), gp=gpar(col="black"))
  } else { # Default is square
    grob.pe <- rectGrob(x=unit(pe, "native"), y=0.5, width=unit(0.2, "snpc"), height=unit(0.2, "snpc"), gp=gpar(fill="black",col="black"))
  }

  build.arrow <- function(ends) {
    arrow(ends=ends, length=unit(0.5, "snpc"))
  }

  arrow <- if (ci.l < xrange[[1]] && ci.u > xrange[[2]]) build.arrow("both")
          else if(ci.l < xrange[[1]]) build.arrow("first")
          else if (ci.u > xrange[[2]]) build.arrow("last")
          else NULL

  line <- linesGrob(x=unit(c(max(ci.l, xrange[[1]]), min(ci.u, xrange[[2]])), "native"), arrow=arrow, y=0.5, gp=gpar(lty=style[['lty']]))

  if (pe > xrange[1] && pe < xrange[2]) {
    ciGrob <- gTree(children=gList(
      line,
      grob.pe
    ))
  } else {
    ciGrob <- gTree(children=gList(
      line
    ))
  }
  ciGrob[['vp']] <- viewport(xscale=xrange)
  ciGrob
}

text.style  <- function(styles) {
  function(text, style) {
    ff <- styles[as.character(style), ,drop=TRUE][['font.weight']]
    ff <- if (is.na(ff)) "plain" else ff
    textGrob(text, x=unit(0, "npc"), just="left", gp=gpar(fontface=ff))
  }
}

draw.page <- function(ci.data, colwidth, rowheights, ci.label, grouped,
                      columns, column.groups, column.group.labels,
                      header.labels, text.fn, xrange, scale.trf, left.label,
                      right.label, center.label, draw.no.effect=TRUE) {
  columns.grouped <- !is.null(column.groups)
  row.offset <- if (columns.grouped) 2 else 1

  # Initialize plot and layout
  rowheight <- unit.c(unit(rep(1, row.offset), "lines"), rowheights, unit(c(0.5, 1), "lines"), unit(c(0.2, 1), "lines"))
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

  if (draw.no.effect) {
      ## No-effect line
      pushViewport(viewport(layout.pos.col=2*nc+1, layout.pos.row=(row.offset+1):(nr), xscale=xrange))
      grid.lines(x=unit(c(0, 0), "native"), y=unit(c(0, 1), "npc"))
      popViewport()
  }

  # Axis and ticks
  pushViewport(viewport(layout.pos.col=2*nc+1, layout.pos.row=nr, xscale=xrange))
  grid.lines(x=unit(c(0, 1), "npc"), y=unit(1, "npc"))
  grid.lines(x=unit(0, "npc"), y=unit(c(0, 1), "npc"))
  grid.lines(x=unit(1, "npc"), y=unit(c(0, 1), "npc"))
  popViewport()

  # Tick labels
  pushViewport(viewport(layout.pos.col=2*nc+1, layout.pos.row=nr+1, xscale=xrange))
  if(draw.no.effect) {
      grid.draw(textGrob(scale.trf(0), just="center", x=unit(0, "native")))
  }
  grid.draw(textGrob(scale.trf(xrange[1]), just="center", x=unit(0, "npc")))
  grid.draw(textGrob(scale.trf(xrange[2]), just="center", x=unit(1, "npc")))
  popViewport()

  x.offset <- if(draw.no.effect) 0 else mean(xrange)

  pushViewport(viewport(layout.pos.col=2*nc+1, layout.pos.row=nr+3, xscale=xrange))
  if (!is.null(left.label))
    grid.draw(textGrob(left.label, just="right", x=sum(unit(x.offset, "native"), unit(-0.2, "npc"))))
  if (!is.null(right.label))
    grid.draw(textGrob(right.label, just="left", x=sum(unit(x.offset, "native"), unit(0.2, "npc"))))
  if (!is.null(center.label))
    grid.draw(textGrob(center.label, just="center", x=sum(unit(x.offset, "native"), unit(0, "npc"))))  
  popViewport()
}

blobbogram.styles.default <- function() {
  styles <- data.frame(
    style=c('normal', 'pooled', 'group'),
    font.weight=c('plain', 'plain', 'bold'),
    row.height=c(1, 1, 1.5),
    pe.style=c('circle', 'square', NA),
    pe.scale=c(FALSE, FALSE, NA),
    lty=c(1,1,NA))
  rownames(styles) <- styles[['style']]
  styles
}

formatCI <- function(ci, scale.trf=identity, digits=2) {
  if (all(!is.na(ci))) {
    fmt <- lapply(ci, function(x) { formatC(scale.trf(x), digits=digits, zero.print="0", flag="#") })
    paste(fmt[['pe']], " (", fmt[['ci.l']], ", ", fmt[['ci.u']], ")", sep="")
  } else {
    "NA"
  }
}

nice.value <- function(x, round.fun, log.scale) {
  # Scale transform and its inverse
  scale.trf <- if (log.scale) exp else identity
  scale.inv <- if (log.scale) log else identity

  # Round to a single significant digit, according to round.fun
  y <- scale.trf(x)
  p <- 10^floor(log10(abs(y)))
  l <- scale.inv(round.fun(y / p) * p)
  if (is.na(l) || !is.finite(l)) x else l
}

blobbogram <- function(data, id.label='Study', ci.label="Mean (95% CI)",
                       left.label=NULL, right.label=NULL, center.label=NULL,
                       log.scale=FALSE, xlim=NULL, styles=NULL,
                       grouped=TRUE, group.labels=NULL,
                       columns=NULL, column.labels=NULL,
                       column.groups=NULL, column.group.labels=NULL,
                       digits=2,
                       ask=dev.interactive(orNone=TRUE),
                       draw.no.effect=TRUE) {

  devAskNewPage(FALSE)
  plot.new()
  devAskNewPage(ask)

  grouped <- !is.null(group.labels) && isTRUE(grouped)

  if (is.null(styles)) {
    styles <- blobbogram.styles.default()
  }
  text.fn <- text.style(styles)

  # Rewrite input: split into groups
  data <- if (grouped) {
    get.row.groups(data, group.labels)
  } else {
    list(list(label=NULL, data=data))
  }

  columns.grouped <- !is.null(column.groups)

  # Add default ('id') column
  columns <- c('id', columns)
  column.labels <- c(id.label, column.labels)
  
  # Scale transform and its inverse
  scale.trf <- if (log.scale) exp else identity
  scale.inv <- if (log.scale) log else identity

  # Round to a single significant digit, according to round.fun
  nice <- function(x, round.fun) {
    nice.value(x, round.fun, log.scale)
  }

  # Calculate plot range
  xrange <- if (is.null(xlim)) {
    ci.l <- do.call(c, lapply(data, function(datagrp) { datagrp[['data']][, 'ci.l', drop=TRUE]}))
    ci.u <- do.call(c, lapply(data, function(datagrp) { datagrp[['data']][, 'ci.u', drop=TRUE]}))
    c(min(nice(min(ci.l, na.rm=TRUE), floor), 0), max(nice(max(ci.u, na.rm=TRUE), ceiling), 0))
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
      labels <- apply(datagrp[['data']], 1, rowToGrobs)

      ci.data <- lapply(1:nrow(datagrp[['data']]), function(i) {
        # Create CI plots
        fmt <- datagrp[['data']][i, c('pe', 'ci.l', 'ci.u'), drop=TRUE]
        ci <- grob.ci(fmt[['pe']], fmt[['ci.l']], fmt[['ci.u']], xrange,
          styles[as.character(datagrp[['data']][i, 'style', drop=TRUE]), , drop=TRUE])

        # Create CI interval labels (right side)
        text <- formatCI(fmt, scale.trf, digits=digits)
        label <-  text.fn(text, as.character(datagrp[['data']][i, 'style', drop=TRUE]))

        list(ci=ci, label=label)
      })
      ci.plot <- lapply(ci.data, function(x) { x[['ci']] })
      ci.label <- lapply(ci.data, function(x) { x[['label']] })

      list(labels=labels, ci.plot=ci.plot, ci.label=ci.label)
    })
  names(forest.data) <- lapply(data, function(grp) { grp[['label']] })

  forest.data.index <- function(fd, idx) {
    list(labels=fd[['labels']][idx], ci.plot=fd[['ci.plot']][idx], ci.label=fd[['ci.label']][idx])
  }

  if (columns.grouped) {
    column.group.labels.grob <- rowToGrobs(column.group.labels)
  }

  # Calculate column widths
  colgap <- unit(3, "mm")
  colwidth <- do.call(unit.c, lapply(columns, function(col) {
    col <- c(header.labels[col], do.call(c, lapply(forest.data, function(grp) { sapply(grp[['labels']], function(row) { row[col] }) })))
    col <- col[!sapply(col, is.null)]
    unit.c(max(unit(rep(1, length(col)), "grobwidth", col)), colgap)
  }))

  # Adjust column widths so column group labels fit
  if (columns.grouped) {
    groups <- names(column.group.labels)
    if (is.null(groups)) {
      groups <- 1:length(column.group.labels)
    }
    for (group in groups) {
      gl <- column.group.labels.grob[group]
      select <- column.groups == group
      for (i in which(select)) {
        colwidth[[2 * i + 1]] <- max(unit(1.0 / sum(select), "grobwidth", gl), colwidth[[2 * i + 1]])
      }
    }
  }

  # Adjust column widths so row group labels fit
  if (grouped) {
    labels <- lapply(group.labels, text.fn, 'group')
    for (i in 1:length(columns))
    colwidth[[2 * i - 1]] <- max(max(unit(rep(1.0 / length(columns), length(labels)), "grobwidth", labels)), colwidth[[2 * i - 1]])
  }

  graphwidth <- unit(5, "cm")
  all.ci.labels <- do.call(c, lapply(forest.data, function(x) { x[['ci.label']] }) )

  ci.colwidth <- max(unit(rep(1, length(all.ci.labels)), "grobwidth", all.ci.labels))
  colwidth <- unit.c(colwidth, graphwidth, colgap, ci.colwidth)

  groupHeight <- function(grp) {
    if (grouped) {
      unit(
        c(styles['group', 'row.height'],
          styles[as.character(grp[['data']][['style']]), 'row.height', drop=TRUE]
        ), "lines")
    } else {
      unit(styles[as.character(grp[['data']][['style']]), 'row.height', drop=TRUE], "lines")
    }
  }

  groupHeightNPC <- function(grp) {
    convertY(sum(groupHeight(grp)), "npc", valueOnly=TRUE)
  }

  # divide data into pages
  height <- sum(unit.c(unit(rep(1, if (columns.grouped) 2 else 1), "lines"), unit(c(0.5, 1), "lines"), unit(c(0.2, 1), "lines")))
  space <- 1.0 - convertY(height, "npc", valueOnly=TRUE)

  pages <- list(list())
  height <- 0
  for (i in 1:length(data)) {
    myHeight <- groupHeightNPC(data[[i]])
    if (myHeight > space) {
      # Create (sub) groups that fit
      i0 <- 1
      i1 <- 1
      while (i0 <= nrow(data[[i]][['data']])) {
        if (height / space > 0.7) { # create a new page
          pages <- c(pages, list(list()))
          height <- 0
        }
        while (groupHeightNPC(list(data=data[[i]][['data']][i0:i1, , drop=FALSE])) < (space - height) && i1 <= nrow(data[[i]][['data']])) {
          i1 <- i1 + 1
        }
        block <- list(list(
          forest.data=forest.data.index(forest.data[[i]], i0:(i1 - 1)),
          rowheight=groupHeight(list(data=data[[i]][['data']][i0:(i1 - 1), , drop=FALSE]))
          ))
        names(block) <- names(forest.data)[i]
        pages[[length(pages)]] <- c(pages[[length(pages)]], block)
        i0 <- i1
        height <- space
      }
    } else if (height + myHeight > space) { # new page
      height <- myHeight
      block <- list(list(
        forest.data=forest.data[[i]],
        rowheight=groupHeight(data[[i]])
        ))
      names(block) <- names(forest.data)[i]
      pages <- c(pages, list(block))
    } else { # append to this page
      height <- height + myHeight
      block <- list(list(
        forest.data=forest.data[[i]],
        rowheight=groupHeight(data[[i]])
        ))
      names(block) <- names(forest.data)[i]
      pages[[length(pages)]] <- c(pages[[length(pages)]], block)
    }
  }

  # Now plot each group
  for (i in 1:length(pages)) {
    page <- pages[[i]]
    if (length(page) > 0) {
      rowheights <- do.call(unit.c, lapply(page, function(grp) { grp[['rowheight']] }))
      fd <- lapply(page, function(grp) { grp[['forest.data']] })
      dev.hold()
      if (i > 1) grid.newpage()
      draw.page(fd, colwidth, rowheights, ci.label,
                grouped, columns, column.groups,
                column.group.labels, header.labels,
                text.fn, xrange, scale.trf,
                left.label, right.label, center.label, draw.no.effect)
      dev.flush()
    }
  }
}
