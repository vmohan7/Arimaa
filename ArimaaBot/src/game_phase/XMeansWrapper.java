package game_phase;

import java.util.ArrayList;
import java.util.Arrays;

import weka.clusterers.XMeans;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Attribute;

/** 
 * This class extends XMeans.
 * The Weka JAR required for XMeans can be found
 * <a href="http://www.cs.waikato.ac.nz/ml/weka/downloading.html">here</a>.
 * <i><br>The JAR itself can be found under the "<b>Other platforms (Linux, etc.)</b>" header.</i>
 */
public class XMeansWrapper extends XMeans {

	/**
	 * TODO: What is this auto-generated code?
	 */
	private static final long serialVersionUID = 3790499446630073185L;
	// END TODO
	
	public static void main(String[] args) {
		double[][] designMatrix = { {0, 1, 2}, {1, 0, 2}, {0, -1, -2}, {-1, 0, -2} };
		XMeansWrapper xmw = new XMeansWrapper(3, 5, designMatrix);
		
		//TODO: Change this...
		final String NAME = "What?";
		final FastVector ATT_INFO = new FastVector();
		final int CAPACITY = designMatrix.length;
		
		//TODO http://stackoverflow.com/questions/12118132/adding-a-new-instance-in-weka
		//TODO Documentation seems inconsistent...
		Instances dataset = new Instances(NAME, ATT_INFO, CAPACITY);
		for (int i = 0; i < designMatrix.length; i++){
			Instance datapoint = null; // Documentation seems inconsistent...
			dataset.add(datapoint);
		}
		
		xmw.buildClusterer(dataset);
		//TODO SO SAD :(
	}
	
	
	
	//TODO: What is the layout of this class?
	
	private int minNumClusters;
	private int maxNumClusters;
	private double[][] designMatrix;
	
	/**
	 * @param minClusters The minimum number of clusters allowed to be created.
	 * @param maxClusters The maximum number of clusters allowed to be created.
	 * @param features Each array is a coordinate (feature)
	 */
	public XMeansWrapper(int minClusters, int maxClusters, double[][] features) {
		minNumClusters = minClusters;
		maxNumClusters = maxClusters;
		designMatrix = doubleArrayCopy(features); //don't trust the client--ever
	}
	
	
	/** Returns a deep copy of the double[][] arr */
	private double[][] doubleArrayCopy(double[][] arr) {
		if (arr == null) return null;
		
		double[][] copy = new double[arr.length][];
		for (int i = 0; i < copy.length; i++) {
			if (arr[i] == null) {
				copy[i] = null;
				continue;
			}
			copy[i] = Arrays.copyOf(arr[i], arr[i].length);
		}
		
		return copy;
	}
}
