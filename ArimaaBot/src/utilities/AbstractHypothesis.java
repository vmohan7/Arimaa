package utilities;

import java.util.BitSet;
import arimaa3.GameState;

public abstract class AbstractHypothesis {
	
	/** 
	 * Returns numbers (consistent internally) corresponding to
	 * "expert-ness" of a BitSet. Higher numbers are more expert. 
	 * Some subclasses can safely ignore "GameState state." 
	 */
	public abstract double evaluate(BitSet bs, GameState state);
	
	
}
