package naive_bayes;

import utilities.GameData;
import utilities.HypothesisTest;
import utilities.helper_classes.Utilities;

public class NBMain {
	
	private static final int MAX_NUM_X_POINTS = 15;
	private static final int X_SCALING = 10;
	private static final double TRAIN_FRACTION = 0.7;

	public static void main(String[] args) {
		// please redirect console output to file by:
		// right click NBMain.java -> Run as -> Run Configurations... -> select "Common" tab
		// 						   -> check "File" under "Standard Input and Output"
		//                         -> enter destination file name :D
		
		for (int i = 1; i <= MAX_NUM_X_POINTS; i++) {
			int numGames = i * X_SCALING;
			System.out.println("------------------------------");
			System.out.println("------START OF ROUND (" + (numGames) + ")------");
			System.out.println("------------------------------\n");
			final long startTime = System.currentTimeMillis();
			
			System.out.println("Training and testing on " + numGames + " games...");
			
			GameData myGameData = new GameData(numGames, TRAIN_FRACTION);
			System.out.println("Finished fetching game data");
			
			NBTrain trainingModel = new NBTrain();
			System.out.println("Created the NB model");
	
			long[][] frequencyTable = trainingModel.train(myGameData);
			System.out.println("Just finished training!");
			
			System.out.println("About to evaluate model: creating a hypothesis...");
			NBHypothesis myHypothesis = new NBHypothesis( frequencyTable, 
					trainingModel.getNumNonExpertMoves(), trainingModel.getNumExpertMoves() );
			
			System.out.println("\nTesting hypothesis on TEST set...");
			myGameData.setMode(GameData.Mode.TEST);
			HypothesisTest.test(myHypothesis, myGameData);
			
			System.out.println("\nTesting hypothesis on TRAIN set...");
			myGameData.setMode(GameData.Mode.TRAIN);
			HypothesisTest.test(myHypothesis, myGameData);
			
			final long endTime = System.currentTimeMillis();
			System.out.println("Total execution time: " + Utilities.msToString(endTime - startTime));
			System.out.println("\n------------------------");
			System.out.println("------END OF ROUND------");
			System.out.println("------------------------\n\n");
		}
		

	}

}
