#' @include mtc.network.R

read.mtc.network <- function(file) {
  # Check that the file exists and can be read.
  # This is not best practice, but the XML parser throws an incredibly cryptic
  # error if it can't read the file, so this seems better.
  if (file.access(file, 4) == -1) {
    stop(paste0("The file \"", file, "\" does not exist or can not be read."))
  }

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
