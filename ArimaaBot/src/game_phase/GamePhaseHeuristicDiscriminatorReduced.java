package game_phase;

import arimaa3.GameState;

import utilities.GameData;
import utilities.GameParser;
import utilities.helper_classes.CSVDataFormatter;
import utilities.helper_classes.GameInfo;
import utilities.helper_classes.Utilities;

public class GamePhaseHeuristicDiscriminatorReduced extends GamePhaseHeuristicDiscriminator{

	private static final int NUM_GAMES = 10;
	private static final String PATH_PREFIX = "../Plotting/game_phase/"; //optional
	private static final String FILE_NAME = PATH_PREFIX + "kmeans_heuristic_reduced_ternary.csv";
	private static final double[] WEIGHTS = {1.0, -0.5};
	
	
	public static void main(String args[]){
		Utilities.printInfo(String.format("<< Running heuristic game phase discrimination without goal threats on %d games >>", NUM_GAMES));
		GameData myGameData = new GameData(NUM_GAMES, 0.0);
		Utilities.printInfo("Finished fetching game data");
		myGameData.setMode(GameData.Mode.TEST);		
		
		Utilities.printInfo("\nTesting and printing to CSV...");
		testAndOutputCSV(myGameData);
		
	}

	/**
	 * 
	 * @param features
	 * @return 0 for beginning, 1 for middle, 2 for end
	 */
	private static GamePhase assignCluster(double[] features){
		double score = dotProduct(features, WEIGHTS);

		if (score > 14)
			return GamePhase.BEGINNING;
		else if (score > 9)
			return GamePhase.MIDDLE;
		else
			return GamePhase.END;		
	}

	/**
	 * Returns the one game phase this state most belongs to.
	 * @param features
	 * @return 0 for beginning, 1 for middle, 2 for end
	 */
	public static GamePhase getStrictGamePhase(GameState gs) {
		return assignCluster(FeatureExtractor.extractFeatures(gs, GamePhaseFeatureType.REDUCED_NO_GOAL_THREATS));
	}
	
	/** Uses the test games to assign each GameState to a cluster. Results output to CSV. */
	private static void testAndOutputCSV(GameData myGameData) {
		CSVDataFormatter csv = new CSVDataFormatter();
		int count = 0;
		
		while (myGameData.hasNextGame()){
			final long startTime = System.currentTimeMillis();
			
			Utilities.printInfoInline("Testing (without goal threats) on game # " + ++count + "..."); //time will be appended in-line
			GameInfo trainGameInfo = myGameData.getNextGame();
			GameParser myParser = new GameParser(trainGameInfo);
			
			csv.appendValue(Integer.toString(trainGameInfo.getGameID()));
			while (myParser.hasNextGameState()){
//				double[] currProbabilities = assignClusterProbs(FeatureExtractor.extractReducedFeatures( myParser.getNextGameState().getCurr()));
//				
//				for (int i = 0; i < currProbabilities.length; i++){
//					csv.appendValue(Double.toString(currProbabilities[i]));
//				}
				
				// Assign ternary phases and print to csv for plotting
				GamePhase currPhase = assignCluster(FeatureExtractor.extractFeatures( myParser.getNextGameState().getCurr(), GamePhaseFeatureType.REDUCED_NO_GOAL_THREATS));
				csv.appendValue(Integer.toString(currPhase.getValue()));
			}
			
			csv.nextLine();
			
			final long endTime = System.currentTimeMillis();
			Utilities.printInfo("testing took " + Utilities.msToString(endTime - startTime));
		}
		
		csv.finalizeFile(FILE_NAME);
	}
	
}
