package montecarlo;

import fairy_agents.FairyEvaluation;
import game_phase.GamePhase;
import game_phase.GamePhaseHeuristicDiscriminator;

import java.util.ArrayList;

import org.junit.Test;

import utilities.helper_classes.ArimaaState;
import arimaa3.ArimaaEvaluate2;
import arimaa3.ArimaaMove;
import arimaa3.Constants;
import arimaa3.GameState;
import arimaa3.MoveList;

public class AlphaBetaTest {
	public static int DEPTH = 2;
	private static class TestAgent extends AlphaBetaSearchAgent {

		private boolean firstMove = false;
		private ArimaaEvaluate2 eval;
		public ArrayList< ArrayList<Double> > depthPrint;
		private DefaultCombiner dCombiner;

		/** This is the original evaluation -- uses the default AbstractCombiner implementation. */
		protected class DefaultCombiner extends AbstractCombiner {

			public DefaultCombiner(GamePhase whichPhase) {
				super(whichPhase);
			}
			
		}

		public TestAgent(int depth) {
			super(null, false, depth);
			eval = new ArimaaEvaluate2();
			depthPrint = new ArrayList< ArrayList<Double> >();
			for(int i = 0; i < DEPTH; i++){
				depthPrint.add( new ArrayList<Double>() );
			}
			dCombiner = new DefaultCombiner(null); //expect the null to be overwritten before use
		}

		@Override
		protected double getGameOverScore(GameState unused) { return Constants.SCORE_MATE; }

		private double getRank(long bb){
			if ( (bb & Constants.RANK_1) > 0){
				return 1;
			}
			else if ( (bb & Constants.RANK_2) > 0){
				return 2;
			}
			else if ( (bb & Constants.RANK_3) > 0){
				return 3;
			}
			else if ( (bb & Constants.RANK_4) > 0){
				return 4;
			}
			else if ( (bb & Constants.RANK_5) > 0){
				return 5;
			}
			else if ( (bb & Constants.RANK_6) > 0){
				return 6;
			}
			else if ( (bb & Constants.RANK_7) > 0){
				return 7;
			}			
			else if ( (bb & Constants.RANK_8) > 0){
				return 8;
			}
			
			
			return 0;
		}
		
		@Override
		protected double evaluation(ArimaaState state) {
			// Use dummy evaluation
//			if (state.getCurr().player == Constants.PL_WHITE ){
//				return getRank( state.getCurr().piece_bb[Constants.PT_WHITE_RABBIT] );
//			} else {
//				return -getRank( state.getCurr().piece_bb[Constants.PT_WHITE_RABBIT] );
//			}
			
			// Copied from FairyAgent.java
			GameState curr = state.getCurr();
			dCombiner.setGamePhase(GamePhaseHeuristicDiscriminator.getStrictGamePhase(curr));
			return FairyEvaluation.evaluate(curr, dCombiner);
		}

		
		@Override
		public ArimaaMove selectMove(final ArimaaState arimaaState, String move_history){
			//TODO: Use move_history as in AlphaBetaSearchAgent?
			MoveList moves = getMoves(arimaaState);
			
			if (firstMove){
				eval.PreProcessRootPosition(arimaaState.getCurr());
				firstMove = false;
			}
			ArimaaMove bestMove = null;
			double maxAlpha = Double.NEGATIVE_INFINITY;
			
			for(ArimaaMove move : moves){
				double a = AlphaBeta(new ArimaaState(arimaaState.getPrevPrev(), arimaaState.getPrev(), arimaaState.getCurr(), 
								arimaaState.getPrevPrevMove(), arimaaState.getPrevMove(), move) , maxDepth - 1, maxAlpha, Double.POSITIVE_INFINITY, false);
				if (maxAlpha < a){
					maxAlpha = a;
					bestMove = move;
				}
			}
			System.out.println("Root Alpha:"+maxAlpha);
			System.out.println("Best Move:"+bestMove);
			
			// Print out node scores for all nodes in tree
//			for(int i = depthPrint.size() - 1; i >= 0; i-- ){
//				ArrayList<Double> nodes = depthPrint.get(i);
//				for(int j = 0; j < nodes.size(); j++){
//					double t = nodes.get(j);
//					if (t == Double.POSITIVE_INFINITY ){
//						System.out.print( "| ");
//					}
//					else {
//						System.out.print( nodes.get(j) + " ");
//					}
//				}
//				
//				System.out.println();
//			}
			
			return bestMove;
		}
		
