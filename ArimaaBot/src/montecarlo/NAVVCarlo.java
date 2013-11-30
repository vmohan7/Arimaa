package montecarlo;

import naive_bayes.NBHypothesis;
import utilities.helper_classes.ArimaaState;

public class NAVVCarlo extends AbstractNAVVSearch {

	public NAVVCarlo(double[] weights, boolean training, int depth,	NBHypothesis hyp) {
		super(weights, training, depth, hyp);
	}

	@Override
	protected double getGameOverScore() {
		return 1.0; //max value for the sigmoid function
	}

	@Override
	protected double evaluation(ArimaaState state) {
		return Utilities.logLinearEvaluation(state, state.getNextMove(), weights);
	}

}
