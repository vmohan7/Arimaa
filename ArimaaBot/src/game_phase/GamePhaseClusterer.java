package game_phase;

import java.util.ArrayList;
import java.util.Arrays;

import utilities.GameData;
import utilities.GameParser;
import utilities.helper_classes.CSVDataFormatter;
import utilities.helper_classes.GameInfo;
import utilities.helper_classes.Utilities;

public class GamePhaseClusterer {
	
	private static final int NUM_GAMES = 100;
	private static final int CLUSTERS = 3;
	private static final int ITERATIONS = 100;
	private static final String PATH_PREFIX = "../Plotting/"; //optional
	private static final String FILE_NAME = PATH_PREFIX + "kmeans_" + CLUSTERS + ".csv";
	
	public static void main(String args[]){
		Utilities.printInfo(String.format("<< Running with %d games, %d clusters, and %d iterations >>", NUM_GAMES, CLUSTERS, ITERATIONS));
		GameData myGameData = new GameData(NUM_GAMES, 0.75);
		Utilities.printInfo("Finished fetching game data");
		
		double[][] trainMatrix = train(myGameData);
		
		Utilities.printInfo("\nBeginning clustering...");
		KMeansWrapper kmeans = new KMeansWrapper(CLUSTERS, ITERATIONS, trainMatrix);
		kmeans.cluster();
		double[][] centers = kmeans.centroids();
		printCentroids(centers);
		
		myGameData.setMode(GameData.Mode.TEST);
		
		Utilities.printInfo("\nTesting and printing to CSV...");
		testAndOutputCSV(myGameData, kmeans);
	}

	/** Formatted print of centroids */
	private static void printCentroids(double[][] centers) {
		for(int j = 0; j < centers[0].length; j++){
			Utilities.printInfoInline(j + "\t");
			for(int i = 0; i < centers.length; i++){
				Utilities.printInfoInline(String.format("%.4f",centers[i][j]) + "\t" );
			}
			Utilities.printInfo("");
		}
	}


	/** Generates the 2D matrix of real-valued features (to be clustered) */
	private static double[][] train(GameData myGameData) {
		ArrayList<double[]> trainMatrix = new ArrayList<double[]>();
		
		int count = 0;
		while (myGameData.hasNextGame()){
			final long startTime = System.currentTimeMillis();
			
			Utilities.printInfoInline("Training on game # " + ++count + "..."); //time will be appended in-line
			GameInfo trainGameInfo = myGameData.getNextGame();
			GameParser myParser = new GameParser(trainGameInfo);
			
			while (myParser.hasNextGameState()){
				trainMatrix.add( FeatureExtractor.extractFeatures(myParser.getNextGameState().getCurr()) );
			}
			
			final long endTime = System.currentTimeMillis();
			Utilities.printInfo("training took " + Utilities.msToString(endTime - startTime));
		}
		
		return trainMatrix.toArray(new double[0][0]);
	}

	
	/** Uses the test games to assign each GameState to a cluster. Results output to CSV. */
	private static void testAndOutputCSV(GameData myGameData, KMeansWrapper kmeans) {
		CSVDataFormatter csv = new CSVDataFormatter();
		int count = 0;
		
		while (myGameData.hasNextGame()){
			final long startTime = System.currentTimeMillis();
			
			Utilities.printInfoInline("Testing on game # " + ++count + "..."); //time will be appended in-line
			GameInfo trainGameInfo = myGameData.getNextGame();
			GameParser myParser = new GameParser(trainGameInfo);
			
			while (myParser.hasNextGameState()){
				csv.appendValue(Integer.toString(
						kmeans.assignCluster(
								FeatureExtractor.extractFeatures( myParser.getNextGameState().getCurr() )
						)
				));
			}
			
			csv.nextLine();
			
			final long endTime = System.currentTimeMillis();
			Utilities.printInfo("testing took " + Utilities.msToString(endTime - startTime));
		}
		
		csv.finalizeFile(FILE_NAME);
	}
}
