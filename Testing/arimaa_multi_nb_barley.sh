#!/bin/bash


# VARIABLE TO CONFIGURE: jar path relative to the Arimaa directory
JAR_PATH=../ArimaaBot/mysql-connector-java-5.1.26-bin.jar

# tell grid engine to merge stdout and stderr streams
#$ -j y

# mail to this address
#$ -M vhchoksi@stanford.edu
# send mail on beginning, ending, or suspension of job
#$ -m bes

# tell grid engine to use current directory
#$ -cwd

# run the java program with the same args as the args to this script
# this assumes that we're running from the root Arimaa directory

# Configure the paths in this line if your directory structure is different! 
#java -Xmx500000000 -classpath $JAR_PATH:../ArimaaBot/bin:../ArimaaBot naive_bayes.NBMain $1 $2 $3 
java -classpath $JAR_PATH:../ArimaaBot/bin:../ArimaaBot naive_bayes.MultiNBMain $1 $2 $3 
