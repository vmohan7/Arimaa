#!/bin/bash

# tell grid engine to merge stdout and stderr streams
#$ -j y

# tell grid engine to use current directory
#$ -cwd

#arguments is 1= the number of games; 
TRAIN=$(echo '.7 * ' $1 | bc | sed 's/[.].*//')
TEST=$(echo '.3 * ' $1 | bc | sed 's/[.].*//')
CACHEMB=$(echo '400 * ' $1 | bc | sed 's/[.].*//')
OUTPUT="/farmshare/user_data/vmohan7/SVM_GAUSS_OUTPUT"
DIRJ="/afs/ir.stanford.edu/users/v/m/vmohan7/CS229/Arimaa/ArimaaBot"
mkdir $OUTPUT
java -classpath $DIRJ/mysql-connector-java-5.1.27-bin.jar:$DIRJ/bin:$DIRJ/ svm.SVMMain --train $OUTPUT/train$1.txt $TRAIN $OUTPUT/gameIDS$1.dat
$DIRJ/libsvm-3.17/svm-train -b 1 $OUTPUT/train$1.txt $OUTPUT/model_$1.model
java -classpath $DIRJ/mysql-connector-java-5.1.27-bin.jar:$DIRJ/libsvm.jar:$DIRJ/liblinear-1.94.jar:$DIRJ/bin:$DIRJ/ svm.SVMMain --test $OUTPUT/model_$1.model $TEST $OUTPUT/gameIDS$1.dat 1