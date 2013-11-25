#!/bin/bash

# tell grid engine to merge stdout and stderr streams
#$ -j y

# tell grid engine to use current directory
#$ -cwd

# run the java program with the same args as the args to this script
java -Xmx500000000 -classpath ./mysql-connector-java-5.1.26-bin.jar:./bin:./ naive_bayes.NBMain $1 $2 $3 
