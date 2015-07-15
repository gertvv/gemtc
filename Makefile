# To check withouth BRugs/R2WinBUGS installed:
# export _R_CHECK_FORCE_SUGGESTS_=false

read_version = $(shell grep 'Version:' $1/DESCRIPTION | sed 's/Version: //')

PKG_NAME := gemtc
PKG_VERSION := $(call read_version,$(PKG_NAME))
PACKAGE := $(PKG_NAME)_$(PKG_VERSION).tar.gz

all: $(PACKAGE)

$(PACKAGE): collate
	rm -f $(PKG_NAME)/src/*.o $(PKG_NAME)/src/*.so
	R CMD build $(PKG_NAME)

.PHONY: $(PACKAGE) install check collate

check: $(PACKAGE)
	_R_CHECK_FORCE_SUGGESTS_=FALSE R CMD check $(PACKAGE)

check-cran: $(PACKAGE)
	_R_CHECK_FORCE_SUGGESTS_=FALSE R CMD check --as-cran $(PACKAGE)

# Note: the tryCatch is a workaround for https://github.com/klutometis/roxygen/issues/358
collate:
	cd $(PKG_NAME) && R --vanilla --slave -e "library(roxygen2); tryCatch(roxygenize(roclets='collate'), error=function(e) {});"

install: $(PACKAGE)
	R CMD INSTALL $(PACKAGE)

# Special test target since R CMD check is incredibly slow :-(
test: $(PACKAGE)
	./run-tests.sh $(PACKAGE) unit rjags

validate-jags: $(PACKAGE)
	./run-tests.sh $(PACKAGE) validate rjags

validate-winbugs: $(PACKAGE)
	./run-tests.sh $(PACKAGE) validate R2WinBUGS

validate-openbugs: $(PACKAGE)
	./run-tests.sh $(PACKAGE) validate BRugs

regress-jags: $(PACKAGE)
	./run-tests.sh $(PACKAGE) regress rjags

regress-winbugs: $(PACKAGE)
	./run-tests.sh $(PACKAGE) regress R2WinBUGS

regress-openbugs: $(PACKAGE)
	./run-tests.sh $(PACKAGE) regress BRugs
