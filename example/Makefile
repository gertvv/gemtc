SAMPLERS:=jags winbugs openbugs
PREFIX:=$(addprefix verify.example.,$(SAMPLERS))
all: regression_test.txt $(addsuffix .txt,$(PREFIX))
.PHONY: all

BUGSTMP:=~/.wine/drive_c/bugstmp

verify.example.%.txt: verify.example.%.R verify.R
	mkdir -p $(BUGSTMP)
	TMPDIR=$(BUGSTMP) R --vanilla --slave --file=$< >$@ 2>&1
	rmdir $(BUGSTMP)

verify.example.%.R:
	echo 'source("verify.R"); lapply(examples, verify.example.$*)' > $@

regression_test.txt: regression_test.R
	R --vanilla --slave --file=$< >$@ 2>&1

regression_test.R: regression_test_*.R
	cat $^ > $@

clean:
	rm -f regression_test.txt $(addsuffix .txt,$(PREFIX))
