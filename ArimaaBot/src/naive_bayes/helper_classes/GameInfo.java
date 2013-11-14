package naive_bayes.helper_classes;

public class GameInfo {
	
	private String whiteStartState, blackStartState;
	private String moveList;
	
	/** Constructs a GameInfo from the three parameters:
	 * @param whiteSS The string representing white's starting state
	 * @param blackSS The string representing blacks's starting state
	 * @param moveL The (single) string representing the game's move list */
	public GameInfo(String whiteSS, String blackSS, String moveL) {
		whiteStartState = whiteSS;
		blackStartState = blackSS;
		moveList = moveL;
	}
	
	public String getWhiteStartState() { return whiteStartState; }
	public String getBlackStartState() { return blackStartState; }
	public String getMoveList() { return moveList; }
	
}
