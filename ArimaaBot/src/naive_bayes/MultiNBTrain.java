package naive_bayes;

import java.util.BitSet;

import utilities.AbstractGameData;
import utilities.GameParser;
import utilities.helper_classes.ArimaaState;
import utilities.helper_classes.GameInfo;
import utilities.helper_classes.Utilities;
import feature_extractor.FeatureConstants;
import feature_extractor.FeatureExtractor;
import game_phase.XMeansWrapper;
import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.GameState;
import arimaa3.MoveList;


public class MultiNBTrain {
	
	private XMeansWrapper xMeansWrapper;
	
	private long[] numExpertMoves;
	private long[] numNonExpertMoves;
	
	public MultiNBTrain() {
		xMeansWrapper = XMeansWrapper.getXMeansWrapper();
		numExpertMoves = new long[xMeansWrapper.numberOfClusters()];
		numNonExpertMoves = new long[xMeansWrapper.numberOfClusters()];
	}
	
	
	public long[] getNumExpertMoves() {
		return numExpertMoves;
	}


	public long[] getNumNonExpertMoves() {
		return numNonExpertMoves;
	}


	/**
	 * Trains multiple Naive Bayes models from trainGames--one model for each XMeans cluster.
	 * Returns the models (usually [~1/1 times] passed directly into MultiNBHypothesis).
	 */
	public NBHypothesis[] train(AbstractGameData trainGames){
		
		long[][][] frequencyTable = new long[xMeansWrapper.numberOfClusters()][2][FeatureConstants.NUM_FEATURES];
		ArimaaEngine myEngine = new ArimaaEngine(); // used to generate all possible moves
		
		// Iterate across all games in training set and extract features for expert and non-expert moves
		int count = 0;
		while (trainGames.hasNextGame()){
			final long startTime = System.currentTimeMillis();
			
			Utilities.printInfoInline("Training on game # " + ++count + "..."); //time will be appended in-line
			GameInfo trainGameInfo = trainGames.getNextGame();
			GameParser myParser = new GameParser(trainGameInfo);
			
			while (myParser.hasNextGameState()){
				trainOnTurn(frequencyTable, myParser.getNextGameState(), myEngine);	
			}
			
			final long endTime = System.currentTimeMillis();
			Utilities.printInfo("training took " + Utilities.msToString(endTime - startTime));
		}
		
		NBHypothesis[] hypotheses = new NBHypothesis[xMeansWrapper.numberOfClusters()];
		for (int h = 0; h < hypotheses.length; h++)
			hypotheses[h] = new NBHypothesis(frequencyTable[h], numNonExpertMoves[h], numExpertMoves[h]);
		
		return hypotheses;
	}
	
	
	// This method is packaged so that it can be accessed in NBTest only. 
	void trainOnTurn(long[][][] frequencyTables, ArimaaState myState, ArimaaEngine myEngine) {
		ArimaaMove expertMove = myState.getNextMove();
		
		int whichTable = getTableIndexFromState(myState.getCurr());
		
		// Extract features for the expert move
		FeatureExtractor myExtractor = new feature_extractor.FeatureExtractor(myState.getCurr(), myState.getPrev(), myState.getPrevPrev(), myState.getPrevMove(), myState.getPrevPrevMove());
		BitSet featureVector = myExtractor.extractFeatures(expertMove); // extract features from expert move
		updateFrequencies(featureVector, frequencyTables[whichTable], true);
		numExpertMoves[whichTable]++;
		
		// Extract features for all non-expert possible moves
		// TODO: Use the new MoveList?
		MoveList allPossibleMoves = myEngine.genRootMoves(myState.getCurr());
		
		for (ArimaaMove possibleMove : allPossibleMoves){
			if (!possibleMove.equals(expertMove)){
				featureVector = myExtractor.extractFeatures(possibleMove); // extract features from non-expert move
				updateFrequencies(featureVector, frequencyTables[whichTable], false);
				numNonExpertMoves[whichTable]++;
			}
		}
	}

	/** 
	 * Uses the XMeansWrapper to return the appropriate cluster index.
	 * This index corresponds to the global table's index. 
	 * @throws Exception Thrown if some issue occurs in assigning clusters
	 */
	private int getTableIndexFromState(GameState curr) {
		double[] features = game_phase.FeatureExtractor.extractFeatures(curr, XMeansWrapper.EXTRACTION_TYPE);
		return xMeansWrapper.clusterInstance(features);
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
		     frequencyTable[(isExpertMove)?1:0][i]++; 
	}

}
