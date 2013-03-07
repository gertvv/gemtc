# To check withouth BRugs/R2WinBUGS installed:
# export _R_CHECK_FORCE_SUGGESTS_=false

read_version = $(shell grep 'Version:' $1/DESCRIPTION | sed 's/Version: //')

PKG_NAME := gemtc
PKG_VERSION := $(call read_version,$(PKG_NAME))
PACKAGE := $(PKG_NAME)_$(PKG_VERSION).tar.gz

all: $(PACKAGE)

$(PACKAGE): $(PKG_NAME)/src/*.c $(PKG_NAME)/R/*.R $(PKG_NAME)/tests/*.R $(PKG_NAME)/inst/*.txt $(PKG_NAME)/inst/tests/*.R $(PKG_NAME)/man/*.Rd $(PKG_NAME)/DESCRIPTION $(PKG_NAME)/NAMESPACE ../example/*.gemtc
	rm -Rf $(PKG_NAME)/inst/extdata
	mkdir -p $(PKG_NAME)/inst/extdata
	cp ../example/*.gemtc $(PKG_NAME)/inst/extdata
	cp samples/*.gz $(PKG_NAME)/inst/extdata
	rm -f $(PKG_NAME)/src/*.o $(PKG_NAME)/src/*.so
	R CMD build $(PKG_NAME)

.PHONY: install

install: $(PACKAGE)
	_R_CHECK_FORCE_SUGGESTS_=FALSE R CMD check $(PACKAGE)
	R CMD INSTALL $(PACKAGE)

# Special test target since R CMD check is incredibly slow :-(
test: $(PACKAGE)
	mktemp -d > tmp.Rlib.loc
	R CMD INSTALL -l `cat tmp.Rlib.loc` $(PACKAGE)
	echo "library($(PKG_NAME), lib.loc='`cat tmp.Rlib.loc`'); source('$(PKG_NAME)/tests/test.R')" | R --vanilla --slave
	rm -rf `cat tmp.Rlib.loc`
