package naive_bayes;

import utilities.GameData;
import utilities.HypothesisTest;
import utilities.MyDB;
import utilities.helper_classes.Utilities;

public class NBMain {
	
	/* ================ VALUES TO CONFIGURE ================ */
	
	private static final boolean RUN_FROM_COMMAND_LINE = false;
	
	/* These values are used if RUN_FROM_COMMAND_LINE is false. They specify the size
	 * of the example set for the start round and the end round, and the amount by which
	 * to increment the size of the example set. E.g. if {START_SIZE, END_SIZE, INCREMENT} 
	 * == {10, 50, 10}, then the program will train on example sets of size 10, 20, ... 50. 
	 */
	private static final int START_SIZE = 10;
	private static final int END_SIZE = 50;
	private static final int INCREMENT = 10;

	private static final double TRAIN_FRACTION = 0.7;
	
	/* If set to false, then log statements will be printed. If set to true, then only 
	 * results will be reported, in a csv-importable format. */
	public static final boolean PARSEABLE_OUTPUT = false;

	/* If set to true, then the percentile of each expert move evaluated will be printed
	 * in a csv-importable format. */
	public static final boolean PRINT_PERCENTILES = false;
	
	/* ===================================================== */
	
	private static void trainAndTest(int numGames){
		Utilities.printInfo("-------------------------------");
		Utilities.printInfo("------START OF ROUND (" + (numGames) + ")------");
		Utilities.printInfo("-------------------------------\n");
		final long startTime = System.currentTimeMillis();
		
		Utilities.printInfo("Training and testing on " + numGames + " games...");
		
		GameData myGameData = new GameData(numGames, TRAIN_FRACTION);
		Utilities.printInfo("Finished fetching game data");
		
		NBTrain trainingModel = new NBTrain();
		Utilities.printInfo("Created the NB model");

		long[][] frequencyTable = trainingModel.train(myGameData);
		Utilities.printInfo("Just finished training!");
		
		Utilities.printInfo("About to evaluate model: creating a hypothesis...");
		NBHypothesis myHypothesis = new NBHypothesis( frequencyTable, 
				trainingModel.getNumNonExpertMoves(), trainingModel.getNumExpertMoves() );
		
		Utilities.printInfo("\nTesting hypothesis on TEST set...");
		myGameData.setMode(GameData.Mode.TEST);
		HypothesisTest.test(myHypothesis, myGameData);
		
		Utilities.printInfo("\nTesting hypothesis on TRAIN set...");
		myGameData.setMode(GameData.Mode.TRAIN);
		HypothesisTest.test(myHypothesis, myGameData);
		
		final long endTime = System.currentTimeMillis();
		Utilities.printInfo("Round execution time: " + Utilities.msToString(endTime - startTime));
		Utilities.printInfo("\n------------------------");
		Utilities.printInfo("------END OF ROUND------");
		Utilities.printInfo("------------------------\n\n");
		
		myGameData.close();
	}

	// Instructions to redirect console output to file:
	// right click NBMain.java -> Run as -> Run Configurations... -> select "Common" tab
	// 						   -> check "File" under "Standard Input and Output"
	//                         -> enter destination file name :D
	
	public static void main(String[] args) {
		final long totalStartTime = System.currentTimeMillis();
		
		if (RUN_FROM_COMMAND_LINE && args.length != 3){
			System.out.println("Error in NBMain: expects 3 arguments...\n"
					+ "Arguments: <min-example-set-size> <max-example-set-size> <increment> \n"
					+ "e.g. to train on example sets of size 10, 15, and 20, use arguments '10 20 5' \n"
					+ "e.g. to train on a single example set of 20 games, use arguments '20 20 1'");
			return;
		}
		
		Utilities.PARSEABLE_OUTPUT = NBMain.PARSEABLE_OUTPUT;
		Utilities.PRINT_PERCENTILES = NBMain.PRINT_PERCENTILES;
		
		int startSize = RUN_FROM_COMMAND_LINE ? Integer.parseInt(args[0]) : START_SIZE;
		int endSize = RUN_FROM_COMMAND_LINE ? Integer.parseInt(args[1]) : END_SIZE;
		int increment = RUN_FROM_COMMAND_LINE ? Integer.parseInt(args[2]) : INCREMENT;

		for (int x = startSize; x <= endSize; x += increment) {
			trainAndTest(x);
		}
		
		MyDB.close();
		
		final long totalEndTime = System.currentTimeMillis();
		Utilities.printInfo("\n\nTotal execution time (from process running to termination): " 
					+ Utilities.msToString(totalEndTime - totalStartTime));
		
	}

}
