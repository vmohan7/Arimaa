package naive_bayes;

import static org.junit.Assert.*;
import game_phase.XMeansWrapper;

import org.junit.Test;

import arimaa3.Constants;

public class MultiNBTest implements Constants {
	

	@Test
	public void serializationTest() {
		XMeansWrapper.main(null); // <-- can comment this line out if XMeans.ser is updated locally
		trainAndWriteToFile();
		deserializeAndTest();
	}
	
	private void trainAndWriteToFile() {
		MultiNBMain.main(null);
	}
	
	private void deserializeAndTest() {
		MultiNBHypothesis nb = MultiNBHypothesis.getMultiNBHypothesis();
		assertTrue(nb.validateState());
	}
	
}