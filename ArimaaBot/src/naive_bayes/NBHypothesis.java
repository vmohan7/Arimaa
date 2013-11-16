package naive_bayes;

import static java.lang.Math.*;

import java.util.BitSet;
import utilities.AbstractHypothesis;

public class NBHypothesis extends AbstractHypothesis {
	
	private static final int LAPLACE_SMOOTHING = 1;
	private final double[][] logLikelihoodRatios;
	//private final long[][] frequencyCount;
	private double y0LogProb, y1LogProb;
	private double x0LogProb; //holds the precomputed value for a feature vector that is all zero
	
	public NBHypothesis(final long[][] frequencyCount, long numNegExamples, long numPosExamples){
		logLikelihoodRatios = new double[frequencyCount.length][frequencyCount[0].length];
		
		long y0 = numNegExamples;
		long y1 = numPosExamples;

		for (int j = 0; j < frequencyCount[0].length; j++){
			double phi0 = (double)(frequencyCount[0][j] + LAPLACE_SMOOTHING) / (y0 + 2 * LAPLACE_SMOOTHING);//intermediatePhis[0][j] / denom; //now phi = P (x_j = 1 | y = 0)
			double phi1 = (double)(frequencyCount[1][j] + LAPLACE_SMOOTHING) / (y1 + 2 * LAPLACE_SMOOTHING);// denom; //now phi = P (x_j = 1 | y = 1)
			
			logLikelihoodRatios[0][j] = log (1 - phi1) - log(1 - phi0); // log( P(x_j = 0|y = 1) / P(x_j = 0|y = 0) )
			logLikelihoodRatios[1][j] = log (phi1) - log(phi0); // log( P(x_j = 1|y = 1) / P(x_j = 1|y = 0) )
			
			x0LogProb += logLikelihoodRatios[0][j];
		}
		
		double probY1 = (double)(y1 + LAPLACE_SMOOTHING) / (y0 + y1 + 2 * LAPLACE_SMOOTHING);
		y0LogProb = log(1 - probY1);
		y1LogProb = log(probY1);
	}

	@Override
	public double evaluate(BitSet bs) {
		double weight = x0LogProb + y1LogProb - y0LogProb;

		//loops through x = 1
		for (int j = bs.nextSetBit(0); j != -1; j = bs.nextSetBit(j+1))
			weight += (logLikelihoodRatios[1][j] - logLikelihoodRatios[0][j]);
		
		return weight;
	}
	
}
