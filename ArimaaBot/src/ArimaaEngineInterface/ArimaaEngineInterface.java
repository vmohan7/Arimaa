package ArimaaEngineInterface;

import java.io.*;
import java.util.*;

import fairy_agents.*;
import naive_bayes.MultiNBHypothesis;
import naive_bayes.NBHypothesis;
import montecarlo.*;
import ai_util.LogFile;


public class ArimaaEngineInterface {
	/*
	 * Step 1 -- Run XMeansWrapper to train the clusters.
	 * Step 2 -- Run MultiNBMain to train move ordering.
	 * Step 3 -- Run loop_games.py.
	 * Step $ -- Profit.
	 */
	
	//private MultiNBHypothesis multiNBHyp;
	
	/** 
	 * Reads in the MultiNBHypothesis from an existing serialized file.
	 * <br><i>-- This requires that MultiNBMain.main(argv) has been run. Make sure to change
	 * the constants (such as number of games, etc.) to the right values. --</i>
	 */
	/*
	public ArimaaEngineInterface() {
	//	multiNBHyp = MultiNBHypothesis.getMultiNBHypothesis();
	//	assert(multiNBHyp != null);
	}
	*/
	
	// All messages must by sent thru here so they get logged
	private static void send_message(String text) {
		text += "\n";
		LogFile.message("AEI s --> " + text);
		System.out.print(text);
		System.out.flush();
	}

	/** resending chat to AEI engine
	 * @param msg
	 */
	public static void chat(String msg) {
		send_message("chat "+msg);
	}
	
	private double[] weights = new double[Utilities.TOTAL_FEATURES];
	private int bot_type = 0; //invalid type
	private NBHypothesis hyp = null;
	
	/*
	 * 
	 * Bot 1 = Reflex Agent
	 * Bot 2 = Naive Reflex Agent
	 * Bot 3 = Alpha Beta Reflex Agent
	 * Bot 4 = NAVVClueless
	 * 
	 */
	
