package fairy_agents;

import java.util.Arrays;
import java.util.Random;

import feature_extractor.FeatureExtractor;
import game_phase.GamePhase;
import montecarlo.AbstractCombiner;
import montecarlo.AlphaBetaSearchAgent;
import naive_bayes.MultiNBHypothesis;
import utilities.MoveArrayList;
import utilities.helper_classes.ArimaaState;
import arimaa3.ArimaaMove;
import arimaa3.GameState;
import arimaa3.MoveList;

/**
 * This bot uses MultiNBHypothesis to order moves and prunes all but TOP_K_PERCENT of them.
 * The MultiNB model uses game phase discrimination to "better" (?) order moves.
 * @author Arzav, Neema
 *
 */
public class FairyMoveOrderingClusteringAgent extends FairyAgent {

	private static final double TOP_K_PERCENT = 0.3;

	private MultiNBHypothesis hyp;

	public FairyMoveOrderingClusteringAgent(int depth, MultiNBHypothesis hyp) {
		super(depth);
		this.hyp = hyp;
	}

	
	@Override
	public ArimaaMove selectMove(final ArimaaState arimaaState, MoveList moves){
		return super.selectMove( arimaaState, getMoves(arimaaState) ); //does the limiting for the first set as well
	}


	/**
	 * NOTE: MoveList returned has its internal cursor set to past the last element.
	 * Use Iterator (or for each syntax) to loop over moves in MoveList returned.
	 */
	@Override
	protected MoveList getMoves(ArimaaState state) {
		FeatureExtractor fe = new feature_extractor.FeatureExtractor( state.getCurr(), state.getPrev(), state.getPrevPrev(),
				state.getPrevMove(), state.getPrevPrevMove() );

		MoveArrayList moves = genRootMovesArrayList(state.getCurr());
		int k = (int) Math.ceil(moves.size() * TOP_K_PERCENT);
		ScoredMove[] sortedMoves = topKMoves(fe, moves, state.getCurr(), k);
		// NOTE: moves' internal count for .getMove() has been modified...

		MoveArrayList bestMoves = new MoveArrayList(k);

		for(int i = 0; i < k; i++)
			bestMoves.getMove().copy(sortedMoves[i].move);	

		return bestMoves;
	}

	/**
	 * If we need to shave off more time, we could modify MoveList to support sorting based
	 * on scores rather than having to copy it over to an array and sort.
	 * @param fe
	 * @param moves
	 * @param curr
	 * @return array of ScoredMove that is of same size as 'moves'. First k entries are 
	 * sorted based on ScoredMove natural ordering (defined by DESC_ORDER). Entries 
	 * following the first k are NOT SORTED!
	 */
	private ScoredMove[] topKMoves(FeatureExtractor fe, MoveList moves, GameState curr, int k) {

		ScoredMove[] topKMoves = new ScoredMove[moves.size()];
		int i = 0;
		for (ArimaaMove m : moves)
			topKMoves[i++] = new ScoredMove(m, hyp.evaluate(fe.extractFeatures(m), curr));

		select(topKMoves, 0, topKMoves.length-1, k-1);
		Arrays.sort(topKMoves, 0, k);
		return topKMoves;
	}



	/* ---- Static helpers for partitioning ---- */

	private static int partition(ScoredMove[] list, int left, int right, int pivotIndex) {
		ScoredMove pivotValue = list[pivotIndex];
		swap(list, pivotIndex, right);  // Move pivot to end
		int storeIndex = left;
		for (int i = left; i < right; i++) {
			/* Uses natural ordering: list[i] "<=" pivotValue
			 * Natural ordering is ascending by default. compareTo implemented
			 * below modifies it depending on DESC_ORDER.
			 * e.g., integers would be ordered in ascending order by a partition using
			 * list[i] <= pivotValue */
			if (list[i].compareTo(pivotValue) <= 0) {
				swap(list, storeIndex, i);
				storeIndex++;
			}
		}
		swap(list, right, storeIndex);  // Move pivot to its final place
		return storeIndex;
	}

	private static void swap(ScoredMove[] list, int first, int second) {
		ScoredMove temp = list[first];
		list[first] = list[second];
		list[second] = temp;
	}

	/**
	 * Puts the nth largest element in its sorted position in list with
	 * either side of the array partitioned.
	 * @param list
	 * @param left inclusive
	 * @param right inclusive
	 * @param n nth largest element
	 */
	private static void select(ScoredMove[] list, int left, int right, int n) {
		if (left == right) return;

		Random rgen = new Random();

		while (true) {
			int pivotIndex = randomInRange(left, right, rgen); // select pivotIndex between left and right
			pivotIndex = partition(list, left, right, pivotIndex);
			if (n == pivotIndex) 
				return;
			else if (n < pivotIndex)
				right = pivotIndex - 1;
			else
				left = pivotIndex + 1;
		}
	}

	/** 
	 * Public wrapper just for testing. 
	 * Puts the nth largest element in its sorted position in list with
	 * either side of the array partitioned. 
	 */
	public static void testSelect(ScoredMove[] list, int n) {
		select(list, 0, list.length - 1, n - 1);
	}

	/** Returns an integer in [low, high] -- both inclusive. */
	private static int randomInRange(int low, int high, Random rgen) {
		int numNumsInRange = high - low + 1;
		return rgen.nextInt(numNumsInRange) + low;
	}




	protected static class ScoredMove implements Comparable<ScoredMove>{

		private static final boolean DESC_ORDER = true;
		public ArimaaMove move;
		public double score;

		public ScoredMove(ArimaaMove move, double w){
			this.move = move;
			score = w;
		}

		@Override
		public int compareTo(ScoredMove move2) {
			int w = 0;
			if (this.score < move2.score)
				w = -1;
			else if (this.score > move2.score)
				w = 1;

			if (DESC_ORDER)
				return -w;
			else 
				return w;
		}

	}
}
