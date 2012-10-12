PKG_NAME=gemtc
PKG_VERSION=0.15-SNAPSHOT
PACKAGE=$(PKG_NAME)_$(PKG_VERSION).tar.gz
JAR=../mtc-mcmc/target/mtc-mcmc-$(PKG_VERSION)-jar-with-dependencies.jar

all: $(PACKAGE)

$(PACKAGE): $(JAR) $(PKG_NAME)/src/*.c $(PKG_NAME)/R/*.R $(PKG_NAME)/man/*.Rd $(PKG_NAME)/DESCRIPTION $(PKG_NAME)/NAMESPACE ../example/*.gemtc
	rm -Rf $(PKG_NAME)/inst/extdata
	rm -Rf $(PKG_NAME)/inst/java
	mkdir -p $(PKG_NAME)/inst/extdata
	mkdir $(PKG_NAME)/inst/java
	cp ../example/*.gemtc $(PKG_NAME)/inst/extdata
	cp $(JAR) $(PKG_NAME)/inst/java
	R CMD build $(PKG_NAME)
#	R CMD check $(PKG_NAME)

.PHONY: install

install: $(PACKAGE)
	R CMD INSTALL $(PKG_NAME)
