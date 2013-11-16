package naive_bayes;

import java.util.concurrent.TimeUnit;

import utilities.GameData;
import utilities.HypothesisTest;
import utilities.helper_classes.Utilities;

public class NBMain {
	
	private static final int NUM_GAMES = 4;
	private static final double TRAIN_FRACTION = 0.7;

	public static void main(String[] args) {
		final long startTime = System.currentTimeMillis();
		
		System.out.println("Training and testing on " + NUM_GAMES + " games...");
		
		GameData myGameData = new GameData(NUM_GAMES, TRAIN_FRACTION);
		System.out.println("Finished fetching game data");
		
		NBTrain trainingModel = new NBTrain();
		System.out.println("Created the NB model");

		long[][] frequencyTable = trainingModel.train(myGameData);
		System.out.println("Just finished training!");
		
		System.out.println("About to evaluate model: creating a hypothesis...");
		NBHypothesis myHypothesis = new NBHypothesis( frequencyTable, 
				trainingModel.getNumNonExpertMoves(), trainingModel.getNumExpertMoves() );
		
		System.out.println("\nTesting hypothesis on test set...");
		myGameData.setMode(GameData.Mode.TEST);
		HypothesisTest.test(myHypothesis, myGameData);
		
		System.out.println("\nTesting hypothesis on train set...");
		myGameData.setMode(GameData.Mode.TRAIN);
		HypothesisTest.test(myHypothesis, myGameData);
		
		final long endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + Utilities.msToString(endTime - startTime));
	}

}
