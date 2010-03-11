#!/bin/bash

scala -cp target/*-jar-with-dependencies.jar network.scala $1.xml
