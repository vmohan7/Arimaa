package svm;

import java.util.BitSet;

import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import utilities.AbstractHypothesis;

public class SVMHypothesis extends AbstractHypothesis {
	
	private Model model;
	
	public SVMHypothesis(Model model){
		this.model = model;
		if (model.isProbabilityModel()) {
			System.out.println("Using a probability based model");
		} else{
			System.out.println("WARNING: Using a prediction based model. Will be less acurate. ");
		}
	}

	@Override
	public double evaluate(BitSet bs) {
		if (model.isProbabilityModel()) {
			double[] yProbs = new double[4];
			Linear.predictProbability(model, SVMUtil.convertBitSet(bs), yProbs );
			return yProbs[1]; //should be probability y = +1
		} else {
			return Linear.predict(model, SVMUtil.convertBitSet(bs));
		}
	}
	
}
