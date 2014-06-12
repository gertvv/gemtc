standardize.treatments <- function(treatments) {
  treatments[['id']] <- as.factor(treatments[['id']])
  treatments[['description']] <- as.character(treatments[['description']])
  rownames(treatments) <- as.character(treatments[['id']])
  treatments[order(treatments[['id']]), , drop=FALSE]
}

standardize.data <- function(data, treatment.levels, re.order=FALSE) {
  data[['study']] <- factor(as.factor(data[['study']]))
  data[['treatment']] <- factor(as.character(data[['treatment']]), levels=treatment.levels)
  if (re.order) {
    na <- sapply(data[['study']], function(study) { sum(data[['study']] == study) })
    bl <- !is.na(data[['diff']])
    data <- data[order(na, data[['study']], bl, data[['treatment']]), , drop=FALSE]
  } else {
    data <- data[order(data[['study']], data[['treatment']]), , drop=FALSE]
  }
  if (nrow(data) > 0) {
    rownames(data) <- seq(1:nrow(data))
  }
  data
}

remove.onearm <- function(data, warn=FALSE) {
  # Remove 1-arm studies
  sel <- as.logical(sapply(data[,'study'], function(study) {
    sum(data[,'study'] == study) > 1
  }))

  if (warn && !all(sel)) {
    warning(paste("Removing", sum(!sel), "one-arm studies"))
  }
  data[sel, , drop=FALSE]
}

check.duplicated.treatments <- function(network, warn=FALSE) {
  duplicates <- FALSE
  for (study in mtc.studies.list(network)[['values']]) {
    design <- rle(sort(as.vector(mtc.study.design(network, study))))
    dup.v <- design[['values']][design[['lengths']] > 1]
    dup.l <- design[['lengths']][design[['lengths']] > 1]
    if (any(design[['lengths']] > 1)) {
      duplicates <- TRUE
      if (warn) warning(paste(dup.v, "occurs", dup.l, "times in", study))
    }
  }
  duplicates
}

mtc.network <- function(
  data.ab=data, treatments=NULL, description="Network",
  data.re=NULL, data=NULL
) {
  if (is.null(data.ab) && is.null(data.re)) {
    stop("Either `data.ab' or `data.re' (or both) must be specified")
  }
  # standardize the data
  if (!is.null(data.ab)) {
    if (!is.data.frame(data.ab)) {
      data.ab <- do.call(rbind, lapply(data.ab, as.data.frame))
    }
    data.ab <- remove.onearm(data.ab, warn=TRUE)
    mtc.validate.data.ab(data.ab)
  }
  if (!is.null(data.re)) {
    if (!is.data.frame(data.re)) {
      data.re <- do.call(rbind, lapply(data.re, as.data.frame))
    }
    data.re <- remove.onearm(data.re, warn=TRUE)
    mtc.validate.data.re(data.re)
  }

  # standardize the treatments
  if (is.null(treatments)) {
    data.treatments <- vector(mode="character")
    if (!is.null(data.ab)) {
      data.treatments <- c(data.treatments, as.character(data.ab[['treatment']]))
    }
    if (!is.null(data.re)) {
      data.treatments <- c(data.treatments, as.character(data.re[['treatment']]))
    }
    treatments <- unique(data.treatments)
  }
  if (is.list(treatments) && !is.data.frame(treatments)) {
    treatments <- as.data.frame(do.call(rbind, treatments))
  }
  if (is.character(treatments) || is.factor(treatments)) {
    treatments <- data.frame(id=treatments, description=treatments)
  }

  treatments <- standardize.treatments(treatments)

  network <- list(
    description=description,
    treatments=treatments)

  if (!is.null(data.ab)) {
    network <- c(network, list(data.ab=standardize.data(data.ab, levels(treatments[['id']]))))
  }
  if (!is.null(data.re)) {
    network <- c(network, list(data.re=standardize.data(data.re, levels(treatments[['id']]), re.order=TRUE)))
  }

  mtc.network.validate(network)

  class(network) <- "mtc.network"
  network
}

