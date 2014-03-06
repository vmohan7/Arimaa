package naive_bayes;

import game_phase.XMeansWrapper;

import java.util.BitSet;

import arimaa3.GameState;
import utilities.AbstractHypothesis;

public class MultiNBHypothesis extends AbstractHypothesis {

	private XMeansWrapper xMeansWrapper;
	private NBHypothesis[] nbHypotheses;
	
	/** 
	 * Keeps an internal pointer to the parameter.
	 * Do not modify nbParameters after passing in to this constructor.
	 */
	public MultiNBHypothesis(NBHypothesis[] nbParameters) {
		nbHypotheses = nbParameters;
		xMeansWrapper = XMeansWrapper.getXMeansWrapper();
	}

	public double evaluate(BitSet bs, GameState state) {
		// finish
		return 0.0;
	}
	
}
