package naive_bayes;

import static org.junit.Assert.*;

import org.junit.Test;

import weka.core.Instances;
import arimaa3.Constants;

public class MultiNBTest implements Constants {
	
	private static final double[][] designMatrix = { 
			{2, 1}, {2, 2}, {2, 3}, {1, 2}, {3, 2}, // quadrant 1
			{-2, 1}, {-2, 2}, {-2, 3}, {-1, 2}, {-3, 2}, // quadrant 2
			{-2, -1}, {-2, -2}, {-2, -3}, {-1, -2}, {-3, -2}, // quadrant 3
			{2, -1}, {2, -2}, {2, -3}, {1, -2}, {3, -2}, // quadrant 4
	};
	
	private static Instances clusters = null;
	private static int[] clusterAssignments = new int[designMatrix.length];
	
	
	@Test
	public void serializationTest() {
		trainAndWriteToFile();
		deserializeAndTest();
	}
	
	private void trainAndWriteToFile() {
		
	}
	
	private void deserializeAndTest() {
		
	}
	
}