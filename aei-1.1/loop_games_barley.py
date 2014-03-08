#!/usr/bin/python

# Script adapted from https://www.stanford.edu/group/farmshare/cgi-bin/wiki/index.php/FlacLikeABoss
import os
import datetime

startJob = datetime.datetime.now()

# Number of jobs to submit to barley. 
# MAKE SURE TO look at loop_games.py to
# see how many games are run on each job!!
NUM_TRIALS = 30

# Submit jobs
# The -N argument to qsub gives the job name
for trialNumber in range(0, NUM_TRIALS + 1):
    os.system("qsub -N one-ply-v-two-ply_trial%d ./barley.sh" % (trialNumber))

endJob = datetime.datetime.now()

print 'time to submit all jobs: ', endJob-startJob
