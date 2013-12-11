#!/usr/bin/python

# Script adapted from https://www.stanford.edu/group/farmshare/cgi-bin/wiki/index.php/FlacLikeABoss
import os
import datetime 

startJob = datetime.datetime.now() 

# Number of trials for each sample set size
numTrials = 1

# Submit job for each training set size
# The -N argument to qsub gives the job name
for (exampleSetSize, model) in [(250,1), (300,1), (350,1), (450,1), (500,1),\
							    (350,6),\
							    (450,7)]: 
    # without discarding, we used 4GB per 10 games --> 400MB / game
    # with 95% discarding, we are going to say 40MB / game
    memory = 2 + exampleSetSize * 40/1000; # 2GB base, then scale with set size
    #for model in [1,6,7]:
    for trialIndex in xrange(numTrials):
        os.system("qsub -l mem_free=%dG -N arimaa_svm_rand_hist_%d_%d_%d ~/cs229/Arimaa/ArimaaBot/arimaa_svm.sh %d %d" % \
			(memory, exampleSetSize, model, trialIndex, exampleSetSize, model) )

endJob = datetime.datetime.now() 

print 'time to submit all jobs: ', endJob-startJob
