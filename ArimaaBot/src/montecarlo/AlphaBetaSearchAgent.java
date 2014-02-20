package montecarlo;

import utilities.helper_classes.ArimaaState;
import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.GameState;
import arimaa3.MoveList;

public abstract class AlphaBetaSearchAgent extends AbstractAgent{

	protected ArimaaEngine engine;
	protected int maxDepth;
	public AlphaBetaSearchAgent(double[] weights, boolean training, int depth) {
		super(weights, training);
		engine = new ArimaaEngine();
		maxDepth = depth;
	}

	/**
	 * Should be evaluated from the current player's (from the game state) perspective
	 * If the current player wins, then it returns a positive score
	 * Otherwise, it should return a negative score
	 * @return
	 */
	protected abstract double getGameOverScore();
	protected abstract double evaluation(ArimaaState state);
	protected abstract MoveList getMoves(ArimaaState state);
	
	//use ArimaaState such that the next move is filled in by this function
	//we assume that the next move is null
	public ArimaaMove selectMove(final ArimaaState arimaaState, MoveList moves){
		ArimaaMove bestMove = trainRandomly( moves );
		if (bestMove != null)
			return bestMove;
		
		double maxAlpha = Double.NEGATIVE_INFINITY;
		
		for(ArimaaMove move : moves){
			//new ArimaaState(state.getPrev(), state.getCurr(), next, state.getPrevMove(), move, null);
			double a = AlphaBeta(new ArimaaState(arimaaState.getPrevPrev(), arimaaState.getPrev(), arimaaState.getCurr(), 
							arimaaState.getPrevPrevMove(), arimaaState.getPrevMove(), move) , maxDepth - 1, maxAlpha, Double.POSITIVE_INFINITY, false);
			if (maxAlpha < a){
				maxAlpha = a;
				bestMove = move;
			}
		}
		
		return bestMove;
	}
	
	private double AlphaBeta(final ArimaaState state, int depth, double alpha, double beta, boolean isMaxPlayer){
		GameState next = new GameState();
		next.playFullClear(state.getNextMove(), state.getCurr());
		
		int sign = (maxDepth - depth) % 2 == 0 ? 1 : -1;
		
		ArimaaState nextState = new ArimaaState(state.getPrev(), state.getCurr(), next, 
				state.getPrevMove(), state.getNextMove(), null);
		
		if (next.isGameOver()){
			double score =  sign*(this.getGameOverScore()); 
			return score;
		} else if (depth == 0){
			double score =  sign*evaluation(nextState);
			return score; //TODO determine if we should put a negative here 
		}

		MoveList moves = getMoves(nextState);  //Changed getMoves for just a GameState and called it on next as oppsed to curr
		
		if (isMaxPlayer){
			for(ArimaaMove move: moves){
				alpha = Math.max(alpha, AlphaBeta(new ArimaaState(state.getPrev(), state.getCurr(), next,
						state.getPrevMove(), state.getNextMove(), move), depth - 1, alpha, beta, false) );
				if (beta <= alpha)
					break; // beta cut off
			}
			return alpha;
		} else {
			for(ArimaaMove move: moves){
				beta = Math.min(beta, AlphaBeta(new ArimaaState(state.getPrev(), state.getCurr(), next,
						state.getPrevMove(), state.getNextMove(), move), depth - 1, alpha, beta, true) );
				if (beta <= alpha)
					break; // alpha cut off
			}
			return beta;
		}
	}
}
