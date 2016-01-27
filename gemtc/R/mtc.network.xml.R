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
