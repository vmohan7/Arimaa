package montecarlo;

import naive_bayes.NBHypothesis;
import utilities.helper_classes.ArimaaState;

/**
 * Combines Alpha-Beta search with Naive Bayes move ordering + pruning to X%
 * and uses the evaluation function learned from MCS.
 * @author vasanth
 *
 */
public class NAVVCarlo extends MoveOrderingPruning {

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
