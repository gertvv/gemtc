GeMTC GUI - Mixed Treatment Comparison model generation
=======================================================

[GeMTC][1] GUI provides a graphical interface to manage Mixed Treatment
Comparison (MTC) data files and generate analysis models. MTC is also
known as Network Meta-Analysis.

The software is capable of generating JAGS and BUGS models for both
dichotomous and continuous data structures. The data is provided in an
XML file and processed by the library to generate the correct
parametrization, whether a consistency, an inconsistency or a node-split
model.

The MTC models can also be estimated by GeMTC GUI itself, using the
YADAS implementation of MCMC for Java.

Several example XML files are provided with the GeMTC distribution to
demonstrate the capabilities of the library. All examples were taken
from published network meta-analyses, and the references to the original
papers are in the respective XML files.

Requirements
------------

GeMTC is built in [Java][2], and requires Java 6 (JRE 1.6) or newer.

Running GeMTC GUI
-----------------

Double click the mtc-gui-0.14.1.jar file, and click "Open" to open an
existing data file (e.g. those provided in the examples directory) or
"New" to create a new one.

Versions
--------

0.14.1: Bugfixes and minor improvements.

0.14: Moved analysis functionality from ADDIS into GeMTC GUI, enabling
the estimation of MTC models directly within GeMTC GUI.

0.12.4: Library updates for ADDIS 1.14.

0.12.3: Bugfixes.

0.12.2: Bugfixes and minor improvements.

0.12.1: Sort studies and treatments alphabetically, fix flaw in prior
generation, and other bugfixes. Improve integration with ADDIS and allow
the simulation to be extended after assessment of convergence.

0.12: Complete rewrite in Java, redesigned node-splitting model
generation.

0.10.1: Bugfixes, generate R code for JAGS models.

0.10: First GeMTC GUI version.

Building from source
--------------------

Source code can be obtained from [the GeMTC site][1]. In order to build
GeMTC you need:

 - [Java >= 1.6][2]
 - [Maven 2][3]
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
[3]: http://maven.apache.org/
