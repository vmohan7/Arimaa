package montecarlo;

import java.util.Random;

import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import utilities.helper_classes.ArimaaState;

public abstract class AbstractAgent {
	
	protected boolean training;
	protected double[] weights;
	
	public static final double RANDOM_CHOICE = 0.20;
	
	public AbstractAgent(double[] weights, boolean training){
		setWeights(weights);
		this.training = training; 
	}
	
	/**
	 * @param arimaaState - We assume that the nextMove field is null and use that to test out moves
	 * @return The best move that the computer can think of.
	 */
	public abstract ArimaaMove selectMove(final ArimaaState arimaaState, ArimaaEngine engine);
	
	/**
	 * 
	 * @param w - Sets the weights that are used for the evaluation functions
	 */
	public void setWeights(double[] w){
		weights = w;
	}
	
	protected ArimaaMove trainRandomly(ArimaaMove[] movelist){
		if (training){
			Random r = new Random();
			if( r.nextDouble() < RANDOM_CHOICE ){ 
				return movelist[ r.nextInt( movelist.length ) ];
			}
		}
		
		return null;
	}
}