	private void control() {

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));	
		GameControl gc = new GameControl( new FairyAgent(1) );

		while (true) {
			try {
				// Get the message
				String message = reader.readLine();

				// Write message to log file
				LogFile.message("AEI r--> " + message);

				// If connection broken, just shutdown
				if (message == null) {
					//info.engine.shutdown_search();
					return;
				}

				ArimaaEngineInterfaceCommand AEIcommand = new ArimaaEngineInterfaceCommand(message);

				// Figure out which command it is
				if (AEIcommand.command.equals( ArimaaEngineInterfaceCommand.AEI )) {
					send_message("protocol version 1");
					send_message("id name NAVV");
					send_message("id author NAVV");
					send_message("id version 2013");
					send_message("aeiok");
				}

				else if (AEIcommand.command.equals("isready")) {
					send_message("readyok");
				}

				else if (AEIcommand.command.equals("quit")) {
					return;
				}

				else if (AEIcommand.command.equals("stop")) {

				}
				else if (AEIcommand.command.equals("print")) {
					System.out.println( gc.state.getCurr().toBoardString() );
				}
				else if (AEIcommand.command.equals("newgame")) {
					send_message("log starting new game using bot " + bot_type);
					
					LogMoveFile.write("\n-------------------------------------------------------------\n");
					
					gc.reset();
					if (bot_type == 1)
						gc.setAgent( new ReflexAgent(weights, false) ); 
					else if (bot_type == 2)
						gc.setAgent(new NaiveReflexAgent(weights, false, hyp));
					else if (bot_type == 3)
						gc.setAgent(new NAVVCarlo(weights, false, 2, hyp));
					else if (bot_type == 4)
						gc.setAgent(new NAVVClueless(null, false, 2, hyp));
					else if (bot_type == 5)
						gc.setAgent( new FairyAgent(2) );
					else if (bot_type == 6)
						gc.setAgent( new FairyAgentHeuristicHardcoded(2) );
					else if (bot_type == 7)
						gc.setAgent( new FairyAgentHeuristicHardcodedReduced(2) );
					//else if (bot_type == 8)
						//gc.setAgent( new FairyMoveOrderingClusteringAgent(2, multiNBHyp) );

				}
				else if (AEIcommand.command.equals("makemove")) {
					String move_text = AEIcommand.getRestOfCommand();
					gc.getMove(move_text);
					
					LogMoveFile.write(move_text);
				}
				else if (AEIcommand.command.equals("setoption")) {
					//TODO check if weights is given for agents
					String name = AEIcommand.arguments[2];
					String value = AEIcommand.arguments[4];
					set_option(name, value);
				}

				else if (AEIcommand.command.equals("go")) {
					send_message("log starting search");
						
					send_message("bestmove " + gc.sendMove() );
				}
				else {
					//May see setposition??????
					// Unknown command received, just log it and ignore the
					LogFile.message("Unknown command " + AEIcommand.command);
				}

			} // Loop forever

			// This should never happen!
			// We're sunk if we get here, just exit
			catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String stackTrace = sw.toString();
				LogFile.message("Exception " + e);
				LogFile.message(stackTrace);
			}
		}
	}

	private void set_option(String name, String value) {
		if (name.equals("weights")){
			setWeights(value);
		} else if (name.equals("nb") ){
			hyp = Utilities.getNBPredictor(value);
		} else if (name.equals("type") ){
			bot_type = Integer.parseInt(value);
		} else if (name.equals("write") ){
			LogMoveFile.setWrtie(true);
		} else if (name.equals("nowrite") ){
			LogMoveFile.setWrtie(false);
		}
		
	}
	
	private void setWeights(String fileLoc){
			try {
				Scanner reader = new Scanner( (new File(fileLoc)).getAbsoluteFile() );
				int counter = 0;
				while(reader.hasNext()){ //for a valid file, it needs TOTAL_FEATURES WEIGHTS
					weights[counter] = reader.nextDouble();
					counter++;
				}
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1); //cannot continue
			}
	}

	public static void main(String args[]) {
		LogFile.setMessageDisplay(false);
		LogFile.message((new Date()).toString());

		ArimaaEngineInterface main = new ArimaaEngineInterface();
		main.control();
	}
	
	/*

	// Separate thread to run the engine
	private class EngineThread implements Runnable {
		ArimaaEngine engine = new ArimaaEngine();
		ReflexAgent agent = new ReflexAgent(new double[1], false);
		
		public void run() {
			long start_time = System.currentTimeMillis();
			
			MoveList moves = engine.genRootMoves(state.getCurr());
			ArimaaMove bestMove = agent.selectMove(state, moves);
			
			// remove any pass words, as arimaa-online doesn't want them
			String final_move = bestMove.toOfficialArimaaNotation( state.getCurr() ).replaceAll(" pass", "");
			state.update(bestMove);
			
			send_message("bestmove " + final_move);
			
			long elapsed_time = System.currentTimeMillis() - start_time;
			LogFile.message("Elapsed time: " + elapsed_time + " ms");

		}
	}
	
	
	private void set_option(String name, String value_text) {
		name = name.toLowerCase();
		LogFile.message("set option "+name+":"+value_text);
		if (name.equals("rated")) {
			//info.is_rated = value_text.equals("1") ? true : false;
		} else if (name.equals("hash")) {
			//info.allowed_hash_size = Integer.parseInt(value_text);
		} else if (name.equals("opponent")) {
			//info.enemy_name = value_text;
		} else if (name.equals("tcmove")) {
			int value = Integer.parseInt(value_text);
			//info.tc_move = value;
		} else if (name.equals("tcpercent")) {
			int value = Integer.parseInt(value_text);
			//info.tc_percent = value;
		} else if (name.equals("tcmax")) {
			int value = Integer.parseInt(value_text);
			if (value == 0) {
				value = Integer.MAX_VALUE;
			}
			//info.tc_max_reserve = value;
		} else if (name.equals("tcreserve")) {
			int value = Integer.parseInt(value_text);
			//info.tc_reserve[0] = info.tc_reserve[1] = value;
		} else if (name.equals("tcturns")) {
			int value = Integer.parseInt(value_text);
			//info.tc_turns = value;
		} else if (name.equals("rating")) {
			int value = Integer.parseInt(value_text);
			//info.rating = value;
		} else if (name.equals("opponent_rating")) {
			int value = Integer.parseInt(value_text);
			//info.opponent_rating = value;
		} else if (name.equals("tctotal")) {
			int value = Integer.parseInt(value_text);
			//info.tc_total = value;
		}  else if (name.equals("moveused")) {
			int value = Integer.parseInt(value_text);
			//info.moveused = value;
		} else if (name.equals("tcmoveused")) {
			int value = Integer.parseInt(value_text);
			//info.tc_moveused = value;
		} else if (name.equals("tcturntime")) {
			int value = Integer.parseInt(value_text);
			if (value == 0) {
				value = Integer.MAX_VALUE;
			}
			//info.tc_max_turn_time = value;
		} else if (name.equals("forbidden")) {
			String movetext = value_text.replace("_", " ");		
			LogFile.message("forbidden "+value_text+">"+movetext);
			//TODO HANDLE THIS CASE
			ArimaaMove move = new ArimaaMove(movetext);
			info.gs.playMove(move);
			long curr_hash = info.gs.getPositionHash();
			info.gs.unplayMoveFull(move);
			info.repetition_rules.positionMoves.put(curr_hash, "");
			
		} else {
			LogFile.message("unknown info "+name);
		}
	}
*/
}
