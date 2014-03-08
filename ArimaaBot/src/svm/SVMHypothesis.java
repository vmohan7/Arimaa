package svm;

import java.util.BitSet;

import arimaa3.GameState;
import libsvm.svm;
import libsvm.svm_model;
import utilities.AbstractHypothesis;

public class SVMHypothesis extends AbstractHypothesis {
	
	private svm_model model;
	
	public SVMHypothesis(svm_model model){
		this.model = model;
	}

	@Override
	public double evaluate(BitSet bs, GameState unused) {
		double[] yProbs = new double[2]; //for y = -1 and y = +1
		svm.svm_predict_probability(model, SVMUtil.convertSVMBitSet(bs), yProbs );
		return yProbs[0]; //should be for y = +1
	}
	
}
