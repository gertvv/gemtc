mtc_0.8.tar.gz: mtc/R/*.R mtc/man/*.Rd mtc/DESCRIPTION mtc/NAMESPACE ../example/*.xml ../mtc-0.8/*.jar
	rm -Rf mtc/inst/extdata
	rm -Rf mtc/inst/java
	mkdir -p mtc/inst/extdata
	mkdir mtc/inst/java
	cp ../example/*.xml mtc/inst/extdata
	cp ../mtc-0.8/*.jar mtc/inst/java
	R CMD build mtc
	R CMD check mtc
