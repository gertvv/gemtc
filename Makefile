read_version = $(shell grep 'Version:' $1/DESCRIPTION | sed 's/Version: //')

R := R #/home/gert/Downloads/R-devel/bin/R
PKG_NAME := gemtc
PKG_VERSION := $(call read_version,$(PKG_NAME))
PACKAGE := $(PKG_NAME)_$(PKG_VERSION).tar.gz

all: $(PACKAGE)

$(PACKAGE): collate
	rm -f $(PKG_NAME)/src/*.o $(PKG_NAME)/src/*.so
	$(R) CMD build $(PKG_NAME)

.PHONY: $(PACKAGE) install check collate

check: $(PACKAGE)
	$(R) CMD check $(PACKAGE)

check-cran: $(PACKAGE)
	$(R) CMD check --as-cran $(PACKAGE)

# Note: the tryCatch is a workaround for https://github.com/klutometis/roxygen/issues/358
collate:
	cd $(PKG_NAME) && $(R) --vanilla --slave -e "library(roxygen2); tryCatch(roxygenize(roclets='collate'), error=function(e) {});"

install: $(PACKAGE)
	$(R) CMD INSTALL $(PACKAGE)

# Special test target since R CMD check is incredibly slow :-(
test: $(PACKAGE)
	./run-tests.sh $(R) $(PACKAGE) unit

validate: $(PACKAGE)
	./run-tests.sh $(R) $(PACKAGE) validate

validate-power: $(PACKAGE)
	./run-tests.sh $(R) $(PACKAGE) validate powerAdjust

regress: $(PACKAGE)
	./run-tests.sh $(R) $(PACKAGE) regress
