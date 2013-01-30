.onLoad <- function(libname, pkgname) {
  .jinit(parameters = "-Xmx512m")
  .jpackage(pkgname, lib.loc = libname)
}
