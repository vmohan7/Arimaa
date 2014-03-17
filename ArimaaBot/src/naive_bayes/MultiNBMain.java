package naive_bayes;

import utilities.GameData;
import utilities.HypothesisTest;
import utilities.MyDB;
import utilities.helper_classes.Utilities;

public class MultiNBMain {

	private static final boolean DO_NOT_TEST = false, TEST = true;
	/** This value is not used. It is used for the English / readability. */
	private static final int IGNORED = -1;
	/** This value is not used. It is used for the English / readability. */
	private static final boolean IGNORED_B = false;
	
	
	/* ================ VALUES TO CONFIGURE ================ */

	/* Fractions of GameData games used for training the model. */
	/** Used in trainAndTest if testing == false. */
	private static final double TRAIN_FRACTION = 0.7;
	/** Used in trainAndTest if testing == true. */
	private static final double ALMOST_ALL = 0.999999;
	
	/** Rating of the games in the GameData. */
	private static final int RATING = 2100;
	
	/** 
	 * If set to false, then log statements will be printed. If set to true, then only 
	 * results will be reported, in a csv-importable format. 
	 */
	public static final boolean PARSEABLE_OUTPUT = true;

	/** 
	 * If set to true, then the percentile of each expert move evaluated will be printed
	 * in a csv-importable format. 
	 */
	public static final boolean PRINT_PERCENTILES = true;
	
	/** Run testing on a range of game-set sizes. */
	private static final boolean TEST_TRAINING_SET_SIZES = false;
	
	
	/* NOTE TO THE CONFIGURER: All of the ternary operators are to bring to the forefront
	 * the dependencies of constants. Simply modify the hard-coded value 
	 * (e.g. "true" or "START_SIZE = ... 10 [<-- change this] ..."). IGNORED and IGNORED_B
	 * are simply English to show that the constant value will not matter (if the ternary
	 * operator evaluates to return IGNORED*).
	 */
	
	/** Running from the command line only matters if testing. */
	private static final boolean RUN_FROM_COMMAND_LINE = TEST_TRAINING_SET_SIZES ? true : IGNORED_B;
	
	/* These values are used if RUN_FROM_COMMAND_LINE is false. They specify the size
	 * of the example set for the start round and the end round, and the amount by which
	 * to increment the size of the example set. E.g. if {START_SIZE, END_SIZE, INCREMENT} 
	 * == {10, 50, 10}, then the program will train on example sets of size 10, 20, ... 50. 
	 */
	/** Values to configure the set: { x = START_SIZE + k*INCREMENT | START_SIZE <= x <= END_SIZE, k natural } */
	private static final int START_SIZE = 	TEST_TRAINING_SET_SIZES ? (RUN_FROM_COMMAND_LINE ? IGNORED : 10) : IGNORED;
	private static final int END_SIZE = 	TEST_TRAINING_SET_SIZES ? (RUN_FROM_COMMAND_LINE ? IGNORED : 10) : IGNORED;
	private static final int INCREMENT = 	TEST_TRAINING_SET_SIZES ? (RUN_FROM_COMMAND_LINE ? IGNORED : 10) : IGNORED;
	
	
	/** Set to true if you want a serialized model with END_SIZE games. */
	private static final boolean SERIALIZE_AFTER_TEST = TEST_TRAINING_SET_SIZES ? true : IGNORED_B;
	
	/** 
	 * This variable is used when training only (and not testing). 
	 * ALMOST_ALL * NUM_GAMES games will be trained on. 
	 */
	private static final int NUM_GAMES = TEST_TRAINING_SET_SIZES ? IGNORED : 101;
	
	/* ===================================================== */
	
	
	/* Instructions to redirect console output to file:
	 * right click MultiNBMain.java -> Run as -> Run Configurations... -> select "Common" tab
	 * 		 				    	-> check "File" under "Standard Input and Output"
	 *								-> enter destination file name :D  
	 */
	/** 
	 * Main has two options: <br>
	 *  1. Trains and tests a MultiNBHypothesis model on different game-set sizes. <br>
	 *  2. Trains a MultiNBHypothesis model and outputs a serialized file.
	 * @param args Optional command line arguments (only potentially used with option 1: testing)
	 */
	public static void main(String[] args) {
		if (TEST_TRAINING_SET_SIZES) {
			testDifferentTrainingSetSizes(args, SERIALIZE_AFTER_TEST);
		}
		else {
			MultiNBHypothesis mnbh = trainAndTest(NUM_GAMES, DO_NOT_TEST);
			mnbh.serialize();
		}
	}
	
	
	// ===========================================================================
	
		
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
		
		Utilities.setParseableOutput(MultiNBMain.PARSEABLE_OUTPUT);
		Utilities.setPrintPercentiles(MultiNBMain.PRINT_PERCENTILES);
		
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
	
	
	// ===========================================================================
	
	
	private static MultiNBHypothesis trainAndTest(int numGames, boolean testing) {
		Utilities.printInfo("-------------------------------");
		Utilities.printInfo("------START OF ROUND (" + (numGames) + ")------");
		Utilities.printInfo("-------------------------------\n");
		final long startTime = System.currentTimeMillis();
		
		double trainFraction = (testing ? TRAIN_FRACTION : ALMOST_ALL);
		Utilities.printInfo("Train fraction: " + trainFraction);
		Utilities.printInfo("Game data rating threshold <" +
							(GameData.USING_EXPERT ? "using" : "not using") +
							">: " + RATING);
		
		
		Utilities.printInfo("Training on " + (int)(trainFraction * numGames) + " games...");
		GameData.setRatingThreshold(RATING);
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


}
