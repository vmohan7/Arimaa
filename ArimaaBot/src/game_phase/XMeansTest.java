package game_phase;

import static org.junit.Assert.*;

import org.junit.Test;

import weka.core.Instances;
import arimaa3.Constants;

public class XMeansTest implements Constants {
	
	private static final double[][] designMatrix = { 
			{2, 1}, {2, 2}, {2, 3}, {1, 2}, {3, 2}, // quadrant 1
			{-2, 1}, {-2, 2}, {-2, 3}, {-1, 2}, {-3, 2}, // quadrant 2
			{-2, -1}, {-2, -2}, {-2, -3}, {-1, -2}, {-3, -2}, // quadrant 3
			{2, -1}, {2, -2}, {2, -3}, {1, -2}, {3, -2}, // quadrant 4
	};
	
	private static Instances clusters = null;
	private static int[] clusterAssignments = new int[designMatrix.length];
	
	
	@Test
	public void serializationTest() throws Exception {
		trainAndWriteToFile();
		deserializeAndTest();
	}
	
	private void trainAndWriteToFile() throws Exception {
		final int minNumClusters = 4, maxNumClusters = 8;
		XMeansWrapper xmw = new XMeansWrapper(minNumClusters, maxNumClusters);

		xmw.buildClustererTestOnly(designMatrix);
		clusters = xmw.getClusterCenters();
		
		for (int feature = 0; feature < clusterAssignments.length; feature++)
			clusterAssignments[feature] = xmw.clusterInstance(designMatrix[feature]);
		
		xmw.serializeTestOnly();
	}
	
	private void deserializeAndTest() throws Exception {
		XMeansWrapper xmw = XMeansWrapper.getXMeansWrapper();
		assertTrue(xmw != null);
		
		for (int feature = 0; feature < clusterAssignments.length; feature++)
			assertEquals(clusterAssignments[feature], xmw.clusterInstance(designMatrix[feature]));
		
		Instances deserializedClusters = xmw.getClusterCenters();
		for (int c = 0; c < clusters.numInstances(); c++)
			// the string is just a string representation of the cluster centers (e.g. "-2, -2")
			assertTrue(  deserializedClusters.instance(c).toString().equals(clusters.instance(c).toString())  ); 
	}
	
}