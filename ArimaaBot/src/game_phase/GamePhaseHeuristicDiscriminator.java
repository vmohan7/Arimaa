package game_phase;

import org.apache.commons.math3.distribution.NormalDistribution;

import utilities.GameData;
import utilities.GameParser;
import utilities.helper_classes.CSVDataFormatter;
import utilities.helper_classes.GameInfo;
import utilities.helper_classes.Utilities;

public class GamePhaseHeuristicDiscriminator {

	private static final int NUM_GAMES = 10;
	private static final String PATH_PREFIX = "../Plotting/game_phase/"; //optional
	private static final String FILE_NAME = PATH_PREFIX + "kmeans_heuristic_probabilities.csv";
	private static final double[] WEIGHTS = {1.0, -0.5};
	
	// Probability distributions: http://commons.apache.org/proper/commons-math/userguide/distribution.html
	// Normal distribution: http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/distribution/NormalDistribution.html
	// Find jar here: http://mvnrepository.com/artifact/org.apache.commons/commons-math3/3.0
	
	private static NormalDistribution BeginningDistr = new NormalDistribution(16.0, 2);
	private static NormalDistribution MiddleDistr = new NormalDistribution(12.0, 2);
	private static NormalDistribution EndDistr = new NormalDistribution(9.0, 2);
	
	
	public enum GamePhase {
        BEGINNING(0), MIDDLE(1), END(2);
        private int value;

        private GamePhase(int value) {
                this.value = value;
        }
        public int getValue() {
            return value;
        }
	}; 
	
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
	
	
	private static double[] assignCluster(double[] features){
		// TODO: deal with end game in an entirely different way (i.e. with at dedicated set of features)
		// That is, build decision tree
		// TODO: tune probability distributions and weights
		
		double score = dotProduct(features, WEIGHTS);
		
		double beginningProb = BeginningDistr.density(score);
		double middleProb = MiddleDistr.density(score);
		double endProb = EndDistr.density(score);
		double totalProb = beginningProb + middleProb + endProb;
		
		return new double[]{beginningProb / totalProb, middleProb / totalProb, endProb / totalProb};
		
//		if (score > 14)
//			return new double[]{1.0, 0.0, 0.0};
//		else if (score > 11)
//			return new double[]{0.0, 1.0, 0.0};
//		else
//			return new double[]{0.0, 0.0, 1.0};
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
				double[] currProbabilities = assignCluster(FeatureExtractor.extractReducedFeatures( myParser.getNextGameState().getCurr()));
				
				for (int i = 0; i < currProbabilities.length; i++){
					csv.appendValue(Double.toString(currProbabilities[i]));
				}

			}
			
			csv.nextLine();
			
			final long endTime = System.currentTimeMillis();
			Utilities.printInfo("testing took " + Utilities.msToString(endTime - startTime));
		}
		
		csv.finalizeFile(FILE_NAME);
	}
	
}
