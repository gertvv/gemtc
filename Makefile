# To check withouth BRugs/R2WinBUGS installed:
# export _R_CHECK_FORCE_SUGGESTS_=false

read_version = $(shell grep 'Version:' $1/DESCRIPTION | sed 's/Version: //')

PKG_NAME := gemtc
PKG_VERSION := $(call read_version,$(PKG_NAME))
PACKAGE := $(PKG_NAME)_$(PKG_VERSION).tar.gz

JAR_PKG_NAME := $(PKG_NAME).jar
JAR_VERSION := $(call read_version,$(JAR_PKG_NAME))
JAR_URL := http://drugis.org/mvn/org/drugis/mtc/mtc-mcmc/$(JAR_VERSION)/mtc-mcmc-$(JAR_VERSION)-jar-with-dependencies.jar
JAR := mtc-mcmc-$(JAR_VERSION).jar
JAR_PACKAGE := $(JAR_PKG_NAME)_$(JAR_VERSION).tar.gz

all: $(PACKAGE)

$(PACKAGE): $(PKG_NAME)/src/*.c $(PKG_NAME)/R/*.R $(PKG_NAME)/man/*.Rd $(PKG_NAME)/DESCRIPTION $(PKG_NAME)/NAMESPACE ../example/*.gemtc install-$(JAR_PACKAGE)
	rm -Rf $(PKG_NAME)/inst/extdata
	mkdir -p $(PKG_NAME)/inst/extdata
	cp ../example/*.gemtc $(PKG_NAME)/inst/extdata
	cp samples/*.gz $(PKG_NAME)/inst/extdata
	rm -f $(PKG_NAME)/src/*.o $(PKG_NAME)/src/*.so
	R CMD build $(PKG_NAME)

$(JAR_PACKAGE): $(JAR) $(JAR_PKG_NAME)/DESCRIPTION
	rm -Rf $(JAR_PKG_NAME)/inst/java
	mkdir -p $(JAR_PKG_NAME)/inst/java
	cp $(JAR) $(JAR_PKG_NAME)/inst/java
	R CMD build $(JAR_PKG_NAME)

$(JAR):
	wget -O $@ $(JAR_URL)

install-$(JAR_PACKAGE): $(JAR_PACKAGE)
	R CMD INSTALL $(JAR_PKG_NAME)
	touch install-$(JAR_PACKAGE)

.PHONY: install

install: $(PACKAGE)
	_R_CHECK_FORCE_SUGGESTS_=FALSE R CMD check $(PACKAGE)
	R CMD INSTALL $(PACKAGE)
