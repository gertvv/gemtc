#!/bin/bash

. env.sh
echo $LD_LIBRARY_PATH
scala -cp target/*-jar-with-dependencies.jar -Djava.library.path=${LD_LIBRARY_PATH} network.scala $1.xml

# jags $1.script
