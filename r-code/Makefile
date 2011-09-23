mtc_%.tar.gz: ../mtc-cli/target/mtc-cli-%-jar-with-dependencies.jar mtc/R/*.R mtc/man/*.Rd mtc/DESCRIPTION mtc/NAMESPACE ../example/*.gemtc
	rm -Rf mtc/inst/extdata
	rm -Rf mtc/inst/java
	mkdir -p mtc/inst/extdata
	mkdir mtc/inst/java
	cp ../example/*.gemtc mtc/inst/extdata
	cp $< mtc/inst/java
	R CMD build mtc
	R CMD check mtc
