package svm;

import java.util.ArrayList;
import java.util.BitSet;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;

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
	
	public SVMTrain() {
		numExpertMoves = 0L;
		numNonExpertMoves = 0L;
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
	public Model train(GameData trainGames, Parameter parameter){
		
		ArimaaEngine myEngine = new ArimaaEngine(); // used to generate all possible moves
		
		// Iterate across all games in training set and extract features for expert and non-expert moves
		int count = 0;
		ArrayList< FeatureNode[] > trainVectors = new ArrayList<FeatureNode[]>();
		ArrayList< Integer > isExpert = new ArrayList< Integer >();
		while (trainGames.hasNextGame()){
			final long startTime = System.currentTimeMillis();
			
			System.out.print("Training on game # " + ++count + "..."); //time will be appended in-line
			GameInfo trainGameInfo = trainGames.getNextGame();
			GameParser myParser = new GameParser(trainGameInfo);
			
			while (myParser.hasNextGameState())	{
				trainOnTurn(trainVectors, isExpert, myParser.getNextGameState(), myEngine);	
			}
			
			final long endTime = System.currentTimeMillis();
			System.out.println("training took " + Utilities.msToString(endTime - startTime));
		}
		
		Problem problem = new Problem();
		problem.l = (int) ( numExpertMoves + numNonExpertMoves ); // number of training examples //WARNING: THIS MAY OVERFLOW
		problem.n = NUM_FEATURES; // number of features
		problem.x = trainVectors.toArray( new FeatureNode[1][] ); // feature nodes
		
		//TODO not efficient but no known way to know number of feature vectors before hand
		// so we will need to make the conversion
		double[] temp = new double[ problem.l ];
		for (int i = 0 ; i < problem.l; i++)
			temp[i] = isExpert.contains(i) ? 1.0 : 0.0;
		
		problem.y = temp; // target values
		
		return Linear.train(problem, parameter);
	}
	
	// This method is packaged so that it can be accessed in SVMTest only. 
	void trainOnTurn(ArrayList< FeatureNode[] > trainVectors, ArrayList< Integer > isExpert,
						ArimaaState myState, ArimaaEngine myEngine) {
		ArimaaMove expertMove = myState.getNextMove();
		
		// Extract features for the expert move
		FeatureExtractor myExtractor = new FeatureExtractor(myState.getCurr(), myState.getPrev());
		BitSet featureVector = myExtractor.extractFeatures(expertMove); // extract features from expert move
		trainVectors.add( SVMUtil.convertBitSet(featureVector) );
		isExpert.add( (int) (numExpertMoves +  numNonExpertMoves) );
		numExpertMoves++;
		
		// Extract features for all non-expert possible moves
		MoveList allPossibleMoves = myEngine.genRootMoves(myState.getCurr()); // upper limit of 400,000 possible moves
		// Note: for optimization, we should consider reducing this number from 400,000 to 40,000-50,000
		
		for (ArimaaMove possibleMove : allPossibleMoves){
			if (!possibleMove.equals(expertMove)){
				featureVector = myExtractor.extractFeatures(possibleMove); // extract features from non-expert move
				trainVectors.add( SVMUtil.convertBitSet(featureVector) );
				numNonExpertMoves++;
			}
		}
	}


}
