package ArimaaEngineInterface;

import montecarlo.AbstractAgent;
import utilities.helper_classes.ArimaaState;
import ai_util.LogFile;
import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.FirstMove;
import arimaa3.GameState;
import arimaa3.GenTurn;
import arimaa3.MoveList;

public class GameControl {
	
	private ArimaaState state;
	private ArimaaEngine engine;
	private boolean isFirst;
	private AbstractAgent agent;
	
	private String move_history;
	
	private String w_state, b_state;
	public GameControl(AbstractAgent agent){
		this.agent = agent;
		reset();
	}
	
	public void reset(){
		engine = new ArimaaEngine();
		state = new ArimaaState(new GameState(), null);
		isFirst = true;
		w_state = null;
		b_state = null;
		move_history = "";
	}
	
	public void setAgent(AbstractAgent a){
		agent = a;
	}
	
	public void getMove(String move){
		LogFile.message("Recieved Move: " + move);
		move_history += (move + "%13");
		if (isFirst){
			if (w_state == null){
				w_state = move;
				return;
			} else {
				b_state = move;
				state = new ArimaaState(new GameState(w_state, b_state), null);
				isFirst = false;
				return;
			}
		}
		
		ArimaaMove bestMove = new ArimaaMove(move);
		bestMove.steps = 4;
		updateBoard(bestMove);
	}
	
	public String sendMove(){
		long start_time = System.currentTimeMillis();
		
		if (isFirst){
			FirstMove first_move = new FirstMove();
			if (w_state == null){
				return first_move.getFirstMove(new GameState() , System.currentTimeMillis());
			} else {
				GameState temp = new GameState(w_state, "");
				temp.playPASS(temp);
				return first_move.getFirstMove(temp , System.currentTimeMillis());
			}
		}
		
		ArimaaMove bestMove = agent.selectMove(state, move_history);
		
		// remove any pass words, as arimaa-online doesn't want them
		GenTurn gt = new GenTurn();
		String final_move = gt.getOfficialArimaaNotation(state.getCurr(), bestMove).replaceAll(" pass", "");
		
		long elapsed_time = System.currentTimeMillis() - start_time;
		LogFile.message("Elapsed time: " + elapsed_time + " ms");

		return final_move;
	}
	
	private void updateBoard(ArimaaMove move){
		GameState next = new GameState();
		next.playFullClear(move, state.getCurr());
		
		LogFile.message(move.toString());
		LogFile.message(next.toBoardString());
		
		state = new ArimaaState(state.getPrev(), state.getCurr(), next, 
				state.getPrevMove(), move, null);
	}
}
