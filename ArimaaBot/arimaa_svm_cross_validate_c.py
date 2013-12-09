#!/usr/bin/python

# Script adapted from https://www.stanford.edu/group/farmshare/cgi-bin/wiki/index.php/FlacLikeABoss
import os
import datetime 

startJob = datetime.datetime.now() 

# Number of trials for each sample set size
numTrials = 3

# Submit job for each training set size
# The -N argument to qsub gives the job name
for exampleSetSize in xrange(10, 110, 10):
    memory = exampleSetSize*4/100;
    for model in [1]:
        for trialIndex in xrange(numTrials):
            os.system("qsub -l mem_free=%dG -N arimaa_crossv_svm_%d_%d_%d ~/Desktop/CS229/Arimaa/ArimaaBot/arimaa_cross_svm_c.sh %d %d %d" % \
			    (memory, exampleSetSize, model, trialIndex, exampleSetSize, model, trialIndex) )

endJob = datetime.datetime.now() 

print 'time to submit all jobs: ', endJob-startJob
