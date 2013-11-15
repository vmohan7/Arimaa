package naive_bayes;

import static java.lang.Math.*;

import java.util.BitSet;
import utilities.AbstractHypothesis;

public class NBHypothesis extends AbstractHypothesis {
	
	private final long[][] frequencyCount;
	private long y0, y1;
	
	public NBHypothesis(final long[][] frequencyCount){
		this.frequencyCount = frequencyCount;
		for (int i = 0; i< frequencyCount.length; i++){
			y0 += frequencyCount[0][i]; //get total number of counts for y = 0
			y1 += frequencyCount[1][i]; //get total number of counts for y = 0
		}
	}

	@Override
	public double evaluate(BitSet bs) {
		// Probability of bs = P (x | y = 0 )*P(y = 0) + P(x | y =1 )*P(y = 1)
		long totalSum = y0 + y1;
		double weightY0 = log(y0) - log(totalSum);
		double weightY1 = log(y1) - log(totalSum);

		for (int i = bs.nextSetBit(0); i != -1; i = bs.nextSetBit(i+1)){
			// P(xi = 1 | y = 0 or 1)
			double columnSum = frequencyCount[0][i] + frequencyCount[1][i] + 2;
			double probY0 = (frequencyCount[0][i] + 1) / columnSum;
			double probY1= (frequencyCount[1][i] + 1) / columnSum;
			
			weightY0 += log ( bs.get(i) ? probY0 : 1.0 - probY0 );	
			weightY1 += log ( bs.get(i) ? probY1 : 1.0 - probY1 );

		}
		
		return weightY0 + weightY1;
	}
	
}
