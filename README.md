GeMTC - Mixed Treatment Comparison library
==========================================

[GeMTC][1] is a library for Mixed Treatment Comparison (MTC) model
generation. MTC is also known as Network Meta-Analysis.

The library is capable of generating JAGS and BUGS models for both
dichotomous and continuous data structures. The data is provided in an
XML file and processed by the library to generate the correct
parametrization, whether a consistency, an inconsistency or a node-split
model.

Currently, the GeMTC library can be used as either:

  * A graphical user interface (GUI) application with built-in data
    management and model generation capabilities. The generated models
    can be run using JAGS or BUGS, or by GeMTC GUI itself.

  * An R package.

  * A Java library  that can also run the MTC models using the YADAS
    pure Java MCMC package. This is used by [ADDIS][2] to perform
    network meta-analysis.

Requirements
------------

GeMTC is built in [Java][2], and requires Java 6 (JRE 1.6) or newer.

Optionally, [JAGS][3], [OpenBUGS][4], or [WinBUGS][5] can be used to
estimate the generated Bayesian models.

The [R][6] package requires [rJava][7] and several other packages.

Versions
--------
0.14.3: Fixed bug in initial values calculation and minor improvements 

0.14.1: Bugfix and minor improvements.

0.14: Moved analysis functionality from ADDIS into GeMTC GUI, enabling
the estimation of MTC models directly within GeMTC GUI. Removed GeMTC
CLI.

0.12.4: Library updates for ADDIS 1.14.

0.12.3: Bugfixes.

0.12.2: Bugfixes and minor improvements.

0.12.1: Sort studies and treatments alphabetically, fix flaw in prior
generation, and other bugfixes. Improve integration with ADDIS and allow
the simulation to be extended after assessment of convergence.

0.12: Complete rewrite in Java, redesigned node-splitting model
generation.

0.10.1: Bugfixes, generate R code for JAGS models.

0.10: Addition of GeMTC GUI, improved consistency model generation,
support for BUGS. Renamed to GeMTC CLI and GeMTC GUI.

0.8: Starting value generation for JAGS models, JAGS node-split models,
provide R code for analysis.

0.6: Node-splitting models, assessment of convergence, starting value
generation, allow access to samples (all for YADAS only, JAGS models
have not advanced). Modularized into 4 libraries.

0.4: Improved parametrization algorithm. Allow output of Network as XML.
Check connectedness of network. Some issues fixed.

0.2: Initial release. JAGS and YADAS model generation. Java API for
running MTC models.

Building from source
--------------------

Source code can be obtained from [the GeMTC site][1]. In order to build
GeMTC you need:

 - [Java >= 1.6][2]
 - [Maven 2][8]
 - Other dependencies are downloaded automatically by Maven

To build, use "mvn package". Due to downloading dependencies, the first
time this may take a while.

License
-------

GeMTC is open source, and licensed under GPLv3. See LICENSE.txt for more
information.

Contact
-------

Contact the author, Gert van Valkenhoef, g.h.m.van.valkenhoef@rug.nl,
for more information.


[1]: http://drugis.org/gemtc
[2]: http://www.java.com/getjava/
[3]: http://sourceforge.net/projects/mcmc-jags/
[4]: http://www.openbugs.info/
[5]: http://www.mrc-bsu.cam.ac.uk/bugs/
[6]: http://r-project.org/
[7]: http://www.rforge.net/rJava/
[8]: http://maven.apache.org/
