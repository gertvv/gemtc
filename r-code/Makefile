all: gemtc_0.15-SNAPSHOT.tar.gz

gemtc_%.tar.gz: ../mtc-cli/target/mtc-cli-%-jar-with-dependencies.jar gemtc/R/*.R gemtc/man/*.Rd gemtc/DESCRIPTION gemtc/NAMESPACE ../example/*.gemtc
	rm -Rf gemtc/inst/extdata
	rm -Rf gemtc/inst/java
	mkdir -p gemtc/inst/extdata
	mkdir gemtc/inst/java
	cp ../example/*.gemtc gemtc/inst/extdata
	cp $< gemtc/inst/java
	R CMD build gemtc
	R CMD check gemtc
