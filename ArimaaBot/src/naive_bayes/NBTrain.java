package naive_bayes;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;

import utilities.GameData;
import utilities.GameParser;
import utilities.helper_classes.ArimaaState;
import utilities.helper_classes.GameInfo;
import feature_extractor.FeatureConstants;
import feature_extractor.FeatureExtractor;
import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.GameState;
import arimaa3.GenTurn;
import arimaa3.MoveList;


public class NBTrain {
	
	/**
	 * @param trainGames The same GameData is instantiated in NaiveBayes and passed onto NBTest after training
	 * @return Matrix (of dimension numFeatures x 2) representing frequencies of each feature in expert and 
	 * non-expert training examples. Everything is zero-indexed, and the 0th column is for non-expert moves. 
	 * E.g. frequencies[10][1] represents the frequency count of feature 10 in expert moves.
	 */
	public static long[][] train(GameData trainGames){
		GameInfo trainGameInfo;
		GameParser myParser;
		ArimaaState myState;
		FeatureExtractor myExtractor;
		BitSet featureVector;
		long[][] frequencyTable = new long[FeatureConstants.NUM_FEATURES][2];
		ArimaaEngine myEngine = new ArimaaEngine();
		MoveList allPossibleMoves;
		ArimaaMove expertMove;
		
		// Iterate across all games in training set and extract features for expert and non-expert moves
		while (trainGames.hasNextGame()){
			trainGameInfo = trainGames.getNextGame();
			myParser = new GameParser(trainGameInfo);
			
			while (myParser.hasNextGameState()){
				trainOnGame(frequencyTable, myParser.getNextGameState());
				myState = myParser.getNextGameState();
				expertMove = myState.getMove();
				
				// Extract features for the expert move
				myExtractor = new FeatureExtractor(myState.getCurr(), myState.getPrev());
				featureVector = myExtractor.extractFeatures(expertMove); // extract features from expert move
				updateFrequencies(featureVector, frequencyTable, true);
				
				// Extract features for all non-expert possible moves
				allPossibleMoves = myEngine.genRootMoves(myState.getCurr()); // upper limit of 400,000 possible moves
				
				for (ArimaaMove possibleMove : allPossibleMoves){
					if (!possibleMove.equals(expertMove)){
						featureVector = myExtractor.extractFeatures(possibleMove); // extract features from non-expert move
						updateFrequencies(featureVector, frequencyTable, false);
					}
				}
			}
		}
		
		// Take another look at genAllTurns in GenTurn.java
		
		return null;
	}
	
	private static void trainOnGame(long[][] frequencyTable,
			ArimaaState nextGameState) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 * @param featureVector 
	 * @param frequencyTable Frequency table to be updated with features in featureVector
	 * @param isExpertMove
	 */
	private static void updateFrequencies(BitSet featureVector, long[][] frequencyTable, boolean isExpertMove){
		for (int i = featureVector.nextSetBit(0); i >= 0; i = featureVector.nextSetBit(i+1)) {
		     frequencyTable[i][(isExpertMove)?1:0]++;
		 }
	}

}
