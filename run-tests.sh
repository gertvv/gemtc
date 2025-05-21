#!/bin/bash

DIR=`mktemp -d -t gemtc.XXXXXX`
$1 CMD INSTALL -l $DIR --install-tests $2

$1 --vanilla --slave --file=run-tests.R --args $DIR $3 $4
rm -rf $DIR
