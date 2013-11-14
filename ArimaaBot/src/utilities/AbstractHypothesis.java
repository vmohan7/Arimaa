package utilities;

import java.util.BitSet;

public abstract class AbstractHypothesis {
	
	/** Returns numbers (consistent internally) corresponding to
	 * "expert-ness" of a BitSet. Higher numbers are more expert. */
	public abstract double evaluate(BitSet bs);
	
	
}
