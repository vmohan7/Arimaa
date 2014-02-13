package montecarlo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.BitSet;
import java.util.Scanner;

import naive_bayes.NBHypothesis;

import utilities.helper_classes.ArimaaState;

import arimaa3.ArimaaMove;
import arimaa3.GameState;

import feature_extractor.FeatureConstants;
import feature_extractor.FeatureExtractor;

public class Utilities {
	
	public static final int ADDITIONAL_FEATURES = 0;
	public static final int TOTAL_FEATURES = FeatureConstants.NUM_FEATURES + ADDITIONAL_FEATURES;
	/**
	 * 
	 * @param state - The current state of the board
	 * @param move - The move we want a feature for
	 * @return Returns a byte[] that captures many binary features and in the future can capture values between 0 - 256
	 */
	public static byte[] getFeatures(final ArimaaState state, final ArimaaMove move){
		byte[] features = new byte[TOTAL_FEATURES];
		GameState currState = new GameState();
		currState.playFullClear(move, state.getCurr());
		
		FeatureExtractor fe = new FeatureExtractor( state.getCurr(), state.getPrev(), state.getPrevPrev(),
								state.getPrevMove(), state.getPrevPrevMove() );
		BitSet bs = fe.extractFeatures(move, currState);
		for (int i = bs.nextSetBit(0); i != -1; i = bs.nextSetBit(i+1))
			features[i] = 1;
		
		//If we want to add any features based on the currState, we can do so here
		
		return features;
	}

	/**
	 * @param move - The move that we want to evaluate
	 * @param weights - The weights used by logisitic regression
	 * @return Our evaluation score based on the sigmoid function
	 */
	public static double logLinearEvaluation(final ArimaaState state, final ArimaaMove move, final double[] weights){
		byte[] features = getFeatures(state, move);
		double z = 0;
		for(int i = 0; i < features.length; i++)
			z += features[i] * weights[i];
		
		return 1.0/(1.0 + Math.exp(-z));
	}
	
	/**
	 * Performs a Temporal Update on the weights
	 * @param state - The previous state
	 * @param nextState - The next state
	 * @param reward - The reward that the training harness will feed us
	 * @param weights - The weights that we will update by reference
	 * @param eta - The step size that the training harness feeds us
	 */
	
	public static void TDUpdate(final ArimaaState state, final ArimaaState nextState, int reward, double eta, double[] weights){
	    double r = reward + (nextState == null ? 0 : logLinearEvaluation(nextState, nextState.getNextMove() , weights) ) 
	    				- logLinearEvaluation(state, state.getNextMove() , weights);
	    byte[] features = getFeatures(state, state.getNextMove());
		long z = 0;
		for(int i = 0; i < features.length; i++)
			z+= features[i] * weights[i];
		
		for(int i = 0; i < features.length; i++){
			double expWeight = Math.exp(z);
			double wi = ( expWeight )/( (expWeight + 1) * (expWeight + 1) );
			weights[i] += features[i]*r*eta*wi; //update weights
		}

	}
	
	private static final int YCOUNT = 2;
	public static NBHypothesis getNBPredictor(String freqTable){
		try {
			Scanner reader = new Scanner( (new File(freqTable)).getAbsoluteFile() );
			long numNeg = reader.nextLong();
			long numPos = reader.nextLong();
			reader.nextLine(); //read the end of the line
			
			long[][] fCounts = new long[YCOUNT][];
			for(int i = 0; i < YCOUNT; i++){ //for a valid file, it needs 2 lines
				String [] counts = reader.nextLine().split(" ");
				fCounts[i] = new long[counts.length];
				for(int j = 0; j < counts.length; j++){
					fCounts[i][j] = Long.parseLong(counts[j]); 
				}
			}
			reader.close();
			
			return new NBHypothesis(fCounts, numNeg, numPos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1); //cannot continue
		}
		
		return null; //cannot get here
	}
	
}
