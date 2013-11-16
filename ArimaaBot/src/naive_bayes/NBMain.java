package naive_bayes;

import utilities.GameData;
import utilities.HypothesisTest;

public class NBMain {
	
	private static final int NUM_GAMES = 100;
	private static final double TRAIN_FRACTION = 0.7;

	public static void main(String[] args) {		
		GameData myGameData = new GameData(NUM_GAMES, TRAIN_FRACTION);
		NBTrain trainingModel = new NBTrain();
		long[][] frequencyTable = trainingModel.train(myGameData);
		NBHypothesis myHypothesis = new NBHypothesis( frequencyTable, 
				trainingModel.getNumNonExpertMoves(), trainingModel.getNumExpertMoves() );
		HypothesisTest.test(myHypothesis, myGameData);
	}

}
