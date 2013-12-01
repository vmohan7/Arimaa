#!/usr/bin/python

import os
import passwords
# import time

## Variables to configure ##

# Username to log into corn.stanford.edu
USERNAME = passwords.my_username

# Password to log into corn.stanford.edu
PASSWORD = passwords.my_password

# Number of trials for each sample set size
NUM_TRIALS = 4

# Number of examples in first test
START_SIZE = 120

# Number of examples in last test (inclusive)
END_SIZE = 150

# Increment by which to increase number of examples in successive tests
INCR_SIZE = 10

# Keeps track of which corn machine to use
counter = 5


# There are 30 corn machines. This function returns a string corresponding 
# to the 'next' corn machine. e.g. '01'
def getCornMachine(a):
    a = a % 30 + 1
    if a < 10:
        return '0' + str(a)
    else: 
        return str(a)


for exampleSetSize in range(START_SIZE, END_SIZE + 1, INCR_SIZE):
    for trialIndex in range(NUM_TRIALS):
        os.system("/usr/bin/expect -d -f arimaa_nb_corn.exp %d %d %d %s '%s' '%s' >> NBcorn120-160.txt &" 
            % (exampleSetSize, exampleSetSize, 1, USERNAME, PASSWORD, getCornMachine(counter)))
        counter = counter + 1
        # time.sleep(10)

