#!/bin/bash

#java -cp target/mtc-main-0.8.2-SNAPSHOT-jar-with-dependencies.jar:target/classes/ org.drugis.mtc.gui.MainWindow
mvn compile exec:java -Dexec.mainClass="org.drugis.mtc.gui.MainWindow"
