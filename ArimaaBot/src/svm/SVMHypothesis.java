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
		double[] yMargin = new double[2]; //for y = -1 and y = +1
		Linear.predictValues(model, SVMUtil.convertBitSet(bs), yMargin );
		return yMargin[0]; //should be the margin for y = +1
	}
	
}
