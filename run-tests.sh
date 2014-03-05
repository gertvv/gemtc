#!/bin/bash

DIR=`mktemp -d`
R CMD INSTALL -l $DIR --install-tests $1
R --vanilla --slave --file=run-tests.R --args $DIR $2 $3
rm -rf $DIR