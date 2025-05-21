GeMTC R package
===============

[![Build Status](https://travis-ci.com/gertvv/gemtc.svg?branch=master)](https://travis-ci.com/gertvv/gemtc)
[![Build Status (develop)](https://travis-ci.com/gertvv/gemtc.svg?branch=develop)](https://travis-ci.com/gertvv/gemtc)

[GeMTC](http://drugis.org/gemtc) is an R package for Network
Meta-Analysis (also know as Mixed Treatment Comparison, MTC) model
generation.

Building
--------

Use `R CMD build gemtc` to build the R package. The `Makefile` offers a
number of targets for convenience, but is entirely optional. Use `make
install` to both build and install the package.

You will need a working installation of rjags, which in turn requires a
working installation of JAGS.

Testing
-------

The `testthat` package is used for testing. Tests reside in the
`tests/testthat` directory. There are three levels of tests:

 - `unit`: unit tests - these test relatively isolated pieces of
   functionality and should be fast to run. They will be run by `make
   test` or `R CMD check`.

 - `regress`: regression tests exercise full code paths, and typically
   run all code that an analyst would run on a given dataset. They aim
   to catch bugs in existing functionality due to the introduction of
   new code. They should not take very long to run, and do not aim to
   produce reasonable posterior estimates. These can be run using `make
   regress`.

 - `validate`: valition tests verify the posterior summaries obtained by
   the R package against results that were previously (manually) checked
   against results published in the literature. They typically take very
   long to run, and are probabilistic in nature. Many individual
   comparisons are made, so a couple of failures over the entire test
   suite are to be expected. However, these should not be systematic.
   These tests can be run using `make validate`.

License
-------

    GeMTC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GeMTC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GeMTC.  If not, see <http://www.gnu.org/licenses/>.

See LICENSE.txt for details.
