GeMTC project strategy
======================

The recent conference at Tufts University and the fact that all the different versions are becoming unwieldy have prompted me to re-evaluate the development strategy for GeMTC.

This is the new strategy.

General
-------

 * Support ONLY the Bayesian / MCMC framework.

 * Java and R remain the primary platforms.

GeMTC GUI
---------

 * The code for running the MCMC models within Java will be adopted from ADDIS, as well as all the convergence diagnostics, tables, and plots.

 * In the future, any convergence diagnostics, tables, and plots developed for ADDIS will be implemented in GeMTC GUI.

 * GeMTC GUI will be the version "for dummies". It will NOT support multi-variate meta-analysis or meta-regression. This frees up a lot of development time that would otherwise be spent on tedious GUI work.

GeMTC CLI
---------

 * The command-line interface will be replaced by the R package, and will be removed as soon as the R package is completed.

GeMTC R-package
---------------

 * The R package will be the primary expert interface to GeMTC. Advanced methods will be provided here, not in GeMTC GUI.

 * The R package will NOT provide a GUI, this will be provided by OpenMeta-Analyst instead.

 * Various input data format conversions will need to be provided.

 * Specific summaries and plots will be provided tailored to network meta-analysis and/or the specific type of model. CODA will be used for most of the functionality.

 * Models can be run "internally" using YADAS, or "externally" using rjags or rbugs -- with preference as: rjags > rbugs > YADAS.

 * The dependency on Java will NOT go away (as I have not found any way of implementing the algorithms in a language that is easily integrated in both Java and R -- duplicate implementation is not an option).

GeMTC library
-------------

 * This is where the core algorithms are implemented, based on Apache commons-math and JUNG.

GeMTC MCMC
----------

 * Currently provides an implementation of the network meta-analysis MCMC models based on YADAS.

 * JAGS support may be added through a JNI wrapper for better performance.

ADDIS
-----

 * We will more clearly position ADDIS as a clinical trials information system / database PROTOTYPE, rather than network meta-analysis software. GeMTC GUI will be positioned as the "replacement" for those who are interested in network meta-analysis only -- data entry will definitely be less confusing and time consuming in GeMTC GUI.

Network meta-analysis tutorial
------------------------------

 * The tutorial currently being developed for ADDIS will be published as a research report, since we *really* need some documentation.

 * Once GeMTC GUI incorporates the functionality to perform the analyses from ADDIS, the tutorial will be ported to GeMTC GUI, improved substantially, and submitted to J Stat Softw.
