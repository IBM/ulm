# Copyright © 2005, 2018 IBM Corp. * Licensed under the Apache license

all:	compile jar
	java -jar ulm.jar

jar:
	jar -cmf META-INF/MANIFEST.MF ulm.jar com/ibm/ptc/ulm/*.class

compile:
	javac -d . com/ibm/ptc/ulm/*.java

clean:
	rm ulm.jar com/ibm/ptc/ulm/*.class
