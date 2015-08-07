#!/bin/bash

DIR=`mktemp -d -t gemtc.XXXXXX`
R CMD INSTALL -l $DIR --install-tests $1

R --vanilla --slave --file=run-tests.R --args $DIR $2 rjags
rm -rf $DIR
