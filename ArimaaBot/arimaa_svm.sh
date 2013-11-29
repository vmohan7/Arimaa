#!/bin/bash

# tell grid engine to merge stdout and stderr streams
#$ -j y

# tell grid engine to use current directory
#$ -cwd

#arguments is 1= the number of games; 2 = the model
TRAIN=$(echo '.7 * ' $1 | bc | sed 's/[.].*//')
TEST=$(echo '.3 * ' $1 | bc | sed 's/[.].*//')
OUTPUT="/farmshare/user_data/vmohan7/OUTPUT"
mkdir $OUTPUT
java -classpath ./mysql-connector-java-5.1.27-bin.jar:./bin:./ svm.SVMMain --train $OUTPUT/train$1_$2.txt $TRAIN $OUTPUT/gameIDS$1_$2.dat
./liblinear-1.94/train -s $2 $OUTPUT/train$1_$2.txt $OUTPUT/model_$2_for_$1.model
java -classpath ./mysql-connector-java-5.1.27-bin.jar:./liblinear-1.94.jar:./bin:./ svm.SVMMain --test $OUTPUT/model_$2_for_$1.model $TEST $OUTPUT/gameIDS$1_$2.dat
