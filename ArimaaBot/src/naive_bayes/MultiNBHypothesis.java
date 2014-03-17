package naive_bayes;

import game_phase.GamePhaseFeatureType;
import game_phase.XMeansWrapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.BitSet;

import arimaa3.GameState;
import utilities.AbstractHypothesis;
import utilities.helper_classes.Utilities;

public final class MultiNBHypothesis extends AbstractHypothesis 
									implements java.io.Serializable {

	/** Auto-generated for serialization verification. */
	private static final long serialVersionUID = -5200361623819629783L;
	
	private static final String FILE_PREFIX = "../Plotting/game_phase/";
	private static final String SERIALIZED_FILE = FILE_PREFIX + "MultiNBHypothesis.ser";
	
	transient private XMeansWrapper xMeansWrapper;
	private NBHypothesis[] nbHypotheses;
	
	
	/** For printing info. */
	private int numGamesTrained;
	public int getNumXMeansGames() { return xMeansWrapper.getNumGames(); } // number of games XMeans trained on
	public int getNumTrainedGames() { return numGamesTrained; } // number of games MultiNBHypothesis trained on
	
	/** 
	 * Keeps an internal pointer to the parameter.
	 * Do not modify nbParameters after passing in to this constructor.
	 */
	public MultiNBHypothesis(NBHypothesis[] nbParameters, int numGamesTrainedOn) {
		nbHypotheses = nbParameters;
		xMeansWrapper = XMeansWrapper.getXMeansWrapper();
		numGamesTrained = numGamesTrainedOn;
	}

	
	/** For logging info. */
	private double clusterTimeInMS = 0.0;
	private double evaluateTimeInMS = 0.0;
	private double extractTimeInMS = 0.0;
	
	public double getExtractTimeInMS() { return extractTimeInMS; }
	public double getClusterTimeInMS() { return clusterTimeInMS; }
	public double getEvaluateTimeInMS() { return evaluateTimeInMS; }
	
	public void printPostRunStats() {
		Utilities.printInfo(String.format("%n--MultiNBHypothesis post run stats:"));
		Utilities.printInfo(String.format("Time extracting Game Phase features (ms): %,.0f" , getExtractTimeInMS()));
		Utilities.printInfo(String.format("Time assigning Game Phase feature clusters (ms): %,.0f" , getClusterTimeInMS()));
		Utilities.printInfo(String.format("Time in NB (not multi!) evaluate (ms): %,.0f" , getEvaluateTimeInMS()));
		Utilities.printInfo(String.format("--%n"));
	}
	
	/**
	 * @param bs The bitset to be evaluated
	 * @param state The GameState that was the source of the move from which bs was generated.
	 */
	@Override
	public double evaluate(BitSet bs, GameState state) {
		double extractStart = System.nanoTime() / 1E6;
			double[] phaseVector = game_phase.FeatureExtractor.extractFeatures(state, XMeansWrapper.EXTRACTION_TYPE);
		extractTimeInMS += System.nanoTime() / 1E6 - extractStart;
			
		double clusterStart = System.nanoTime() / 1E6;
			int cluster = xMeansWrapper.clusterInstance(phaseVector);
		clusterTimeInMS += System.nanoTime() / 1E6 - clusterStart;
		
		double evaluateStart = System.nanoTime() / 1E6;
			double evalResult = nbHypotheses[cluster].evaluate(bs, state);
		evaluateTimeInMS += System.nanoTime() / 1E6 - evaluateStart;
		
		return evalResult;
	}
	
	
	/** Deserializes an MultiNBHypothesis and returns it, or NULL on error. */
	public static MultiNBHypothesis getMultiNBHypothesis() {
		MultiNBHypothesis recoveredMNBH = null;
		
		try {
			InputStream buffer = new BufferedInputStream(new FileInputStream(SERIALIZED_FILE));
			ObjectInput input = new ObjectInputStream(buffer);
			
			try {
				recoveredMNBH = (MultiNBHypothesis)input.readObject();
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
		
		recoveredMNBH.xMeansWrapper = XMeansWrapper.getXMeansWrapper();
		return recoveredMNBH;
	}
	
	
	/** 
	 * Outputs the MultiNBHypothesis object to SERIALIZED_FILE
	 * (from where it is also read in the getter above). 
	 */
	public void serialize() {
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
			System.err.println("Could not serialize object in MultiNBHypothesis. \nExiting...");
			ex.printStackTrace();
			System.exit(1);
		}
		
	}
	
	/**
	 * Testing purposes.
	 * @return
	 */
	public boolean validateState() {
		if (xMeansWrapper.numberOfClusters() != nbHypotheses.length)
			return false;
		return true;
	}
	
}
