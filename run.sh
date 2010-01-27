#!/bin/bash

scala -cp target/classes network.scala $1.xml

jags $1.script
