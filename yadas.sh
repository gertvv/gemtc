#!/bin/bash

. env.sh
echo $LD_LIBRARY_PATH
scala -cp target/*-jar-with-dependencies.jar -Djava.library.path=${LD_LIBRARY_PATH} yadas.scala
