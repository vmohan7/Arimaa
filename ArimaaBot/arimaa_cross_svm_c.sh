#!/bin/bash

# tell grid engine to merge stdout and stderr streams
#$ -j y

# tell grid engine to use current directory
#$ -cwd

#arguments is 1= the number of games; 2 = the model; 3 = num trials
TRAIN=$(echo '.7 * ' $1 | bc | sed 's/[.].*//')
TEST=$(echo '.3 * ' $1 | bc | sed 's/[.].*//')
OUTPUT="/farmshare/user_data/arzavj/MINI_OUTPUT_C"
DIRJ="/afs/ir.stanford.edu/users/a/r/arzavj/Desktop/CS229/Arimaa/ArimaaBot"
mkdir $OUTPUT
java -classpath $DIRJ/mysql-connector-java-5.1.22-bin.jar:$DIRJ/bin:$DIRJ/ svm.SVMMain --train $OUTPUT/train$1_$2_$3.txt $TRAIN $OUTPUT/gameIDS$1_$2_$3.dat

testingArgs=""
for c in 0.1 1 10 100 1000
do
	outF="$OUTPUT/model_$2_for_$1_$3_c$c.model"
	$DIRJ/liblinear-1.94/train -B 1 -s $2 -c $c $OUTPUT/train$1_$2_$3.txt $outF
	testingArgs="$testingArgs $outF"
         echo $outF
done

java -classpath $DIRJ/mysql-connector-java-5.1.22-bin.jar:$DIRJ/libsvm.jar:$DIRJ/liblinear-1.94.jar:$DIRJ/bin:$DIRJ/ svm.SVMCrossValidate $TEST $OUTPUT/gameIDS$1_$2_$3.dat $testingArgs 
