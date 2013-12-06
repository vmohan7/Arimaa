package svm;

import java.util.ArrayList;
import java.util.BitSet;

import libsvm.svm_node;

import de.bwaldvogel.liblinear.FeatureNode;

public class SVMUtil {
	/**
	 * Converts the bit set into a sparse array of Feature Nodes which can be used by the LibLinear library
	 * @param BitSet bs 
	 */
	public static FeatureNode[] convertBitSet(BitSet bs){
		ArrayList< FeatureNode > list = new ArrayList< FeatureNode >();
		for (int i = bs.nextSetBit(0); i != -1; i = bs.nextSetBit(i+1))
		     list.add( new FeatureNode(i + 1, 1) ); // index is one based
		
		return list.toArray( new FeatureNode[1] );
	}
	
	/**
	 * Converts the bit set into a sparse array of svm_node which can be use by the LibSVM library
	 * @param BitSet bs 
	 */
	public static svm_node[] convertSVMBitSet(BitSet bs){
		ArrayList< svm_node > list = new ArrayList< svm_node >();
		for (int i = bs.nextSetBit(0); i != -1; i = bs.nextSetBit(i+1)) { 
			svm_node node = new svm_node();
			node.index = i+1; //1 indexed
			node.value = 1;
		    list.add( node ); // index is one based
		}
		
		return list.toArray( new svm_node[1] );
	}
}
