package montecarlo;

import naive_bayes.NBHypothesis;
import utilities.helper_classes.ArimaaState;
import arimaa3.ArimaaEvaluate2;
import arimaa3.ArimaaMove;
import arimaa3.Constants;
import arimaa3.MoveList;

/**
 * Combines Alpha-Beta search with Naive Bayes move ordering + pruning to X%
 * and uses Jeff Bacher's evaluation function.
 * @author vasanth
 *
 */
public class NAVVClueless extends MoveOrderingPruning implements Constants {

	private boolean firstMove;
	private ArimaaEvaluate2 eval;
	public NAVVClueless(double[] weights, boolean training, int depth, NBHypothesis hyp) {
		super(weights, training, depth, hyp);
		eval = new ArimaaEvaluate2();
		firstMove = true;
	}

	@Override
	protected double getGameOverScore() {
		return Constants.SCORE_MATE;
	}
	
	public ArimaaMove selectMove(final ArimaaState arimaaState, MoveList moves){
		if (firstMove){
			eval.PreProcessRootPosition(arimaaState.getCurr());
			firstMove = false;
		}
		return super.selectMove( arimaaState, getMoves(arimaaState) ); //does the limiting for the first set as well
	}

	@Override
	protected double evaluation(ArimaaState state) {
		return eval.Evaluate(state.getCurr(), false);
	}
}
