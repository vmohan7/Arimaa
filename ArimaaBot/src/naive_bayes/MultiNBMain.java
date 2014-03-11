package naive_bayes;

import utilities.GameData;
import utilities.HypothesisTest;
import utilities.MyDB;
import utilities.helper_classes.Utilities;

public class MultiNBMain {

	
	/* ================ VALUES TO CONFIGURE ================ */

	/** Used in trainAndTest if testing == false. */
	private static final double TRAIN_FRACTION = 0.7;
	/** Used in trainAndTest if testing == true. */
	private static final double ALMOST_ALL = 0.999999;
	
	/* If set to false, then log statements will be printed. If set to true, then only 
	 * results will be reported, in a csv-importable format. */
	public static final boolean PARSEABLE_OUTPUT = false;

	/* If set to true, then the percentile of each expert move evaluated will be printed
	 * in a csv-importable format. */
	public static final boolean PRINT_PERCENTILES = false;
	
	/* ===================================================== */
	
	private static MultiNBHypothesis trainAndTest(int numGames, boolean testing){
		Utilities.printInfo("-------------------------------");
		Utilities.printInfo("------START OF ROUND (" + (numGames) + ")------");
		Utilities.printInfo("-------------------------------\n");
		final long startTime = System.currentTimeMillis();
		
		double trainFraction = (testing ? TRAIN_FRACTION : ALMOST_ALL);
		Utilities.printInfo("Train fraction: " + trainFraction);
		Utilities.printInfo("Game data rating threshold <" +
							(GameData.USING_EXPERT ? "using" : "not using") +
							">: " + GameData.RATING_THRESHOLD);
		
		
		Utilities.printInfo("Training on " + (int)(trainFraction * numGames) + " games...");
		
		GameData myGameData = new GameData(numGames, trainFraction);
		Utilities.printInfo("Finished fetching game data");
		
		MultiNBTrain trainingModel = new MultiNBTrain();
		Utilities.printInfo("Created the MultiNB model");

		NBHypothesis[] nbParameters = trainingModel.train(myGameData);
		Utilities.printInfo("Just finished training!");
		
		Utilities.printInfo("Creating a hypothesis...");
		MultiNBHypothesis myHypothesis = new MultiNBHypothesis(nbParameters, (int)(numGames * trainFraction)); 

		
		if (testing) {
			Utilities.printInfoInline("\nTesting hypothesis on TEST set...");
			Utilities.printInfo(" (" + (int)((1 - trainFraction) * numGames) + " games...)");
			myGameData.setMode(GameData.Mode.TEST);
			HypothesisTest.test(myHypothesis, myGameData); 
			
			
			Utilities.printInfoInline("\nTesting hypothesis on TRAIN set...");
			Utilities.printInfo(" (" + (int)(trainFraction * numGames) + " games...)");
			myGameData.setMode(GameData.Mode.TRAIN);
			HypothesisTest.test(myHypothesis, myGameData);
		}

		
		final long endTime = System.currentTimeMillis();
		Utilities.printInfo("Round execution time: " + Utilities.msToString(endTime - startTime));
		Utilities.printInfo("\n------------------------");
		Utilities.printInfo("------END OF ROUND------");
		Utilities.printInfo("------------------------\n\n");
		
		myGameData.close();
		return myHypothesis;
	}

	
	private static final boolean TEST_TRAINING_SET_SIZES = false;
	private static final int NUM_GAMES = 151;
	
	private static final boolean DO_NOT_TEST = false, TEST = true;
	
	// Instructions to redirect console output to file:
	// right click MultiNBMain.java -> Run as -> Run Configurations... -> select "Common" tab
	// 			 				    -> check "File" under "Standard Input and Output"
	//      	                    -> enter destination file name :D
	/** 
	 * Outputs a MultiNBHypothesis object to a file for later deserialization and use.
	 * @param args Optional command line arguments (for testing)
	 */
	public static void main(String[] args) {
		
		if (TEST_TRAINING_SET_SIZES) {
			final boolean serializeAfterTest = true;
			testDifferentTrainingSetSizes(args, serializeAfterTest);
		}
		else {
			// The MultiNBHypothesis contains all of the trained parameters...
			MultiNBHypothesis mnbh = trainAndTest(NUM_GAMES, DO_NOT_TEST);
			mnbh.serialize();
		}
		
	}
	
	
	
	
	/** LEGACY CODE **/
	
	private static final boolean RUN_FROM_COMMAND_LINE = false;
	
	/* These values are used if RUN_FROM_COMMAND_LINE is false. They specify the size
	 * of the example set for the start round and the end round, and the amount by which
	 * to increment the size of the example set. E.g. if {START_SIZE, END_SIZE, INCREMENT} 
	 * == {10, 50, 10}, then the program will train on example sets of size 10, 20, ... 50. 
	 */
	private static final int START_SIZE = 150;
	private static final int END_SIZE = 150;
	private static final int INCREMENT = 10;
	
	private static void testDifferentTrainingSetSizes(String[] args) {
		testDifferentTrainingSetSizes(args, false);
	}
	
	private static void testDifferentTrainingSetSizes(String[] args, boolean serialize) {
		final long totalStartTime = System.currentTimeMillis();
		
		if (RUN_FROM_COMMAND_LINE && args.length != 3){
			System.out.println("Error in MultiNBMain: expects 3 arguments...\n"
					+ "Arguments: <min-example-set-size> <max-example-set-size> <increment> \n"
					+ "e.g. to train on example sets of size 10, 15, and 20, use arguments '10 20 5' \n"
					+ "e.g. to train on a single example set of 20 games, use arguments '20 20 1'");
			return;
		}
		
		Utilities.PARSEABLE_OUTPUT = MultiNBMain.PARSEABLE_OUTPUT;
		Utilities.PRINT_PERCENTILES = MultiNBMain.PRINT_PERCENTILES;
		
		int startSize = RUN_FROM_COMMAND_LINE ? Integer.parseInt(args[0]) : START_SIZE;
		int endSize = RUN_FROM_COMMAND_LINE ? Integer.parseInt(args[1]) : END_SIZE;
		int increment = RUN_FROM_COMMAND_LINE ? Integer.parseInt(args[2]) : INCREMENT;

		Utilities.printInfoInline(String.format("%n -- Serializing: %s -- %n", serialize ? "true" : "false"));
		MultiNBHypothesis mnbh = null;
		for (int x = startSize; x <= endSize; x += increment)
			mnbh = trainAndTest(x, TEST);
			
		if (serialize) 
			mnbh.serialize();
		
		MyDB.close();
		
		final long totalEndTime = System.currentTimeMillis();
		Utilities.printInfo("\n\nTotal execution time (from process running to termination): " 
					+ Utilities.msToString(totalEndTime - totalStartTime));
		
	}

}
