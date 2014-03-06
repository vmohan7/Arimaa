package game_phase;

import java.io.*;
import java.util.ArrayList;

import utilities.GameData;
import utilities.GameParser;
import utilities.helper_classes.GameInfo;
import utilities.helper_classes.Utilities;
import weka.clusterers.XMeans;
import weka.core.Attribute;
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
	
	private static final String FILE_PREFIX = "../Plotting/game_phase/";
	private static final String SERIALIZED_FILE = FILE_PREFIX + "XMeans.ser";
	
	private static final int MIN_NUM_CLUSTERS = 2;
	private static final int MAX_NUM_CLUSTERS = 8;
	
	private static final int NUM_GAMES = 2000;
	
	
	/** Run main to train and output the .ser file */
	public static void main(String[] unused) {
		XMeansWrapper xmw = new XMeansWrapper(MIN_NUM_CLUSTERS, MAX_NUM_CLUSTERS);
		xmw.train(); 
		xmw.serialize();
	}
	
	
	
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
	
	
	/** Deserializes an XMeansWrapper and returns it, or NULL on error. */
	public static XMeansWrapper getXMeansWrapper() {
		XMeansWrapper recoveredXMW = null;
		
		try {
			InputStream buffer = new BufferedInputStream(new FileInputStream(SERIALIZED_FILE));
			ObjectInput input = new ObjectInputStream(buffer);
			
			try {
				recoveredXMW = (XMeansWrapper)input.readObject();
			}
			finally {
				input.close();
			}
		}
		catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return recoveredXMW;
	}
	
	
	/**
	 * Once buildClusterer(*args*) has been called, this assigns the double array
	 * to the appropriate cluster.<br>
	 * Call this wrapper if you have a raw vector rather than an Instance object.
	 * @param vector The feature to be assigned to a cluster.
	 */
	public int clusterInstance(double[] vector) {
		Instance datapoint = new Instance(DEFAULT_WEIGHT, vector);
		int retVal = -1;
		
		try {
			retVal = clusterInstance(datapoint);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return retVal;
	}
	
	
	
	// ----- TESTING METHODS -----
	
	/**
	 * Do not call this method unless you are testing.
	 * @param designMatrix Array of features
	 * @throws Exception Passing on the exception from buildClusterer (ball's in your court)
	 */
	public void buildClustererTestOnly(double[][] designMatrix) throws Exception {
		buildClusterer(designMatrix);
	}
	
	/**
	 * Do not call this method unless you are testing.
	 * Automatically serializes to the same location that getXMeansWrapper() reads from.
	 */
	public void serializeTestOnly() {
		serialize();
	}
	
	
	
	// ----- PRIVATE METHODS -----

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
	private static final double DEFAULT_WEIGHT = 1.0;
	
	
	/** 
	 * Clusters the data in designMatrix. <br>
	 * 
	 * Call this method instead of the XMeans implementation to have some of
	 * the administrivia handled for you. (e.g. setting cluster ranges, creating an Instances object...).
	 * 
	 * @param designMatrix Each array is a coordinate (feature)
	 * @throws Exception Passing on the Exception from the superclass
	 */
	private void buildClusterer(double[][] designMatrix) throws Exception {
		String instancesTitle = "Game Phase Vectors";
		FastVector attributesVector = new FastVector();
		int capacity = designMatrix.length;
		
		
		// NOTE: Each Attribute is just a name for each coordinate of the feature vector
		// ...   Given more time, we could try to name them usefully in a programmatic way
		for (int i = 0; i < designMatrix[0].length; i++) {
			Attribute testAttr = new Attribute("test attribute " + i);
			attributesVector.addElement(testAttr);
		}

		
		// Initialize an empty Instances object with the header information from above
		Instances dataset = new Instances(instancesTitle, attributesVector, capacity);

		// Add each feature to the dataset
		for (int i = 0; i < designMatrix.length; i++){
			Instance datapoint = new Instance(DEFAULT_WEIGHT, designMatrix[i]);
			dataset.add(datapoint);
		}
		
		
		// Cluster using settings from the constructor
		this.setMinNumClusters(minNumClusters);
		this.setMaxNumClusters(maxNumClusters);
		super.buildClusterer(dataset);
	}
	
	
	
	
	
	
	/** Reads games from GameData, extracts features, and builds the clusters. */
	private void train() {
		Utilities.printInfo(String.format("<< Running with %d games, and %d to %d clusters >>", 
												NUM_GAMES, MIN_NUM_CLUSTERS, MAX_NUM_CLUSTERS));
		
		GameData myGameData = new GameData(NUM_GAMES, 0.99); // 1.0 doesn't work? janky
		Utilities.printInfo("Finished fetching game data");
		double[][] designMatrix = getFeatureMatrix(myGameData); 

		try {
			buildClusterer(designMatrix);
			Utilities.printInfo("Number of clusters: " + numberOfClusters());
		} catch (Exception e) {
			System.err.println("Something went wrong went clustering in XMeansWrapper.");
			e.printStackTrace();
			System.err.println("Exiting...");
			System.exit(1);
		}
		
		Utilities.printInfo("End training...");
	}


	/** Generates the 2D matrix of real-valued features (to be clustered) */
	private static double[][] getFeatureMatrix(GameData myGameData) {
		ArrayList<double[]> trainMatrix = new ArrayList<double[]>();
		
		int count = 0;
		while (myGameData.hasNextGame()){
			final long startTime = System.currentTimeMillis();
			
			Utilities.printInfoInline("Training on game # " + ++count + "..."); //time will be appended in-line
			GameInfo trainGameInfo = myGameData.getNextGame();
			GameParser myParser = new GameParser(trainGameInfo);
			
			while (myParser.hasNextGameState()){
				trainMatrix.add( game_phase.FeatureExtractor.extractFeatures(myParser.getNextGameState().getCurr()) );
			}
			
			final long endTime = System.currentTimeMillis();
			Utilities.printInfo("training took " + Utilities.msToString(endTime - startTime));
		}
		
		return trainMatrix.toArray(new double[0][0]);
	}

	
	/** Outputs the XMeansWrapper object to a specific file. */
	private void serialize() {
		assert(SERIALIZED_FILE.indexOf(".ser") != -1);
		
		try {
			OutputStream buffer = new BufferedOutputStream(new FileOutputStream(SERIALIZED_FILE));
			ObjectOutput output = new ObjectOutputStream(buffer);
			
			try {
				output.writeObject(this);
			}
			finally {
				output.close();
			}
		}
		catch (IOException ex) {
			System.err.println("Could not serialize object in XMeansWrapper. \nExiting...");
			ex.printStackTrace();
			System.exit(1);
		}
		
	}

	
	
	
	
	
	
			/** Look no further

    	   		   ___           |"|               ___        |             .      .       _   _              ___          |"|                         ___       #   ___              ...      
     '*`          .|||.         _|_|_             /_\ `*      |.===.      .  .:::.        '\\-//`            /_\ `*       _|_|_         ()_()         .|||.      #  <_*_>        o,*,(o o)     
    (o o)         (o o)         (o o)            (o o)        {}o o{}       :(o o):  .     (o o)            (o o)         (o o)         (o o)         (o o)      #  (o o)       8(o o)(_)Ooo   
ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--(_)--Ooo----ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--(_)--Ooo----ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--`o'--Ooo-ooO--(_)--Ooo--8---(_)--Ooo-ooO-(_)---Ooo----	
  
			   Look no further **/ 
	
}
