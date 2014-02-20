package montecarlo;

import static org.junit.Assert.*;

import java.util.PriorityQueue;

import montecarlo.AbstractNAVVSearch.MoveOrder;
import montecarlo.FairyEvaluation.FairyBoard;

import org.junit.Test;

import arimaa3.GameState;

public class MonteCarloTest {
	private static String tests[] = {
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|         D   r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   d X     X     |%135|   d R M c       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   e             |%135| R               |%134|                 |%133| R               |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   E             |%135| r               |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| r E             |%135|                 |%134| r               |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| r E             |%135| c               |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| m E             |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| r E             |%135|                 |%134| c               |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
	};
	
	
	@Test
	public void test() {
		NaiveReflexAgent temp = new NaiveReflexAgent(null, false, null);
		PriorityQueue<MoveOrder> minPQ = new PriorityQueue<MoveOrder>( 3, temp.new MoveOrder(true) );
		minPQ.add(temp.new MoveOrder(null, 0.5) );
		minPQ.add(temp.new MoveOrder(null, 0.6) );
		
		assertEquals(minPQ.remove(), 0.5);
		assertEquals(minPQ.remove(), 0.6);

	}
	
	@Test
	public void testConvertBoard(){
		
		// Test this starting position
	    String white = "1w Ee2 Md2 Ha2 Hh2 Db2 Dg2 Cf2 Cc1 Ra1 Rb1 Rd1 Re1 Rf1 Rg1 Rh1 Rc2"; 
	    String black = "1b ee7 md7 ch8 ca8 dc7 hb7 hg7 df7 ra7 rh7 rb8 rc8 rd8 re8 rf8 rg8";
	    GameState state = new GameState(white,black);
	    
	    System.out.println(state.toBoardString());
	    
	    FairyEvaluation fe = new FairyEvaluation();
		FairyBoard fb = fe.new FairyBoard(state);
		
		System.out.println(fb.toBoardString());
		
		assertTrue(fb.isEqualToGS(state));
		
		// Test all boards in tests[]
		for (int i = 0; i < tests.length; i++){
			state = new GameState(tests[i]);
			fb = fe.new FairyBoard(state);
			assertTrue(fb.isEqualToGS(state));
		}
		
	}

}
