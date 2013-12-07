#!/usr/bin/python

# Script adapted from https://www.stanford.edu/group/farmshare/cgi-bin/wiki/index.php/FlacLikeABoss
import os
import datetime 

startJob = datetime.datetime.now() 

# Number of trials for each sample set size
numTrials = 1

# Submit job for each training set size
# The -N argument to qsub gives the job name
for exampleSetSize in xrange(10, 50, 10):
    memory = exampleSetSize/2;
    for trialIndex in xrange(numTrials):
      os.system("qsub -l mem_free=%dG -N arimaa_libsvm_%d_%d ~/CS229/Arimaa/ArimaaBot/arimaa_libsvm.sh %d" % \
			    (memory, exampleSetSize, trialIndex, exampleSetSize) )

endJob = datetime.datetime.now() 

print 'time to submit all jobs: ', endJob-startJob
