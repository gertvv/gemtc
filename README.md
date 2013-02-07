GeMTC R package
===============

[GeMTC][1] is a library for Mixed Treatment Comparison (MTC) model
generation. MTC is also known as Network Meta-Analysis.

The GeMTC R package provides an R interface for the GeMTC library. The R
package is versioned separately, but depends on a specific version of
the GeMTC library. This is arranged as follows:

  * The 'gemtc' package contains all R code and has an independent
    version number. It depends on 'gemtc.jar'.

  * The 'gemtc.jar' package contains the GeMTC JAR file (the mtc-mcmc
    JAR with dependencies) and has the same version number as the JAR.

Building
--------

The build has only been tested on Ubuntu GNU/Linux. You need R, make,
and wget. To build and install the R package, simply 'make install'.

The required mtc-mcmc JAR file is automatically retrieved from the maven
repository, so the build is independent from the Java build.
