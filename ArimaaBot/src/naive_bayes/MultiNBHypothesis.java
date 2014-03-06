package naive_bayes;

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

public final class MultiNBHypothesis extends AbstractHypothesis 
									implements java.io.Serializable {

	/** Auto-generated for serialization verification. */
	private static final long serialVersionUID = -5200361623819629783L;
	
	private static final String FILE_PREFIX = "../Plotting/game_phase/";
	private static final String SERIALIZED_FILE = FILE_PREFIX + "MultiNBHypothesis.ser";
	
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

	/**
	 * @param bs The bitset to be evaluated
	 * @param state The GameState that was the source of the move from which bs was generated.
	 */
	@Override
	public double evaluate(BitSet bs, GameState state) {
		double[] phaseVector = game_phase.FeatureExtractor.extractFeatures(state);
		int cluster = xMeansWrapper.clusterInstance(phaseVector); 
		return nbHypotheses[cluster].evaluate(bs, state);
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
	
}
