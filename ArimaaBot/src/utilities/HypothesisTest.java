package utilities;

public class HypothesisTest {

	private AbstractHypothesis hypothesis;
	private GameData gameData;
	
	public HypothesisTest(AbstractHypothesis hyp, GameData gd) {
		hypothesis = hyp;
		gameData = gd;
		
		gameData.setMode(GameData.Mode.TEST); //ensure that we are testing...
	}
	
	public void test() { //rename
		
	}
	/* for each game:
	 * 		gp = GameParser(game)
	 * 		while gp.hasMove:
	 * 			ArimaaState as = gp.next
	 * 			FeatureExtractor fe = FeatureExtractor(as.prev, as.curr)
	 * 			getAllMoves(as.curr)
	 * 			for each possible move:
	 * 				BitSet bs = fe.extract(possible move)
	 * 				weight = hypothesis.evaluate(bs)
	 * 			
	 * 			order the moves...
	 * 
	 * print statistics	
	 */
}
