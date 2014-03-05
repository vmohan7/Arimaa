import subprocess
import os

numGames = 1000
for i in xrange(numGames):
	if (i % 2 == 0):
		os.chdir("FairyGold")
		subprocess.call(['python', 'roundrobin.py'])
	else:
		os.chdir("FairySilver")
		subprocess.call(['python', 'roundrobin.py'])

	os.chdir("..")
