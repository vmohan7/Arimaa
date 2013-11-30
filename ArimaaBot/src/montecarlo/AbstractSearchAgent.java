package montecarlo;

import utilities.helper_classes.ArimaaState;
import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.GameState;
import arimaa3.MoveList;

public abstract class AbstractSearchAgent extends AbstractAgent{

	protected ArimaaEngine engine;
	private int maxDepth;
	public AbstractSearchAgent(double[] weights, boolean training, int depth) {
		super(weights, training);
		engine = new ArimaaEngine();
		maxDepth = depth;
	}

	protected abstract double getGameOverScore();
	protected abstract double evaluation(ArimaaState state);
	protected abstract MoveList getMoves(ArimaaState state);
	
	public ArimaaMove selectMove(final ArimaaState arimaaState, MoveList moves){
		ArimaaMove bestMove = trainRandomly( moves );
		if (bestMove != null)
			return bestMove;
		
		double maxAlpha = Double.NEGATIVE_INFINITY;
		
		for(ArimaaMove move : moves){
			double a = AlphaBeta(new ArimaaState(arimaaState.getPrev(), arimaaState.getCurr(), move) , maxDepth, maxAlpha, Double.POSITIVE_INFINITY, false);
			if (maxAlpha < a){
				maxAlpha = a;
				bestMove = move;
			}
		}
		
		return bestMove;
	}
	
	private double AlphaBeta(ArimaaState state, int depth, double alpha, double beta, boolean myTurn){
		if (state.getCurr().isGameOver()){
			return getGameOverScore();
		} else if (depth == 0){
			return evaluation(state);
		}

		MoveList moves = getMoves(state);
		GameState next = new GameState();
		next.playFullClear(state.getNextMove(), state.getCurr());
		if (myTurn){
			for(ArimaaMove move: moves){
				alpha = Math.max(alpha, AlphaBeta(new ArimaaState(state.getCurr(), next, move) , depth - 1, alpha, beta, !myTurn) );
				if (beta <= alpha)
					break; // beta cut off
			}
			
			return alpha;
		} else {
			for(ArimaaMove move: moves){
				GameState gs = new GameState();
				gs.playFullClear(move, state.getCurr());
				beta = Math.min(alpha, AlphaBeta(new ArimaaState(state.getCurr(), next, move) , depth - 1, alpha, beta, !myTurn) );
				if (beta <= alpha)
					break; // alpha cut off
			}
			
			return beta;
		}
	}
}
