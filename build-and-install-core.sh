#!/bin/sh

cd ~/os-git/git/onestonesoup/Core
mvn clean install
mvn install:install-file -Dfile=./target/core-0.0.9-SNAPSHOT.jar -DgroupId=org.onestonesoup -DartifactId=core -Dversion=0.0.9 -Dpackaging=jar
