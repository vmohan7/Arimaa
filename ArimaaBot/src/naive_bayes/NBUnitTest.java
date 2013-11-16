package naive_bayes;

import static org.junit.Assert.*;

import java.util.BitSet;

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
		long[][] frequencyTable = new long[2][FeatureConstants.NUM_FEATURES];
		
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
		
		assertTrue(frequencyTable[0][245] == 10);
		assertTrue(frequencyTable[1][245] == 1);
		assertTrue(frequencyTable[0][763] == 1);
		assertTrue(frequencyTable[0][767] == 1);
		assertTrue(frequencyTable[0][768] == 1);
		assertTrue(frequencyTable[1][769] == 1);
		assertTrue(frequencyTable[0][770] == 2);
		assertTrue(frequencyTable[0][771] == 1);
		assertTrue(frequencyTable[0][772] == 1);
		assertTrue(frequencyTable[0][773] == 1);
		assertTrue(frequencyTable[0][774] == 1);
		assertTrue(frequencyTable[0][775] == 1);
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
	
	@Test
	public void testHypothesis() {
		long[][] frequencyTable = new long[2][FeatureConstants.NUM_FEATURES];
		frequencyTable[0][0] = 2;
		frequencyTable[1][0] = 1;
		
		NBHypothesis hyp = new NBHypothesis(frequencyTable);
		BitSet bs = new BitSet(1);
		bs.set(0);
		
		double weight = hyp.evaluate(bs);
		System.out.println("The weight is: "+ weight);
	}

}
