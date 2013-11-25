#!/usr/bin/python

# Script adapted from https://www.stanford.edu/group/farmshare/cgi-bin/wiki/index.php/FlacLikeABoss
import os
import datetime 

startJob = datetime.datetime.now() 

# Number of trials for each sample set size
numTrials = 1

# Submit job for each training set size
# The -N argument to qsub gives the job name
for exampleSetSize in range(10, 21, 10):
    for trialIndex in range(numTrials):
        os.system("qsub -N arimaa_nb_%d_%d ./arimaa_nb.sh %d %d %d" % (exampleSetSize, trialIndex, exampleSetSize, exampleSetSize, 1))

endJob = datetime.datetime.now() 

print 'time to submit all jobs: ', endJob-startJob
