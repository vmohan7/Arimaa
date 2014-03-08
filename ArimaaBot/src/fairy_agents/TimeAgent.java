package fairy_agents;

import game_phase.FeatureExtractor;
import montecarlo.AlphaBetaSearchAgent;
import utilities.helper_classes.ArimaaState;
import arimaa3.GameState;
import arimaa3.MoveList;

public class TimeAgent {

	public static final int DEPTH = 2;
	public static final String test_positions[] = {
			  "16b %13 +-----------------+%138|     e           |%137|                 |%136|                 |%135|           E     |%134|                 |%133|       R         |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4
			  "16w %13 +-----------------+%138|     e           |%137|                 |%136|                 |%135|           E     |%134|                 |%133|       R         |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13", // Endgame Corner I Pos 4
			  "35b %13 +-----------------+%138|           r r   |%137|               r |%136|           R E   |%135|   d             |%134|     r   r       |%133| r M     D   e   |%132| R h d     D R   |%131|     R R       m |%13 +-----------------+%13   a b c d e f g h%13",
			  "30b %13 +-----------------+%138| r   E c r h r r |%137| R r             |%136| C   r           |%135|     e D         |%134|   H     r       |%133|       H     C   |%132|     R M R D   R |%131|     R   R R R   |%13 +-----------------+%13   a b c d e f g h%13",
			  "41b %13 +-----------------+%138|                 |%137|         r       |%136|         e   r   |%135|   R         D r |%134|     r     E     |%133| r               |%132| R M d     m     |%131|   h R R     R   |%13 +-----------------+%13   a b c d e f g h%13",
			  "45b %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|     R           |%134|     e   r   r   |%133| r     r E   D r |%132| R M   R m       |%131|   h R         R |%13 +-----------------+%13   a b c d e f g h%13",
			  "46b %13 +-----------------+%138|                 |%137|                 |%136|                 |%135|                 |%134|         r   r   |%133| r   r   E   D r |%132| R M e R m     R |%131|   h R           |%13 +-----------------+%13   a b c d e f g h%13",
			  "38w %13 +-----------------+%138|   c   r r       |%137| r   r       r   |%136|   r   H       d |%135| R h   h       R |%134| R   e         r |%133|           E C   |%132|                 |%131| R       R R   R |%13 +-----------------+%13   a b c d e f g h%13",
			  "34w %13 +-----------------+%138|                 |%137|     r   M     r |%136|       H e   r R |%135|   h R     R     |%134| r C       r     |%133|       D         |%132|           d E m |%131|     R       R R |%13 +-----------------+%13   a b c d e f g h%13",
			  "40w %13 +-----------------+%138|         M       |%137|     r H e     r |%136|   h         r R |%135|                 |%134| r D     R d m   |%133|   C         E   |%132|             R   |%131| R             R |%13 +-----------------+%13   a b c d e f g h%13",
			  "15w %13 +-----------------+%138|   c r r r       |%137| r   r     r     |%136|   h         d   |%135|         d E   r |%134|       R   h r c |%133|     H   e     H |%132| D   D M     m C |%131| R R R R R   R R |%13 +-----------------+%13   a b c d e f g h%13",
			  "2b %13 +-----------------+%138| r r r r r r r r |%137| d m c e h c h d |%136|                 |%135|       E         |%134|                 |%133|                 |%132| D H C M   C H D |%131| R R R R R R R R |%13 +-----------------+%13   a b c d e f g h%13"
	};
	
	public static void main(String[] args) {
		FairyAgentHeuristicHardcodedReduced phaseAgent = new FairyAgentHeuristicHardcodedReduced(DEPTH);
		FairyAgent fairyAgent = new FairyAgent(DEPTH);
		
		printTimeForAgent(phaseAgent);
		System.out.println("---------------------------------------------------------------");
		printTimeForAgent(fairyAgent);

	}
	
	public static void printTimeForAgent(AlphaBetaSearchAgent agent){
		double avgTime = 0.0;
		for(String test_pos : test_positions){
			GameState board = new GameState(test_pos);
			ArimaaState state = new ArimaaState(board, null);
			MoveList moves = agent.genRootMovesArrayList(board);
			
			double startTime = System.currentTimeMillis();
			agent.selectMove(state, moves);
			double totalTime = System.currentTimeMillis() - startTime;
			
			System.out.println(board.toBoardString() + "\n" 
								+ "Time to select move (ms): " + totalTime
			);

			System.out.flush();
			
			avgTime += totalTime;

		}
		
		
		avgTime /= test_positions.length;
		System.out.println("Average Time: " + avgTime);
	}

}
