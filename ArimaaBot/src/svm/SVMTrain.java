package svm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;

import utilities.GameData;
import utilities.GameParser;
import utilities.helper_classes.ArimaaState;
import utilities.helper_classes.GameInfo;
import utilities.helper_classes.Utilities;
import feature_extractor.FeatureConstants;
import feature_extractor.FeatureExtractor;
import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.MoveList;


public class SVMTrain implements FeatureConstants{
	
	private long numExpertMoves;
	private long numNonExpertMoves;
	private BufferedWriter writer;
	
	public SVMTrain(File file) {
		numExpertMoves = 0L;
		numNonExpertMoves = 0L;
		
		try {
			writer = new BufferedWriter( new FileWriter(file.getAbsoluteFile()) );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public long getNumExpertMoves() {
		return numExpertMoves;
	}

	public long getNumNonExpertMoves() {
		return numNonExpertMoves;
	}

	/**
	 * @param trainGames The same GameData is instantiated in NaiveBayes and passed onto NBTest after training
	 * @return Matrix (of dimension 2 x numFeatures) representing frequencies of each feature in expert and 
	 * non-expert training examples. Everything is zero-indexed, and the 0th row is for non-expert moves. 
	 * E.g. frequencies[1][10] represents the frequency count of feature 10 in expert moves.
	 */
	public void train(GameData trainGames){
		
		ArimaaEngine myEngine = new ArimaaEngine(); // used to generate all possible moves
		
		// Iterate across all games in training set and extract features for expert and non-expert moves
		int count = 0;
		while (trainGames.hasNextGame()){
			final long startTime = System.currentTimeMillis();
			
			System.out.print("Training on game # " + ++count + "..."); //time will be appended in-line
			GameInfo trainGameInfo = trainGames.getNextGame();
			GameParser myParser = new GameParser(trainGameInfo);
			
			while (myParser.hasNextGameState())	{
				try {
					trainOnTurn(myParser.getNextGameState(), myEngine);
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
			
			final long endTime = System.currentTimeMillis();
			System.out.println("training took " + Utilities.msToString(endTime - startTime));
		}
	}
	
	// This method is packaged so that it can be accessed in SVMTest only. 
	void trainOnTurn(ArimaaState myState, ArimaaEngine myEngine) throws IOException {
		ArimaaMove expertMove = myState.getNextMove();
		
		// Extract features for the expert move
		FeatureExtractor myExtractor = new FeatureExtractor(myState.getCurr(), myState.getPrev());
		BitSet featureVector = myExtractor.extractFeatures(expertMove); // extract features from expert move
		outputToFile(featureVector, true);
		numExpertMoves++;
		
		// Extract features for all non-expert possible moves
		MoveList allPossibleMoves = myEngine.genRootMoves(myState.getCurr()); // upper limit of 400,000 possible moves
		// Note: for optimization, we should consider reducing this number from 400,000 to 40,000-50,000
		
		for (ArimaaMove possibleMove : allPossibleMoves){
			if (!possibleMove.equals(expertMove)){
				featureVector = myExtractor.extractFeatures(possibleMove); // extract features from non-expert move
				outputToFile(featureVector, false);
				numNonExpertMoves++;
			}
		}
		
		writer.flush();
	}
	
	/**
	 * 
	 * @param featureVector 
	 * @param frequencyTable Frequency table to be updated with features in featureVector
	 * @param isExpertMove
	 * @throws IOException 
	 */
	private void outputToFile(BitSet featureVector, boolean isExpertMove) throws IOException{
		// Iterate across all set bits in featureVector and increment the appropriate cell in frequencyTable
		// Warms the cockles of my heart
		String sparseVector = isExpertMove ? "+1" : "-1"; 
		for (int i = featureVector.nextSetBit(0); i != -1; i = featureVector.nextSetBit(i+1))
		     sparseVector += (" " + i+1 + ":1"); //index is 1 based
		
		writer.write(sparseVector + "\n");
	}


}
