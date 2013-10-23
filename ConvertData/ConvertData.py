import glob
from itertools import chain

players = open("players.sql", "w")
games = open("games.sql", "w")
events = open("events.sql", "w")
sites = open("sites.sql", "w")
terminations = open("terminations.sql", "w")

W_ID = 1
W_USERNAME = 3
W_TITLE = 5
W_COUNTRY = 7
W_TYPE = 13

B_ID = W_ID+1
B_USERNAME = W_USERNAME+1
B_TITLE = W_TITLE+1
B_COUNTRY = W_COUNTRY+1
B_TYPE = W_TYPE+1

EVENT = 15
SITE = 16
TERMINATION = 22

player_ids = []
event_counter = 1
event_ids = {}
termination_counter = 1
termination_ids = {}
site_counter = 1
site_ids = {}

def insertSite(splitData):
	global site_counter
	site = splitData[SITE]
	if site not in site_ids:
		sites.write('INSERT INTO Arimaa.sites VALUES ({}, "{}");\n'.format(site_counter, site) )
		site_ids[ site ] = site_counter
		site_counter += 1
	
	return site_ids[ site ]

def insertTermination(splitData):
	global termination_counter
	termination = splitData[TERMINATION]
	if termination not in termination_ids:
		terminations.write('INSERT INTO Arimaa.terminations VALUES ({}, "{}");\n'.format(termination_counter, termination) )
		termination_ids[ termination ] = termination_counter
		termination_counter += 1
	
	return termination_ids[ termination ]

def insertEvent(splitData):
	global event_counter
	event = splitData[EVENT]
	if event not in event_ids:
		events.write('INSERT INTO Arimaa.events VALUES ({}, "{}");\n'.format(event_counter, event) )
		event_ids[ event ] = event_counter
		event_counter += 1
	
	return event_ids[event]
	
def insertPlayer(p_id, username, title, country, player_type):
	players.write('INSERT INTO Arimaa.players VALUES ({}, "{}", "{}", "{}", "{}");\n'.format(
			p_id, username, title, country, player_type
		) 
	)
	player_ids.append(p_id)

def insertPlayers(splitData):
	if splitData[W_ID] not in player_ids: #white player
		insertPlayer(splitData[W_ID], splitData[W_USERNAME], splitData[W_TITLE], splitData[W_COUNTRY], splitData[W_TYPE] )
	if splitData[B_ID] not in player_ids: #black player
		insertPlayer(splitData[B_ID], splitData[B_USERNAME], splitData[B_TITLE], splitData[B_COUNTRY], splitData[B_TYPE] )

NUM_GAME_COLS = 20
def createGameValuesStub():
	return ("INSERT INTO Arimaa.games VALUES (" + ', '.join(["{}"]*NUM_GAME_COLS) + ");\n")


W_RATING = 9
B_RATINGK = 12
TIME = 17
CORRUPT = 26
POSTAL = 18
RESULT = 21
MOVELIST = 27
EVENTS = 28

def insertGame(splitData, event_id, site_id, term_id):	
	moves = splitData[MOVELIST].split("\\n", 2) #split on literal \n to seperate start states from moves
	if (len(moves) != 3): return; #ignore any games that do not have the proper amount of moves
	datalist =  [
					[ splitData[0], splitData[W_ID], splitData[B_ID] ], 
					splitData[W_RATING: B_RATINGK+1], 
					[event_id, site_id, '"' + splitData[TIME] + '"'],
					splitData[POSTAL:RESULT],
					['"' + splitData[RESULT] + '"', term_id, splitData[CORRUPT] ],
					[ '"' + moves[0] + '"', '"' + moves[1] + '"', '"' + moves[2] + '"', '"' + splitData[EVENTS] + '"']
				]

	arglist = [data for l in datalist for data in l]

	games.write( createGameValuesStub().format( *arglist ) )

def createInsertData(gameData):
	splitData = gameData.split("\t")
	insertPlayers(splitData)	
	event_id = insertEvent(splitData)	
	site_id = insertSite(splitData)	
	term_id = insertTermination(splitData)	
	insertGame(splitData, event_id, site_id, term_id)	
	
for f in glob.iglob("TextData/allgames*"):
	print f
	gameData = open(f, "r")
	columns = gameData.readline() #get columns
	for line in gameData.readlines(): #get rest of games
		createInsertData(line)
	gameData.close()

players.close()
games.close()
events.close()
sites.close()
terminations.close()
