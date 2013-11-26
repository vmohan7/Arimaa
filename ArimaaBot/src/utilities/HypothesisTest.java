package utilities;

import java.util.ArrayList;
import java.util.BitSet;

import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.MoveList;
import feature_extractor.FeatureExtractor;
import utilities.helper_classes.ArimaaState;
import utilities.helper_classes.GameInfo;
import utilities.helper_classes.Utilities;

public class HypothesisTest {
	
    private static class AggregateThread implements Runnable {
    	private int gameNumber;
    	private GameParser gp;
    	private AbstractHypothesis hyp;
    	private AggregateResults totalScore;
    	
    	public AggregateThread(GameInfo gi, int gameNum, AbstractHypothesis hyp, AggregateResults total_results){
    		gp = new GameParser(gi);
    		gameNumber = gameNum;
    		this.hyp = hyp;
    		totalScore = total_results;
    	}
    	
	    public void run() {
	    	final long startTime = System.currentTimeMillis(); //for each test, we can say how long it took
			System.out.println("Testing game # " + gameNumber + "..."); //time will be appended in-line
			
			AggregateResults ar = new AggregateResults();
			while (gp.hasNextGameState())
				evaluateMoveOrdering(ar, hyp, gp.getNextGameState());
			
			totalScore.addResult(ar);
			//???? print ar moves for game????
			
			final long endTime = System.currentTimeMillis();
			System.out.println("Testing game # " + gameNumber + " took " + Utilities.msToString(endTime - startTime)); //this is appended to "Testing on game #x..."
	    }
    }
	
	/** Runs evaluation based on the 30% of the data that is in GameData
	 * Once we have run our tests, we print out the statistics.
	 * The client should ensure that GameData is in the correct mode (train or test)
	 * before calling this method. 
	 * @param hyp The evaluation hypothesis 
	 * @param gd The testing game data 
	 * */
	public static void test(AbstractHypothesis hyp, GameData gd) { //rename
		
		AggregateResults totalScore = new AggregateResults();
		
		int count = 0;
		while (gd.hasNextGame()) {
			int cores = Runtime.getRuntime().availableProcessors() - 1; //do based on the number of cores to limit memory usage
			ArrayList<Thread> threads = new ArrayList<Thread>();
			
			for (int i = 0; i < cores && gd.hasNextGame(); i++){
				count++;
				GameInfo gi = gd.getNextGame();
				Thread t = new Thread( new AggregateThread(gi, count, hyp, totalScore) );
				t.start();
				threads.add(t);
			}
			
			for(Thread t: threads){
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println(totalScore); //prints the statistics stored in AggregateResults
	}
	
	/** Given an arimaaState, we run evaluations on the 16k moves
	 * and see where the expert move lies.
	 * @param ar The result to update 
	 * @param hyp The hypothesis
	 * @param arimaaState The state containing the expert move */
	private static void evaluateMoveOrdering(AggregateResults ar, AbstractHypothesis hyp, ArimaaState arimaaState){
		FeatureExtractor fe = new FeatureExtractor(arimaaState.getCurr(), arimaaState.getPrev());
		ArimaaEngine ai = new ArimaaEngine(); //TODO see if this is stupidly slow
		MoveList possibleMoves = ai.genRootMoves(arimaaState.getCurr());
		
		int numAbove = 0;
		ArimaaMove expertMove = arimaaState.getNextMove();
		BitSet bs = fe.extractFeatures( expertMove );
		double expertWeight = hyp.evaluate(bs);
		
		for (ArimaaMove possibleMove : possibleMoves){
			if ( !possibleMove.equals( expertMove ) ){ //make sure that this is not the expert move
				bs = fe.extractFeatures(possibleMove);
				if ( hyp.evaluate(bs) > expertWeight ){ // must be greater than expert in ordering
					numAbove++;
				}
			}
			
		}
		
		ar.addMove(numAbove, possibleMoves.size());
	}
	
	/** This internal class maintains the aggregate 
	 * information across each given move and then prints them out with toString()
	 */
	static class AggregateResults {
		
		public static final double TOP5PERCENT = 0.05;
		
		private int numInTop5Percent = 0; //number of moves where we classified in the top 5%
		private double sumPercent = 0;  //sum used for average percentile
		private int numExpertMoves = 0; //number of expert moves
		
		public synchronized void addResult(AggregateResults otherAr){
			numInTop5Percent += otherAr.getNumInTop5Percent();
			sumPercent += otherAr.getSumPercent();
			numExpertMoves += otherAr.getNumExpertMoves();
		}

		public void addMove(int numAbove, int totalMoves){
			numExpertMoves++;
			double percentage = ( (double) numAbove ) / totalMoves;
			sumPercent += percentage;
			if ( percentage <= TOP5PERCENT){
				numInTop5Percent++;
			}
		}
		
		public double getAvgEvaluation(){
			return 1.0 - sumPercent / numExpertMoves;
		}

		public int getNumInTop5Percent() {
			return numInTop5Percent;
		}
		
		public double getSumPercent() {
			return sumPercent;
		}

		public int getNumExpertMoves() {
			return numExpertMoves;
		}
		
		public String toString(){
			double avgEval = getAvgEvaluation() * 100;
			return String.format("The proportion of expert moves classified in the top 5 percent of the move ordering is : %d / %d\n"+
									"The average percentile of the expert move classified is : %f\n", 
									numInTop5Percent, numExpertMoves, avgEval);
		}

	}

}
