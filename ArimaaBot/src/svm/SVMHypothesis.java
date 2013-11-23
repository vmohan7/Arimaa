package svm;

import java.util.BitSet;

import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import utilities.AbstractHypothesis;

public class SVMHypothesis extends AbstractHypothesis {
	
	private Model model;
	
	public SVMHypothesis(Model model){
		this.model = model;
	}

	@Override
	public double evaluate(BitSet bs) {
		double[] yProbs = new double[2];
		Linear.predictProbability(model, SVMUtil.convertBitSet(bs), yProbs );
		return yProbs[1];
	}
	
}
