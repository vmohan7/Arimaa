#!/usr/bin/python

# Script adapted from https://www.stanford.edu/group/farmshare/cgi-bin/wiki/index.php/FlacLikeABoss
import os
import datetime 

startJob = datetime.datetime.now() 

## Variables to configure ##

# Number of trials for each sample set size
NUM_TRIALS = 1

# Number of examples in first test
START_SIZE = 2

# Number of examples in last test (inclusive)
END_SIZE = 4

# Increment by which to increase number of examples in successive tests
INCR_SIZE = 1

# Submit job for each training set size
# The -N argument to qsub gives the job name
for exampleSetSize in range(START_SIZE, END_SIZE + 1, INCR_SIZE):
    for trialIndex in range(NUM_TRIALS):
        os.system("qsub -N arimaa_nb_%d_%d ./arimaa_nb.sh %d %d %d" % (exampleSetSize, trialIndex, exampleSetSize, exampleSetSize, 0))

endJob = datetime.datetime.now() 

print 'time to submit all jobs: ', endJob-startJob
