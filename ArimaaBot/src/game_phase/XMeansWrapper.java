package game_phase;

import weka.clusterers.XMeans;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/** 
 * This class extends XMeans.
 * The Weka 3.6 JAR required for XMeans can be found
 * <a href="http://www.cs.waikato.ac.nz/ml/weka/downloading.html">here</a>.
 * <i><br>The JAR itself can be found under the "<b>Other platforms (Linux, etc.)</b>" header.</i>
 * <br><br>
 * <b>NOTE:</b> We are using <b><i>Weka 3.6</i></b>--which as of writing this--is the "stable" version. <br>
 * Be careful that the documentation you are looking at is for <b>3.6</b>. As of writing this,
 * the URL contains "/doc.stable/" as opposed to "/doc.dev/". 
 * <br><br>
 * (You can extract just the JAR to the folder ".../ArimaaBot/weka-3-6-10".)
 */
public class XMeansWrapper extends XMeans {
	//TODO: Layout this class properly -- what abstractions? Is extending a good idea?

	/**
	 * The serialization runtime associates with each serializable class a version number,
	 * called a serialVersionUID, which is used during deserialization to verify that the 
	 * sender and receiver of a serialized object have loaded classes for that object that
	 * are compatible with respect to serialization. If the receiver has loaded a class for 
	 * the object that has a different serialVersionUID than that of the corresponding 
	 * sender's class, then deserialization will result in an InvalidClassException. 
	 * A serializable class can declare its own serialVersionUID explicitly by declaring a 
	 * field named "serialVersionUID" that must be static, final, and of type long.
	 * 
	 * Eclipse auto-generated this serialVersionUID.
	 */
	private static final long serialVersionUID = 3790499446630073185L;
	
	
	/** 
	 * A weight for each feature--Weka seems to support weighting different
	 * feature vectors differently...
	 */
	public static final double DEFAULT_WEIGHT = 1.0;
	
	private int minNumClusters;
	private int maxNumClusters;
	
	
	/**
	 * @param minClusters The minimum number of clusters allowed to be created.
	 * @param maxClusters The maximum number of clusters allowed to be created.
	 */
	public XMeansWrapper(int minClusters, int maxClusters) {
		minNumClusters = minClusters;
		maxNumClusters = maxClusters;
	}
	
	
	/** 
	 * Clusters the data in designMatrix. <br>
	 * 
	 * Call this method instead of the XMeans implementation to have some of
	 * the administrivia handled for you. (e.g. setting cluster ranges).
	 * 
	 * @param designMatrix Each array is a coordinate (feature)
	 * @throws Exception Passing on the Exception from the superclass
	 */
	public void buildClusterer(double[][] designMatrix) throws Exception {
		// Good example (for 3.7) http://stackoverflow.com/questions/12118132/adding-a-new-instance-in-weka
		// TODO: Add attributes to the vector
		String instancesTitle = "Game Phase Vectors";
		FastVector attributesVector = new FastVector();
		int capacity = designMatrix.length;
		// TODO: Get ^--- these from the user?

		
		// Build the data-set from the matrix 
		Instances dataset = new Instances(instancesTitle, attributesVector, capacity);

		for (int i = 0; i < designMatrix.length; i++){
			Instance datapoint = new Instance(DEFAULT_WEIGHT, designMatrix[i]);
			dataset.add(datapoint);
		}
		
		
		// Cluster using settings from the constructor
		this.setMinNumClusters(minNumClusters);
		this.setMaxNumClusters(maxNumClusters);
		super.buildClusterer(dataset);
	}
	
	
	/**
	 * Once buildClusterer(*args*) has been called, this assigns the double array
	 * to the appropriate cluster.<br>
	 * Call this wrapper if you have a raw vector rather than an Instance object.
	 * @param vector The feature to be assigned to a cluster.
	 * @throws Exception Passing on the Exception from the original method
	 */
	public int clusterInstance(double[] vector) throws Exception {
		Instance datapoint = new Instance(DEFAULT_WEIGHT, vector);
		return clusterInstance(datapoint);
	}
	
	
	
	
	
	
	
	
			/** Look no further

    	   		   ___           |"|               ___        |             .      .       _   _              ___          |"|                         ___       #   ___              ...      
     '*`          .|||.         _|_|_             /_\ `*      |.===.      .  .:::.        '\\-//`            /_\ `*       _|_|_         ()_()         .|||.      #  <_*_>        o,*,(o o)     
    (o o)         (o o)         (o o)            (o o)        {}o o{}       :(o o):  .     (o o)            (o o)         (o o)         (o o)         (o o)      #  (o o)       8(o o)(_)Ooo   
ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--(_)--Ooo----ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--(_)--Ooo----ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--`o'--Ooo-ooO--(_)--Ooo--8---(_)--Ooo-ooO-(_)---Ooo----	
  
			   Look no further **/
	
	
	public static void main(String[] args) {
		// Declare some dummy test values
		final int minNumClusters = 2, maxNumClusters = 4;
		double[][] designMatrix = { {0, 1, 2}, {1, 0, 2}, {0, -1, -2}, {-1, 0, -2} };
		
		XMeansWrapper xmw = new XMeansWrapper(minNumClusters, maxNumClusters);

		try {
			xmw.buildClusterer(designMatrix);
			System.out.println("Cluster result of first feature: " + xmw.clusterInstance(designMatrix[0]));
		} catch (Exception e) {
			System.err.println("Something went wrong went clustering in XMeansWrapper.");
			e.printStackTrace();
			System.err.println("Exiting...");
			System.exit(1);
		}
		
		System.out.println("Number of clusters: " + xmw.numberOfClusters());
	}
}