read.mtc.network <- function(file) {
  doc <- XML::xmlInternalTreeParse(file)
  description <- unlist(XML::xpathApply(doc, "/network", XML::xmlGetAttr, "description"))
  type <- unlist(XML::xpathApply(doc, "/network", XML::xmlGetAttr, "type", "rate"))
  treatments <- XML::xpathApply(doc, "/network/treatments/treatment",
    function(node) {
      c(
        id = XML::xmlGetAttr(node, "id"),
        description = XML::xmlValue(node)
      )
    }
  )
  if (identical(type, "rate")) {
    data.ab <- XML::xpathApply(doc, "/network/studies/study/measurement",
      function(node) {
        list(
          study = XML::xmlGetAttr(XML::xmlParent(node), "id"),
          treatment = XML::xmlGetAttr(node, "treatment"),
          responders = as.numeric(XML::xmlGetAttr(node, "responders")),
          sampleSize = as.numeric(XML::xmlGetAttr(node, "sample"))
        )
      }
    )
  } else if (identical(type, "continuous")) {
    data.ab <- XML::xpathApply(doc, "/network/studies/study/measurement",
      function(node) {
        list(
          study = XML::xmlGetAttr(XML::xmlParent(node), "id"),
          treatment = XML::xmlGetAttr(node, "treatment"),
          mean = as.numeric(XML::xmlGetAttr(node, "mean")),
          std.dev = as.numeric(XML::xmlGetAttr(node, "standardDeviation")),
          sampleSize = as.numeric(XML::xmlGetAttr(node, "sample"))
        )
      }
    )
  } else if (identical(type, "none")) {
    data.ab <- XML::xpathApply(doc, "/network/studies/study/measurement",
      function(node) {
        list(
          study = XML::xmlGetAttr(XML::xmlParent(node), "id"),
          treatment = XML::xmlGetAttr(node, "treatment")
        )
      }
    )
  }
  mtc.network(data.ab, treatments=treatments, description=description)
}

fix.network <- function(network) {
  # move data into data.ab for legacy networks 
  if (is.null(network[['data.ab']]) && !is.null(network[['data']])) {
    network[['data.ab']] <- network[['data']]
    network[['data']] <- NULL
  }
  network
}

write.mtc.network <- function(network, file) {
  if (!is.null(network[['data.re']]) && nrow(network[['data.re']]) > 0) {
    stop('write.mtc.network does not support data.re')
  }

  network <- fix.network(network)

  root <- XML::newXMLNode("network")
  XML::xmlAttrs(root)["description"] <- network[['description']]
  type <- if (all(c('responders', 'sampleSize') %in% colnames(network[['data.ab']]))) {
    'rate'
  } else if (all(c('mean', 'std.dev', 'sampleSize') %in% colnames(network[['data.ab']]))) {
    'continuous'
  } else {
    warning('write.mtc.network only supports dichotomous or continuous data; writing structure only.')
    'none'
  }
  XML::xmlAttrs(root)["type"] <- type

  treatments <- XML::newXMLNode("treatments", parent = root)
  apply(network[['treatments']], 1, function(row) {
    node <- XML::newXMLNode("treatment", parent = treatments)
    XML::xmlAttrs(node)["id"] <- row['id']
    XML::xmlValue(node) <- row['description']
  })

  studies <- XML::newXMLNode("studies", parent = root)
  study <- sapply(levels(network[['data.ab']][['study']]), function(sid) {
    node <- XML::newXMLNode("study", parent = studies)
    XML::xmlAttrs(node)["id"] <- sid
    node
  })

  apply(network[['data.ab']], 1, function(row) {
    node <- XML::newXMLNode('measurement', parent=study[[row['study']]])
    XML::xmlAttrs(node)['treatment'] <- row['treatment']
    if (identical(type, 'rate')) {
      XML::xmlAttrs(node)['responders'] <- row['responders']
      XML::xmlAttrs(node)['sample'] <- row['sampleSize']
    } else if (identical(type, 'continuous')) {
      XML::xmlAttrs(node)['mean'] <- row['mean']
      XML::xmlAttrs(node)['standardDeviation'] <- row['std.dev']
      XML::xmlAttrs(node)['sample'] <- row['sampleSize']
    }
    node
  })

  cat(XML::saveXML(root), file=file)
}

mtc.validate.data.ab <- function(data) {
  # No longer validating arm-based data at this stage
  # This is now defined at the likelihood/link level
}

