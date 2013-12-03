package montecarlo;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import utilities.helper_classes.ArimaaState;

import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.FirstMove;
import arimaa3.GameState;
import arimaa3.MoveList;

public class MonteCarlo {
	
	public static final int AGENT = 0;
	public static final int OUTPUT = 1;
	public static final int NUMTRAILS = 2;
	public static final int NBTABLE = 3;
	
	public static final int MAX_DEPTH = 2;

	/**
	 * @param args - The first string should be the location of the output. The second string is the number of trails
	 */
	public static void main(String[] args) {
		if (args.length < 3){
			System.out.println("Error in usage - 3 (optional 4) string: <agent #> <location of output> <number of trails> <location of NB freq table - if needed>");
			System.out.println("agent 1 = Reflex");
			System.out.println("agent 2 = Naive Bayes Reflex");
			System.out.println("agent 3 = Naive Bayes ply-" + MAX_DEPTH);
			System.exit(1);
		}
		int agent = Integer.parseInt(args[AGENT]);
		int numGames = Integer.parseInt(args[NUMTRAILS]);

		double[] weights = new double[Utilities.TOTAL_FEATURES];
		train(agent, numGames, weights,  args.length >= 4 ? args[NBTABLE] : null);

		outputWeights(weights, new File(args[OUTPUT]));
	}
	
	public static void outputWeights(double[] weights, File output ) {
		try {
			BufferedWriter writer = new BufferedWriter( new FileWriter(output.getAbsoluteFile()) );
			
			for(int i = 0; i < weights.length; i++){
				writer.write(weights[i] + "\n");
			}
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static final double VARIANCE = .01; 
	public static final double ETA = .1; 
	
	public static void train(int agent, int numGames, double[] weights, String fqtbl){
		Random rand = new Random();
		for (int i = 0; i < weights.length; i++){
			weights[i] = VARIANCE*rand.nextGaussian(); //normal belief about the world
		}
		
		for (int i = 0; i < numGames; i++){
			System.out.println("Running game # " + (i+1) );
			runTrial(agent, weights, fqtbl);
		}
	}
	
	private static AbstractAgent getTrainingAgent(int agent, double[] weights, String fqtbl){
		if (agent == 1) {
			return new ReflexAgent(weights, true);
		} else if (agent == 2){
			return new NaiveReflexAgent(weights, true, Utilities.getNBPredictor(fqtbl) );
		} else if (agent == 3) {
			return new NAVVCarlo(weights, true, MAX_DEPTH, Utilities.getNBPredictor(fqtbl) );
		}
			
		return null;
	}


	private static void runTrial(int a, double[] weights, String fqtbl) {
		GameState gameBoard = getRandomStart();
		// 0 is white, 1 is black
		AbstractAgent agent  = getTrainingAgent(a, weights, fqtbl);
		ArimaaState state = new ArimaaState(gameBoard, null);
		ArimaaEngine engine = new ArimaaEngine();
		MoveList possibleMoves = engine.genRootMoves(state.getCurr());
		
		//white needs to make the first move to initialize the state for TD
		ArimaaMove move = agent.selectMove(state, possibleMoves);
		state = new ArimaaState(gameBoard, move); 
		
		int moveCount = 1;
		gameBoard.playFullClear(state.getNextMove(), state.getCurr());
		while( !gameBoard.isGameOver() ) {
			System.out.println(state.getCurr());
			
			possibleMoves = engine.genRootMoves(gameBoard);
			if (possibleMoves.size() == 0) break; //Game Over does not seem to capture this
			
			ArimaaState nextState = new ArimaaState(state.getCurr(), gameBoard, null);
			move = agent.selectMove(nextState, possibleMoves);
			nextState = new ArimaaState(state.getCurr(), gameBoard, move);
			
			//Utilities.TDUpdate(state, nextState, 0, ETA, weights);
			state = nextState;
			
			//Should be the same pointer, but let's be explicit about the update
			agent.setWeights(weights);

			moveCount++;
			
			gameBoard.playFullClear(state.getNextMove(), state.getCurr());
		}

		System.out.println(state.getCurr());
		Utilities.TDUpdate(state, null, 1 , ETA, weights);
	}
	
	private static GameState getRandomStart(){
		GameState board = new GameState();
		FirstMove first_move = new FirstMove();

	    String w_text = first_move.getFirstMove(board, System.currentTimeMillis());
	    board = new GameState(w_text, "");
	    board.playPASS(board);
	    String b_text = first_move.getFirstMove(board, System.currentTimeMillis());
	    
	    return new GameState(w_text, b_text);
	}
	
}
