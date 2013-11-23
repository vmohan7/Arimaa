package svm;

import java.util.ArrayList;
import java.util.BitSet;

import de.bwaldvogel.liblinear.FeatureNode;

public class SVMUtil {
	/**
	 * Converts the bit set into an array of Feature Nodes which can be use in the SVM library
	 * @param BitSet bs 
	 */
	public static FeatureNode[] convertBitSet(BitSet bs){
		ArrayList< FeatureNode > list = new ArrayList< FeatureNode >();
		for (int i = bs.nextSetBit(0); i != -1; i = bs.nextSetBit(i+1))
		     list.add( new FeatureNode(i, 1) ); 
		
		return list.toArray( new FeatureNode[1] );
	}
}
