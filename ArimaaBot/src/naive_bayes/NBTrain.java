package naive_bayes;

import java.util.BitSet;
import java.util.HashSet;

import feature_extractor.FeatureConstants;
import feature_extractor.FeatureExtractor;
import arimaa3.GameState;
import naive_bayes.helper_classes.ArimaaState;
import naive_bayes.helper_classes.GameInfo;

public class NBTrain {
	
	/**
	 * @param trainGames The same GameData is instantiated in NaiveBayes and passed onto NBTest after training
	 * @return Matrix (of dimension numFeatures x 2) representing frequencies of each feature in expert and 
	 * non-expert training examples. Everything is zero-indexed, and the 0th column is for non-expert moves. 
	 * E.g. frequencies[10][1] represents the frequency count of feature 10 in expert moves.
	 */
	public static long[][] train(GameData trainGames){
		GameInfo trainInfo;
		GameParser myParser;
		ArimaaState myState;
		FeatureExtractor myExtractor;
		BitSet featureVector;
		long[][] frequencyTable = new long[FeatureConstants.NUM_FEATURES][2];
		
		while (trainGames.hasNextGame()){
			trainInfo = trainGames.getNextGame();
			myParser = new GameParser(trainInfo);
			
			if (myParser.hasNextGameState()) // don't need to extract features for starting state
				myParser.getNextGameState();
			
			while (myParser.hasNextGameState()){
				myState = myParser.getNextGameState();
				myExtractor = new FeatureExtractor(myState.getPrev(), myState.getPrev_prev());
				featureVector = myExtractor.extractFeatures(myState.getMove(), myState.getCurr()); // extract features from expert move
				updateFrequencies(featureVector, frequencyTable, true);
			}
		}
		
		return null;
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
	/*
	 * 
	public double[][] frequencies train(GameData myGames)
		for each training game from GameData 
		    GameParser(game…);
		    while myGameParser.hasNextGameState()
		        Get states and move from myGameParser.getNextGameState()
		        expertVector = getFeatureVector(expertMove)
		        updateFreq(expertVector, y=1)
		        legalMoves = generateLegalMoves(prevState)
		        for each legalMove in legalMoves (excluding the expert move)
		            notExpertVector = getFeatureVector(legalMove)
		            updateFreq(notExpertVector, y=0)
	 * 
	 */

}
