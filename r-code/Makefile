PKG_NAME=gemtc
PKG_VERSION=0.1
PACKAGE=$(PKG_NAME)_$(PKG_VERSION).tar.gz

JAR_VERSION=0.14.1
JAR_URL=http://drugis.org/mvn/org/drugis/mtc/mtc-mcmc/$(JAR_VERSION)/mtc-mcmc-$(JAR_VERSION)-jar-with-dependencies.jar
JAR=mtc-mcmc-$(JAR_VERSION).jar

all: $(PACKAGE)

$(PACKAGE): $(JAR) $(PKG_NAME)/src/*.c $(PKG_NAME)/R/*.R $(PKG_NAME)/man/*.Rd $(PKG_NAME)/DESCRIPTION $(PKG_NAME)/NAMESPACE ../example/*.gemtc
	rm -Rf $(PKG_NAME)/inst/extdata
	rm -Rf $(PKG_NAME)/inst/java
	mkdir -p $(PKG_NAME)/inst/extdata
	mkdir $(PKG_NAME)/inst/java
	cp ../example/*.gemtc $(PKG_NAME)/inst/extdata
	cp samples/*.gz $(PKG_NAME)/inst/extdata
	cp $(JAR) $(PKG_NAME)/inst/java
	R CMD build $(PKG_NAME)

$(JAR):
	wget -O $@ $(JAR_URL)

.PHONY: install

install: $(PACKAGE)
	R CMD check $(PKG_NAME)
	R CMD INSTALL $(PKG_NAME)
