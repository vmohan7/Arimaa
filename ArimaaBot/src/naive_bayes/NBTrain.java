package naive_bayes;

import java.util.BitSet;
import utilities.GameData;
import utilities.GameParser;
import utilities.helper_classes.ArimaaState;
import utilities.helper_classes.GameInfo;
import feature_extractor.FeatureConstants;
import feature_extractor.FeatureExtractor;
import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.MoveList;


public class NBTrain {
	
	/**
	 * @param trainGames The same GameData is instantiated in NaiveBayes and passed onto NBTest after training
	 * @return Matrix (of dimension numFeatures x 2) representing frequencies of each feature in expert and 
	 * non-expert training examples. Everything is zero-indexed, and the 0th column is for non-expert moves. 
	 * E.g. frequencies[10][1] represents the frequency count of feature 10 in expert moves.
	 */
	public static long[][] train(GameData trainGames){
		
		long[][] frequencyTable = new long[FeatureConstants.NUM_FEATURES][2];
		ArimaaEngine myEngine = new ArimaaEngine(); // used to generate all possible moves
		
		// Iterate across all games in training set and extract features for expert and non-expert moves
		while (trainGames.hasNextGame()){
			GameInfo trainGameInfo = trainGames.getNextGame();
			GameParser myParser = new GameParser(trainGameInfo);
			
			while (myParser.hasNextGameState()){
				trainOnTurn(frequencyTable, myParser.getNextGameState(), myEngine);	
			}
		}
		
		return frequencyTable;
	}
	
	// This method is packaged so that it can be accessed in NBTest only. 
	static void trainOnTurn(long[][] frequencyTable, ArimaaState myState, ArimaaEngine myEngine) {
		ArimaaMove expertMove = myState.getNextMove();
		
		// Extract features for the expert move
		FeatureExtractor myExtractor = new FeatureExtractor(myState.getCurr(), myState.getPrev());
		BitSet featureVector = myExtractor.extractFeatures(expertMove); // extract features from expert move
		updateFrequencies(featureVector, frequencyTable, true);
		
		// Extract features for all non-expert possible moves
		MoveList allPossibleMoves = myEngine.genRootMoves(myState.getCurr()); // upper limit of 400,000 possible moves
		// Note: for optimization, we should consider reducing this number from 400,000 to 40,000-50,000
		
		for (ArimaaMove possibleMove : allPossibleMoves){
			if (!possibleMove.equals(expertMove)){
				featureVector = myExtractor.extractFeatures(possibleMove); // extract features from non-expert move
				updateFrequencies(featureVector, frequencyTable, false);
			}
		}
	}

	/**
	 * 
	 * @param featureVector 
	 * @param frequencyTable Frequency table to be updated with features in featureVector
	 * @param isExpertMove
	 */
	private static void updateFrequencies(BitSet featureVector, long[][] frequencyTable, boolean isExpertMove){
		// Iterate across all set bits in featureVector and increment the appropriate cell in frequencyTable
		// Warms the cockles of my heart
		for (int i = featureVector.nextSetBit(0); i != -1; i = featureVector.nextSetBit(i+1))
		     frequencyTable[i][(isExpertMove)?1:0]++; 
	}

}
