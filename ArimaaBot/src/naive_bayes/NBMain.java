package naive_bayes;

import utilities.GameData;
import utilities.HypothesisTest;

public class NBMain {
	
	private static final int NUM_GAMES = 2;
	private static final double TRAIN_FRACTION = 0.5;

	public static void main(String[] args) {		
		System.out.println("I am in");
		GameData myGameData = new GameData(NUM_GAMES, TRAIN_FRACTION);
		System.out.println("Just got game data");
		NBTrain trainingModel = new NBTrain();
		System.out.println("Created my model");
		long[][] frequencyTable = trainingModel.train(myGameData);
		NBHypothesis myHypothesis = new NBHypothesis( frequencyTable, 
				trainingModel.getNumNonExpertMoves(), trainingModel.getNumExpertMoves() );
		HypothesisTest.test(myHypothesis, myGameData);
	}

}
