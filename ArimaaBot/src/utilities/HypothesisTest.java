package utilities;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.Semaphore;

import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.MoveList;
import feature_extractor.FeatureExtractor;
import utilities.AbstractGameData.Mode;
import utilities.helper_classes.ArimaaState;
import utilities.helper_classes.GameInfo;
import utilities.helper_classes.Utilities;

public class HypothesisTest {
	
    private static class AggregateThread implements Runnable {
    	private int gameNumber;
    	private GameParser gp;
    	private AbstractHypothesis hyp;
    	private AggregateResults totalScore;
    	private Mode mode;
    	
    	public AggregateThread(GameInfo gi, int gameNum, AbstractHypothesis hyp, AggregateResults total_results, Mode mode){
    		gp = new GameParser(gi);
    		gameNumber = gameNum;
    		this.hyp = hyp;
    		totalScore = total_results;
    		this.mode = mode;
    	}
    	
	    public void run() {
	    	try {
	    		
				threadsRunning.acquire();
				
				final long startTime = System.currentTimeMillis(); //for each test, we can say how long it took
				System.out.println("Testing game # " + gameNumber + "..."); 
				
				AggregateResults ar = new AggregateResults();
				while (gp.hasNextGameState())
					evaluateMoveOrdering(ar, hyp, gp.getNextGameState(), mode, gp.getMoveNumber());
				
				totalScore.addResult(ar); //this method is synchronized so threading does not affect this
				//???? print ar moves for game????
				
				final long endTime = System.currentTimeMillis();
				System.out.println("Testing game # " + gameNumber + " took " + Utilities.msToString(endTime - startTime)); 
				
				threadsRunning.release();
				
	    	} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
    }
	
    public static final int FREE_CORES = 2;
    private static final int MAX_NUM_THREADS = Runtime.getRuntime().availableProcessors() - FREE_CORES;
    private static Semaphore threadsRunning;
    
	/** Runs evaluation based on the 30% of the data that is in GameData
	 * Once we have run our tests, we print out the statistics.
	 * The client should ensure that GameData is in the correct mode (train or test)
	 * before calling this method. 
	 * @param hyp The evaluation hypothesis 
	 * @param gd The testing game data */
	public static void test(AbstractHypothesis hyp, AbstractGameData gd) { //rename
		
		AggregateResults totalScore = new AggregateResults();
		threadsRunning = new Semaphore(MAX_NUM_THREADS);
		ArrayList<Thread> threads = new ArrayList<Thread>();
		int count = 0;
		while (gd.hasNextGame()) {
			final long startTime = System.currentTimeMillis(); //for each test, we can say how long it took
			Utilities.printInfoInline("Testing game # " + ++count + "..."); //time will be appended in-line
			
			GameInfo gi = gd.getNextGame();
			GameParser gp = new GameParser(gi);
			
			AggregateResults ar = new AggregateResults();
			while (gp.hasNextGameState())
				evaluateMoveOrdering(ar, hyp, gp.getNextGameState(), gd.getMode(), gp.getMoveNumber());
			
			totalScore.addResult(ar);
			//???? print ar moves for game????
			
			final long endTime = System.currentTimeMillis();
			Utilities.printInfo("testing took " + Utilities.msToString(endTime - startTime)); //this is appended to "Testing on game #x..."
		}
		
		Utilities.printInfo(totalScore.toString()); //prints the statistics stored in AggregateResults
		
		// Print machine-parseable hypothesis evaluation stats
		// Format: num-games,{TEST|TRAIN},percentile,proportion-in-top-5%
		String parseable = String.format("%d,%s,%f,%f", 
										gd.getNumGames(), 
										gd.getMode().name(), 
										totalScore.getAvgEvaluation() * 100, 
										(double) totalScore.numInTop5Percent / totalScore.numExpertMoves);

		Utilities.printParseable(parseable);
	}
	
	/** Given an arimaaState, we run evaluations on the 16k moves
	 * and see where the expert move lies.
	 * @param ar The result to update 
	 * @param hyp The hypothesis
	 * @param arimaaState The state containing the expert move */
	private static void evaluateMoveOrdering(AggregateResults ar, AbstractHypothesis hyp, ArimaaState arimaaState, Mode mode, int moveNumber){
		FeatureExtractor fe = new FeatureExtractor(arimaaState.getCurr(), arimaaState.getPrev(), arimaaState.getPrevPrev(), arimaaState.getPrevMove(), arimaaState.getPrevPrevMove());
		ArimaaEngine ai = new ArimaaEngine(); //TODO see if this is stupidly slow
		MoveList possibleMoves = ai.genRootMoves(arimaaState.getCurr());
		
		int numAbove = 0;
		ArimaaMove expertMove = arimaaState.getNextMove();
		BitSet bs = fe.extractFeatures( expertMove );
		double expertWeight = hyp.evaluate(bs, arimaaState.getCurr());
		
		for (ArimaaMove possibleMove : possibleMoves){
			if ( !possibleMove.equals( expertMove ) ){ //make sure that this is not the expert move
				bs = fe.extractFeatures(possibleMove);
				if ( hyp.evaluate(bs, arimaaState.getCurr()) > expertWeight ){ // must be greater than expert in ordering
					numAbove++;
				}
			}
			
		}
		
		ar.addMove(numAbove, possibleMoves.size(), mode, moveNumber);
	}
	
	/** This internal class maintains the aggregate 
	 * information across each given move and then prints them out with toString()
	 */
	static class AggregateResults {
		
		public static final double TOP5PERCENT = 0.05; // do we want to change this to top 10%?
		
		private int numInTop5Percent = 0; //number of moves where we classified in the top 5%
		private double sumPercent = 0;  //sum used for average percentile
		private int numExpertMoves = 0; //number of expert moves
		
		//This method is synchronized because we are overwriting variables and don't want to lose information
		public synchronized void addResult(AggregateResults otherAr){
			numInTop5Percent += otherAr.getNumInTop5Percent();
			sumPercent += otherAr.getSumPercent();
			numExpertMoves += otherAr.getNumExpertMoves();
		}

		public void addMove(int numAbove, int totalMoves, Mode mode, int moveNumber){
			numExpertMoves++;
			double percentage = ( (double) numAbove ) / totalMoves;
			sumPercent += percentage;
			if ( percentage <= TOP5PERCENT){
				numInTop5Percent++;
			}
			
			if (mode == Mode.TEST) {
				Utilities.printPercentile("PERCENTILE," + moveNumber + "," + (1.0 - percentage));
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
