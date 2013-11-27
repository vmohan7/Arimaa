package feature_extractor;

import java.util.BitSet;


/** The abstract superclass of all extractors that will be used to add to the
 *  feature BitSet, for use by the FeatureExtactor class. The FeatureExtractor
 *  class itself will not extend this class.
 *  <p> Implements FeatureConstants
 *  <br> Supports updating a BitSet */
public abstract class AbstractExtractor implements FeatureConstants {
	
	/** Updates the BitSet -- should be declared static when implemented. Only
	 *  a portion of the entire BitSet will be set by a particular extractor class. 
	 *  @param bitset The features BitSet to be updated by this static method. */
	public abstract void updateBitSet(BitSet featureVector);
	
	/** Returns the <b>first</b> index at which the BitSet will be updated. This should be set
	 * by the class' constructor, depending on the class. */
	public abstract int startOfRange();
	
	/** Returns the <b>last</b> index at which the BitSet will be updated. This should be set
	 * by the class' constructor, depending on the class. */
	public abstract int endOfRange();
	
}