mtc.validate.data.re <- function(data) {
  # These checks are still essential, because they check more then just the presence / absence of
  # columns.

  columns <- colnames(data)
  reColumns <- c('diff', 'std.err')
  if (!all(reColumns %in% columns)) {
    stop(paste('data.re must contain columns: ', paste(reColumns, collapse=', ')))
  }

  baselineCount <- sapply(unique(data[['study']]), function(study) { sum(is.na(data[['diff']][data[['study']] == study])) })
  if (!all(baselineCount == 1)) {
    stop('Each study in data.re must have a unique baseline arm (diff=NA)')
  }
  if (!all(!is.na(data[['std.err']][!is.na(data[['diff']])]))) {
    stop('All non-baseline arms in data.re must have std.err specified')
  }

  studies <- unique(data[['study']])
  ma <- sapply(studies, function(study) { sum(data[['study']] == study) > 2 })
  ma <- studies[ma]
  if (!all(!is.na(data[['std.err']][data[['study']] %in% ma]))) {
    stop('All multi-arm trials (> 2 arms) must have the std.err of the baseline specified')
  }
}

mtc.network.validate <- function(network) {
  # Check that there is some data
  stopifnot(nrow(network[['treatments']]) > 0)
  stopifnot(nrow(network[['data.ab']]) > 0 || nrow(network[['data.re']]) > 0)

  # Check that the treatments are correctly cross-referenced and have valid names
  all.treatments <- c(network[['data.ab']][['treatment']], network[['data.re']][['treatment']])
  all.treatments <- factor(all.treatments, levels=1:nlevels(network[['treatments']][['id']]), labels=levels(network[['treatments']][['id']]))
  stopifnot(all(all.treatments %in% network[['treatments']][['id']]))
  # stopifnot(all(network[['treatments']][['id']] %in% all.treatments)) -- disabled for node-splitting
  invalidId <- grep('^[a-zA-Z0-9_]*$', network[['treatments']][['id']], invert=TRUE)
  if (length(invalidId) > 0) {
    stop(paste0("\n", paste0(' Treatment name "',
      network[['treatments']][['id']][invalidId], '" invalid.', collapse="\n"),
      '\nTreatment names may only contain letters, digits, and underscore (_).'))
  }

  # Check that studies are not duplicated between [['data.ab']] and [['data.re']]
  if (!is.null(network[['data.ab']]) && !is.null(network[['data.re']])) {
    dup.study <- intersect(unique(network[['data.ab']][['study']]), unique(network[['data.re']][['study']]))
    if (length(dup.study) > 0) {
      stop(paste('Studies', paste(dup.study, collapse=", "), 'occur in both data and data.re'))
    }
  }

  # Check that the data frame has a sensible combination of columns
  if (!is.null(network[['data.ab']])) {
    mtc.validate.data.ab(network[['data.ab']])
  }

  # Check data.re is well formed
  if (!is.null(network[['data.re']])) {
    mtc.validate.data.re(network[['data.re']])
  }

  check.duplicated.treatments(network)
}

as.treatment.factor <- function(x, network) {
  v <- network[['treatments']][['id']]
  if (is.numeric(x)) {
    factor(x, levels=1:nlevels(v), labels=levels(v))
  } else if (is.factor(x) || is.character(x)) {
    x <- as.character(x)
    factor(x, levels=levels(v))
  }
}

mtc.merge.data <- function(network) {
  data.frame(
    study=c(
      as.character(network[['data.ab']][['study']]),
      as.character(network[['data.re']][['study']])),
    treatment=as.treatment.factor(c(
      network[['data.ab']][['treatment']],
      network[['data.re']][['treatment']]), network),
    stringsAsFactors=FALSE)
}

mtc.studies.list <- function(network) {
  rle(as.character(mtc.merge.data(network)[['study']]))
}

mtc.study.design <- function(network, study) {
  data <- mtc.merge.data(network)
  data[['treatment']][data[['study']] == study]
}

coerce.factor <- function(x, prototype) {
  factor(x, levels=1:nlevels(prototype), labels=levels(prototype))
}

