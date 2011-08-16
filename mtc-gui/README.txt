drugis.org MTC GUI - Mixed Treatment Comparisons
================================================

drugis.org MTC is a library for Mixed Treatment Comparison (MTC) model
generation. MTC is also known as Network Meta-Analysis. This program
provides a graphical interface to manage MTC data files and generate
JAGS syntax models.

The library is capable of generating JAGS (an open-source variant of
BUGS) models for both dichotomous and continuous data structures. The
data is provided in an XML file and processed by the library to generate
the correct parametrization, whether a consistency, an inconsistency or
a node-split model.

Currently, the MTC library can be used as a graphical or command-line
application to generate JAGS models, or as a Java library that can also
run the MTC models using the YADAS pure Java MCMC package. The latter
option is used by ADDIS (http://drugis.org/addis).

Several example XML files are provided with the MTC distribution to
demonstrate the capabilities of the library. All examples were taken
from published network meta-analyses, and the references to the original
papers are in the respective XML files.

Requirements
------------

drugis.org MTC is written in a mixture of Java and Scala, and requires
Java 5 (JRE 1.5) to run (http://java.com/). To run the generated models,
you need JAGS 1.0.4 (http://sourceforge.net/projects/mcmc-jags/) and you
may want to use R (http://r-project.org/) to perform post-processing and
analysis.

The JAGS model language is a close analogue of the (Win)BUGS language,
so you should be able to run the generated models in WinBUGS with little
or no modification. However, this is untested.

Known issues
------------

The node-split model generation algorithm is not completely developed
yet, and in some cases it generates a model that will not converge; this
should be evident from the time-series plot for the split node.

Using drugis.org MTC with JAGS
------------------------------

Double click the mtc-gui-0.10.jar file, and click "Open" to open an
existing data file (e.g. those provided in the examples directory) or
"New" to create a new one.

Versions
--------

0.10: Graphical User Interface (GUI) to generate consistency models, new
algorithm for consistency model generation.

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

Source code can be obtained from http://drugis.org/mtc. In order to
build the MTC library, you need:

 - Java >= 1.5
 - Maven 2
 - Other dependencies are downloaded automatically by Maven2

To build, use "mvn package". Due to downloading dependencies, the first
time this may take a while.

License
-------

drugis.org MTC is open source, and licensed under GPLv3. See LICENSE.txt
for more information.

Contact
-------

Contact the author, Gert van Valkenhoef, g.h.m.van.valkenhoef@rug.nl,
for more information. Also see http://drugis.org/mtc.
