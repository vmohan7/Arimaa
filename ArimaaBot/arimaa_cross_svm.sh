#!/bin/bash

# tell grid engine to merge stdout and stderr streams
#$ -j y

# tell grid engine to use current directory
#$ -cwd

#arguments is 1= the number of games; 2 = the model
TRAIN=$(echo '.7 * ' $1 | bc | sed 's/[.].*//')
TEST=$(echo '.3 * ' $1 | bc | sed 's/[.].*//')
OUTPUT="/farmshare/user_data/vmohan7/CROSS_VAL_ALL"
DIRJ="/afs/ir.stanford.edu/users/v/m/vmohan7/CS229/Arimaa/ArimaaBot"
mkdir $OUTPUT
java -classpath $DIRJ/mysql-connector-java-5.1.27-bin.jar:$DIRJ/bin:$DIRJ/ svm.SVMMain --train $OUTPUT/train$1_$2.txt $TRAIN $OUTPUT/gameIDS$1_$2.dat

testingArgs = ""
for (( w=1; w<=10000; w=$i*10 ))
do
	outF="$OUTPUT/model_$2_for_$1_w$w.model"
	$DIRJ/liblinear-1.94/train -B 1 -s $2 -w-1 1 -w1 $w $OUTPUT/train$1_$2.txt $outF
	testingArgs = "$testingArgs $outF"
done

java -classpath $DIRJ/mysql-connector-java-5.1.27-bin.jar:$DIRJ/libsvm.jar:$DIRJ/liblinear-1.94.jar:$DIRJ/bin:$DIRJ/ svm.SVMCrossValidate $TEST $OUTPUT/gameIDS$1_$2.dat $testingArgs 