# See nodesplit-auto draft for definition
has.indirect.evidence <- function(network, t1, t2) {
  has.both <- sapply(mtc.studies.list(network)[['values']], function(study) {
    all(c(t1, t2) %in% mtc.study.design(network, study))
  })

  data <- mtc.merge.data(network)
  not.both <- !has.both[data[['study']]]
  data <- data[not.both, , drop=FALSE]

  if (nrow(data) > 0) {
    n <- mtc.network(data)
    g <- mtc.network.graph(n)
    all(c(t1, t2) %in% V(g)$name) && as.logical(is.finite(shortest.paths(as.undirected(g), t1, t2)))
  } else {
    FALSE
  }
}

mtc.treatment.pairs <- function(treatments) {
  n <- length(treatments)
  t1 <- do.call(c, lapply(1:(n-1), function(i) { rep(treatments[i], n - i) }))
  t2 <- do.call(c, lapply(1:(n-1), function(i) { treatments[(i+1):n] }))
  data.frame(t1=coerce.factor(t1, treatments), t2=coerce.factor(t2, treatments))
}

# Get all comparisons with direct evidence from the data set.
# Returns a (sorted) data frame with two columns (t1 and t2).
mtc.comparisons <- function(network) {
  data <- mtc.merge.data(network)

  # Identify the unique "designs" (treatment combinations)
  design <- function(study) { mtc.study.design(network, study) }
  designs <- unique(lapply(unique(data[['study']]), design))

  # Generate all pair-wise comparisons from each "design"
  comparisons <- do.call(rbind, lapply(designs, mtc.treatment.pairs))

  # Make sure we include each comparison in only one direction
  swp <- as.character(comparisons[['t1']]) > as.character(comparisons[['t2']])
  tmp <- comparisons[['t1']]
  comparisons[['t1']][swp] <- comparisons[['t2']][swp]
  comparisons[['t2']][swp] <- tmp[swp]

  # Ensure the output comparisons are unique and always in the same order
  comparisons <- unique(comparisons)
  comparisons <- comparisons[order(comparisons[['t1']], comparisons[['t2']]), ,drop=FALSE]
  row.names(comparisons) <- NULL
  comparisons[['t1']] <- as.treatment.factor(comparisons[['t1']], network)
  comparisons[['t2']] <- as.treatment.factor(comparisons[['t2']], network)
  comparisons
}

edges.create <- function(e, ...) {
  e <- t(matrix(c(e[['t1']], e[['t2']]), ncol=2))
  edges(as.vector(e), ...)
}

graph.create <- function(v, e, ...) {
  g <- graph.empty()
  g <- g + vertex(levels(v))
  g <- g + edges.create(e, ...)
  g
}

mtc.network.graph <- function(network) {
  comparisons <- mtc.comparisons(network)
  treatments <- network[['treatments']][['id']]
  graph.create(treatments, comparisons, arrow.mode=0)
}

## mtc.network class methods
print.mtc.network <- function(x, ...) {
  x <- fix.network(x)
  cat("MTC dataset: ", x[['description']], "\n", sep="")
  if (!is.null(x[['data.ab']])) {
    cat('Arm-level data: \n')
    print(x[['data.ab']])
  }
  if (!is.null(x[['data.re']])) {
    cat('Relative effect data: \n')
    print(x[['data.re']])
  }
}

summary.mtc.network <- function(object, ...) {
  object <- fix.network(object)
  data <- mtc.merge.data(object)
  studies <- unique(data[['study']])
  m <- sapply(object[['treatments']][['id']], function(treatment) {
    sapply(studies, function(study) {
      any(data[['study']] == study & data[['treatment']] == treatment)
    })
  })
  colnames(m) <- object[['treatments']][['id']]
  x <- as.factor(apply(m, 1, sum))
  levels(x) <- sapply(levels(x), function(y) { paste(y, "arm", sep="-") })
  list("Description"=paste("MTC dataset: ", object[['description']], sep=""),
     "Studies per treatment"=apply(m, 2, sum),
     "Number of n-arm studies"=summary(x))
}

plot.mtc.network <- function(x, layout=igraph::layout.circle, ...) {
  x <- fix.network(x)
  igraph::plot.igraph(mtc.network.graph(x), layout=layout, ...)
}
