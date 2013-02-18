SAMPLERS:=jags winbugs openbugs
PREFIX:=$(addprefix verify.example.,$(SAMPLERS))
all: $(addsuffix .txt,$(PREFIX))
.PHONY: all

BUGSTMP:=~/.wine/c_drive/bugstmp

verify.example.%.txt: verify.example.%.R verify.R
	mkdir -p $(BUGSTMP)
	TMPDIR=$(BUGSTMP) R --vanilla --slave --file=$< 2>&1 >$@
	rmdir $(BUGSTMP)

verify.example.%.R:
	echo 'source("verify.R"); lapply(examples, verify.example.$*)' > $@