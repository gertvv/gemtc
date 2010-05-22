drugis.org MTC - Mixed Treatment Comparison library
===================================================

drugis.org MTC is a library for Mixed Treatment Comparison (MTC) model
generation. MTC is also known as Network Meta-Analysis.

The library is capable of generating JAGS (an open-source variant of
BUGS) models for both dichotomous and continuous data structures. The
data is provided in an XML file and processed by the library to generate
the correct parametrization, either a consistency or an inconsistency
model.

Currently, the MTC library can be used either as a command-line
application to generate JAGS models, or as a Java library that can also
run the MTC models using the Yadas pure Java MCMC package. The latter
option is used by ADDIS (http://drugis.org/addis) to perform network
meta-analysis in a graphical user interface (if you are looking for a
tool in which to play around with network meta-analysis, ADDIS may be
what you are looking for).

Several example XML files are provided with the MTC distribution to
demonstrate the capabilities of the library. All examples were taken
from published network meta-analyses, and the references to the original
papers are in the respective XML files.

Requirements
------------

drugis.org MTC is written in a mixture of Java and Scala, and requires
Java 5 (JRE 1.5) to run (http://java.com). To run the generated models,
you need JAGS 1.0.3 (http://sourceforge.net/projects/mcmc-jags/) and you
may want to use R (http://r-project.org) to perform post-processing and
analysis. Future versions may include scripts to aid in the process.

The JAGS model language is a close analogue of the (Win)BUGS language,
so you should be able to run the generated models in WinBUGS with little
or no modification. However, this is untested.

Using drugis.org MTC with JAGS
------------------------------

For example, say we have a data file 'cardiovasc.xml'. To generate and
run the model, do the following (on a command line; in windows, use
start -> run -> type 'cmd' -> ok).

 $ cd directory_with_data_file_and_mtc

 $ java -jar mtc-0.2.jar --consistency cardiovasc.xml cardiovasc_cons

    ... some output follows ...   

 $ jags cardiovasc_cons.script

    ... running model ...

 $ R

 > source('cardiovasc_cons.R')
 > summary(trace)

    ... summary of simulation results ...

To run an inconsistency model, replace --consistency with
--inconsistency, or leave out the switch.

Versions
--------

0.4: Improved parametrization algorithm.

0.2: Initial release. JAGS and Yadas model generation. Java API for
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
