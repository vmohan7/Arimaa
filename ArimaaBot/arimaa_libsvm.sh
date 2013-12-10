#!/bin/bash

# tell grid engine to merge stdout and stderr streams
#$ -j y

# tell grid engine to use current directory
#$ -cwd

#arguments is 1= the number of games; 
TRAIN=$(echo '.7 * ' $1 | bc | sed 's/[.].*//')
TEST=$(echo '.3 * ' $1 | bc | sed 's/[.].*//')
CACHEMB=$(echo '400 * ' $1 | bc | sed 's/[.].*//')

# change these to your desired folders :D
OUTPUT="/farmshare/user_data/neemazad/libSVM_rand_discard/barleyoutput"
DIRJ="/afs/ir.stanford.edu/users/n/e/neemazad/cs229/Arimaa/ArimaaBot"

mkdir $OUTPUT
java -classpath $DIRJ/mysql-connector-java-5.1.27-bin.jar:$DIRJ/bin:$DIRJ/ svm.SVMMain --train $OUTPUT/train$1.txt $TRAIN $OUTPUT/gameIDS$1.dat
$DIRJ/libsvm-3.17/svm-train $OUTPUT/train$1.txt $OUTPUT/model_$1.model
java -classpath $DIRJ/mysql-connector-java-5.1.27-bin.jar:$DIRJ/libsvm.jar:$DIRJ/liblinear-1.94.jar:$DIRJ/bin:$DIRJ/ svm.SVMMain --test $OUTPUT/model_$1.model $TEST $OUTPUT/gameIDS$1.dat 1
