#!/usr/bin/python

# Script adapted from https://www.stanford.edu/group/farmshare/cgi-bin/wiki/index.php/FlacLikeABoss
import os
import datetime 

startJob = datetime.datetime.now() 

# Number of trials for each sample set size
numTrials = 1

# Submit job for each training set size
# The -N argument to qsub gives the job name
for exampleSetSize in xrange(10, 120, 10):
    for model in [2,6,7]:
        for trialIndex in xrange(numTrials):
            os.system("qsub -N arimaa_svm_%d_%d_%d ./arimaa_svm.sh %d %d" % \
			    (exampleSetSize, model, trialIndex, exampleSetSize, model) )

endJob = datetime.datetime.now() 

print 'time to submit all jobs: ', endJob-startJob