		//TOOD add prints
		private double AlphaBeta(final ArimaaState state, int depth, double alpha, double beta, boolean isMaxPlayer){
			GameState next = new GameState();
			next.playFullClear(state.getNextMove(), state.getCurr());
			
			int sign = (maxDepth - depth) % 2 == 0 ? 1 : -1;
			
			ArimaaState nextState = new ArimaaState(state.getPrev(), state.getCurr(), next, 
					state.getPrevMove(), state.getNextMove(), null);
			
			if (next.isGameOver()){
				double score =  sign*(-next.getGameResult());
				depthPrint.get(depth).add( score );
				return score;
			} else if (depth == 0){
				double score =  sign*evaluation(nextState);
				depthPrint.get(depth).add( score );
				return score; //TODO determine if we should put a negative here 
			}

			MoveList moves = getMoves(nextState); 
			
			if (isMaxPlayer){
				for(ArimaaMove move: moves){
					alpha = Math.max(alpha, AlphaBeta(new ArimaaState(state.getPrev(), state.getCurr(), next,
							state.getPrevMove(), state.getNextMove(), move), depth - 1, alpha, beta, false) );
					if (beta <= alpha)
						break; // beta cut off
				}
				depthPrint.get(depth).add( alpha );
				depthPrint.get(depth-1).add( Double.POSITIVE_INFINITY );
				return alpha;
			} else {
				for(ArimaaMove move: moves){
					beta = Math.min(beta, AlphaBeta(new ArimaaState(state.getPrev(), state.getCurr(), next,
							state.getPrevMove(), state.getNextMove(), move), depth - 1, alpha, beta, true) );
					if (beta <= alpha)
						break; // alpha cut off
				}
				depthPrint.get(depth).add( beta );
				depthPrint.get(depth-1).add( Double.POSITIVE_INFINITY );
				return beta;
			}
			
		}

		@Override
		protected MoveList getMoves(ArimaaState state) {
//			MoveList moves = engine.genRootMoves(state.getCurr()); 
			MoveList moves = genRootMovesArrayList(state.getCurr()); 
			return moves;
		}
		
		
	}

	@Test
	public void test() {
		TestAgent agent = new TestAgent(DEPTH);
		String white = "1w Ee2 Md2 Ha2 Hh2 Db2 Dg2 Cf2 Cc1 Ra1 Rb1 Rd1 Re1 Rf1 Rg1 Rh1 Rc2"; 
	    String black = "1b ee7 md7 ch8 ca8 dc7 hb7 hg7 df7 ra7 rh7 rb8 rc8 rd8 re8 rf8 rg8";
		ArimaaState game = new ArimaaState(new GameState(white, black), null);

		double time = System.currentTimeMillis();
		
		ArimaaMove move = agent.selectMove(game, "");
		System.out.println(game.getCurr().toBoardString());
		System.out.println( System.currentTimeMillis() - time + " ms elapsed");
	}
	
	/*
	 *  Vivek's laptop gets...
	 *  440 ms for dummy eval at depth 1
	 *  750 ms for Fairy (default combiner) eval at depth 1
	 *  100 to 2000 ms for one call to getMoves()
	 *  30 seconds for depth 2 search with Fairy eval and movelist size capped at 20,000
	 *  14 seconds for depth 2 search with dummy eval and movelist size capped at 20,000
	 */
				   

}

