package game_phase;

import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

public class KMeansWrapper {

	public KMeansWrapper(int numClusters, int iteration, double[][] features){
		
	}
	
	public static double[][][] cluster(int numClusters, int iteration, double[][] features){
		// Iterate across features and create DenseInstances
		// Put those in a DataSet
		// Call 'cluster'
		Dataset dataset = new DefaultDataset();
		for (int i = 0; i < features.length; i++){
			Instance datapoint = new DenseInstance(features[i]);
			dataset.add(datapoint);
		}
		KMeans kmeans = new KMeans(numClusters, iteration);
		Dataset[] clusters = kmeans.cluster(dataset);
		

		
		return null;
	}
}
