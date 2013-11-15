package naive_bayes;

import static org.junit.Assert.*;

import org.junit.Test;

import utilities.helper_classes.ArimaaState;
import arimaa3.ArimaaEngine;
import arimaa3.ArimaaMove;
import arimaa3.GameState;
import arimaa3.MoveList;
import feature_extractor.FeatureConstants;

public class NBUnitTest {
	
	private static String tests[] = {
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|         D   r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   d X     X     |%135|   d R M c       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
	};

	@Test
	public void testGetFeaturesFromMove() {
		long[][] frequencyTable = new long[FeatureConstants.NUM_FEATURES][2];
		
		GameState curr = new GameState(tests[3]); // TODO: initialize these to sensical values
		
//		System.out.println(curr.toBoardString());
		
		ArimaaState myState = new ArimaaState(null, curr, new ArimaaMove("Rb6n Rb7e"));
		// contrive an ArimaaState
		
		ArimaaEngine myEngine = new ArimaaEngine();
		MoveList list = myEngine.genRootMoves(curr);
//		System.out.println("# Possible moves: "+ list.size());
//		for(ArimaaMove m: list) {
//			System.out.println(m.toBoardString());
//		}
		NBTrain.trainOnTurn(frequencyTable, myState, myEngine);
		
		assertTrue(frequencyTable[245][0] == 10);
		assertTrue(frequencyTable[245][1] == 1);
		assertTrue(frequencyTable[763][0] == 1);
		assertTrue(frequencyTable[767][0] == 1);
		assertTrue(frequencyTable[768][0] == 1);
		assertTrue(frequencyTable[769][1] == 1);
		assertTrue(frequencyTable[770][0] == 2);
		assertTrue(frequencyTable[771][0] == 1);
		assertTrue(frequencyTable[772][0] == 1);
		assertTrue(frequencyTable[773][0] == 1);
		assertTrue(frequencyTable[774][0] == 1);
		assertTrue(frequencyTable[775][0] == 1);
//		for(int i = 0; i< frequencyTable.length; i++) {
//				if(frequencyTable[i][0]!=0) {
//					System.out.println("Non-expert Move: Feature :" + i + " Count: " + frequencyTable[i][0]);
//				}
//				if(frequencyTable[i][1]!=0) {
//					System.out.println("Expert Move: Feature :" + i + " Count: " + frequencyTable[i][1]);
//				}
//		}
		// assert on the values of cells in frequencyTable. 
		
	}

}
