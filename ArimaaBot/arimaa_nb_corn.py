#!/usr/bin/python

import os

## Variables to configure ##

# Username to log into corn.stanford.edu
USERNAME = "my_username"

# Password to log into corn.stanford.edu
PASSWORD = "my_password"

# Number of trials for each sample set size
NUM_TRIALS = 1

# Number of examples in first test
START_SIZE = 2

# Number of examples in last test (inclusive)
END_SIZE = 4

# Increment by which to increase number of examples in successive tests
INCR_SIZE = 1

for exampleSetSize in range(START_SIZE, END_SIZE, INCR_SIZE):
    for trialIndex in range(NUM_TRIALS):
        os.system("/usr/bin/expect arimaa_nb_corn.exp %d %d %d %s %s >> cornNB%d_%d.txt &" 
            % (exampleSetSize, exampleSetSize, 1, USERNAME, PASSWORD, exampleSetSize, trialIndex))
