#!/bin/bash

echo '---- Getting version from Maven'
mvn validate || exit

VERSION=`cat version`
rm version
GUIDIR=gemtc-gui-$VERSION
CLIDIR=gemtc-cli-$VERSION

GUIREADME=mtc-gui/README.md

if [ "$VERSION" = '' ]; then
	echo '!!!! Error: could not get version';
	exit;
else
	echo "---- Version: $VERSION"
fi

if [[ "$VERSION" == *-SNAPSHOT ]]; then
	echo '!!!! Not packaging -SNAPSHOT';
	exit;
fi;

if grep -q $VERSION: $GUIREADME
then
  echo "---- README up-to-date"
else 
  echo "!!!! Could not find version $VERSION in GeMTC-GUI README.md, please update appropriately"
  exit
fi

# Add license to all files
echo '---- Putting license on all sources'
ant license || exit

# Package
echo '---- Building JAR'
mvn clean package -q || exit

generate-readme() {
	echo '<html><head><style type="text/css">h1 {font-size: 20pt;}</style><title>GeMTC README</title></head><body>' > $2
	markdown $1 >> $2
	echo '</body></html>' >> $2
}

# Package the GUI
rm -Rf $GUIDIR
mkdir $GUIDIR
ARTIFACT=$GUIDIR/gemtc-gui-$VERSION.jar
cp mtc-gui/target/mtc-gui-$VERSION-jar-with-dependencies.jar $ARTIFACT
chmod a+x $ARTIFACT 
cp LICENSE.txt $GUIDIR
generate-readme mtc-gui/README.md $GUIDIR/README.html
cp mtc-gui/README.md $GUIDIR/README.txt
mkdir $GUIDIR/example
cp example/*.gemtc $GUIDIR/example/
cp r-code/gemtc/R/mtc.R $GUIDIR/
zip -r gemtc-gui-$VERSION.zip $GUIDIR

# Package the CLI
rm -Rf $CLIDIR
mkdir $CLIDIR
ARTIFACT=$CLIDIR/gemtc-cli-$VERSION.jar
cp mtc-cli/target/mtc-cli-$VERSION-jar-with-dependencies.jar $ARTIFACT
chmod a+x $ARTIFACT 
cp LICENSE.txt $CLIDIR
generate-readme mtc-cli/README.md $CLIDIR/README.html
cp mtc-cli/README.md $CLIDIR/README.txt
mkdir $CLIDIR/example
cp example/*.gemtc $CLIDIR/example/
cp r-code/gemtc/R/mtc.R $CLIDIR/
zip -r gemtc-cli-$VERSION.zip $CLIDIR

# Package the R package
#cd r-code
#make gemtc_$VERSION.tar.gz
#mv gemtc_$VERSION.tar.gz ../gemtc_$VERSION-EXPERIMENTAL.tar.gz
#cd ..
