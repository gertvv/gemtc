# To check withouth BRugs/R2WinBUGS installed:
# export _R_CHECK_FORCE_SUGGESTS_=false

read_version = $(shell grep 'Version:' $1/DESCRIPTION | sed 's/Version: //')

PKG_NAME := gemtc
PKG_VERSION := $(call read_version,$(PKG_NAME))
PACKAGE := $(PKG_NAME)_$(PKG_VERSION).tar.gz

all: $(PACKAGE)

$(PACKAGE):
	rm -f $(PKG_NAME)/src/*.o $(PKG_NAME)/src/*.so
	R CMD build $(PKG_NAME)

.PHONY: $(PACKAGE) install check

check: $(PACKAGE)
	_R_CHECK_FORCE_SUGGESTS_=FALSE R CMD check $(PACKAGE)

check-cran: $(PACKAGE)
	_R_CHECK_FORCE_SUGGESTS_=FALSE R CMD check --as-cran $(PACKAGE)

install: $(PACKAGE)
	R CMD INSTALL $(PACKAGE)

# Special test target since R CMD check is incredibly slow :-(
test: $(PACKAGE)
	mktemp -d > tmp.Rlib.loc
	R CMD INSTALL -l `cat tmp.Rlib.loc` --install-tests $(PACKAGE)
	echo "library($(PKG_NAME), lib.loc='`cat tmp.Rlib.loc`'); library(testthat); setwd('`cat tmp.Rlib.loc`/gemtc/tests'); test_check('gemtc')" | R --vanilla --slave
	rm -rf `cat tmp.Rlib.loc`
	rm tmp.Rlib.loc
