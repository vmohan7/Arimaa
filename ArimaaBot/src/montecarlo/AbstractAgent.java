package montecarlo;

import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import utilities.helper_classes.ArimaaState;

public abstract class AbstractAgent {
	
	protected byte[] weights;
	
	/**
	 * @param arimaaState - We assume that the nextMove field is null and use that to test out moves
	 * @return The best move that the computer can think of.
	 */
	public abstract ArimaaMove selectMove(ArimaaState arimaaState, ArimaaEngine engine);
	
	/**
	 * 
	 * @param w - Sets the weights that are used for the evaluation functions
	 */
	public void setWeights(byte[] w){
		weights = w;
	}
}
