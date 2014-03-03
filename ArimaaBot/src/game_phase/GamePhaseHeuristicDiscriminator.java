package game_phase;

import org.apache.commons.math3.distribution.NormalDistribution;

import arimaa3.GameState;

import utilities.GameData;
import utilities.GameParser;
import utilities.helper_classes.CSVDataFormatter;
import utilities.helper_classes.GameInfo;
import utilities.helper_classes.Utilities;

public class GamePhaseHeuristicDiscriminator {

	private static final int NUM_GAMES = 10;
	private static final String PATH_PREFIX = "../Plotting/game_phase/"; //optional
	private static final String FILE_NAME = PATH_PREFIX + "kmeans_heuristic_ternary.csv";
	private static final double[] WEIGHTS = {1.0, -0.5, -10.0};
	
	// Probability distributions: http://commons.apache.org/proper/commons-math/userguide/distribution.html
	// Normal distribution: http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/distribution/NormalDistribution.html
	// Find jar here: http://mvnrepository.com/artifact/org.apache.commons/commons-math3/3.0
	
	private static NormalDistribution BeginningDistr = new NormalDistribution(16.0, 2);
	private static NormalDistribution MiddleDistr = new NormalDistribution(12.0, 2);
	private static NormalDistribution EndDistr = new NormalDistribution(6.0, 2);
	
	public static void main(String args[]){
		Utilities.printInfo(String.format("<< Running heuristic game phase discrimination on %d games >>", NUM_GAMES));
		GameData myGameData = new GameData(NUM_GAMES, 0.0);
		Utilities.printInfo("Finished fetching game data");
		myGameData.setMode(GameData.Mode.TEST);		
		
		Utilities.printInfo("\nTesting and printing to CSV...");
		testAndOutputCSV(myGameData);
		
	}
	
	private static double dotProduct(double[] vector1, double[] vector2){
		double sum = 0.0;
		assert(vector1.length == vector2.length);
		
		for (int i = 0; i < vector1.length; i++){
			sum += (vector1[i] * vector2[i]);
		}
		
		return sum;
	}
	
	
	private static double[] assignClusterProbs(double[] features){
		// TODO: deal with end game in an entirely different way (i.e. with at dedicated set of features)
		// That is, build decision tree
		// TODO: tune probability distributions and weights
		
		double score = dotProduct(features, WEIGHTS);
		
		double beginningProb = BeginningDistr.density(score);
		double middleProb = MiddleDistr.density(score);
		double endProb = EndDistr.density(score);
		double totalProb = beginningProb + middleProb + endProb;
		
		return new double[]{beginningProb / totalProb, middleProb / totalProb, endProb / totalProb};
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
		return assignCluster(FeatureExtractor.extractReducedFeatures(gs));
	}
	
	/** Uses the test games to assign each GameState to a cluster. Results output to CSV. */
	private static void testAndOutputCSV(GameData myGameData) {
		CSVDataFormatter csv = new CSVDataFormatter();
		int count = 0;
		
		while (myGameData.hasNextGame()){
			final long startTime = System.currentTimeMillis();
			
			Utilities.printInfoInline("Testing on game # " + ++count + "..."); //time will be appended in-line
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
				GamePhase currPhase = assignCluster(FeatureExtractor.extractReducedFeatures( myParser.getNextGameState().getCurr()));
				csv.appendValue(Integer.toString(currPhase.getValue()));
			}
			
			csv.nextLine();
			
			final long endTime = System.currentTimeMillis();
			Utilities.printInfo("testing took " + Utilities.msToString(endTime - startTime));
		}
		
		csv.finalizeFile(FILE_NAME);
	}
	
}
