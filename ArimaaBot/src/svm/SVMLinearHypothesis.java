package svm;

import java.util.BitSet;

import arimaa3.GameState;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import utilities.AbstractHypothesis;

public class SVMLinearHypothesis extends AbstractHypothesis {
	
	private Model model;
	
	public SVMLinearHypothesis(Model model){
		this.model = model;
	}

	@Override
	public double evaluate(BitSet bs, GameState unused) {
		if (model.isProbabilityModel()) { //logistic regression
			double[] yProbs = new double[2]; //for y = +1 and y = -1
			Linear.predictProbability(model, SVMUtil.convertBitSet(bs), yProbs );
			return yProbs[0]; //should be probability y = +1
		} else {
			double[] yMargin = new double[2]; //for y = -1 and y = +1
			Linear.predictValues(model, SVMUtil.convertBitSet(bs), yMargin );
			return yMargin[0]; //should be the margin for y = +1
		}
	}
	
}
