#!/bin/bash

# tell grid engine to merge stdout and stderr streams
#$ -j y

# mail to this address
#$ -M vmohan7@stanford.edu
# send mail on beginning, ending, or suspension of job
#$ -m bes

# tell grid engine to use current directory
#$ -cwd

cd ..
python loop_games.py
