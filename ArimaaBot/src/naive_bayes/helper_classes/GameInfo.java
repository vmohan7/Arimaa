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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((blackStartState == null) ? 0 : blackStartState.hashCode());
		result = prime * result
				+ ((moveList == null) ? 0 : moveList.hashCode());
		result = prime * result
				+ ((whiteStartState == null) ? 0 : whiteStartState.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GameInfo other = (GameInfo) obj;
		if (blackStartState == null) {
			if (other.blackStartState != null)
				return false;
		} else if (!blackStartState.equals(other.blackStartState))
			return false;
		if (moveList == null) {
			if (other.moveList != null)
				return false;
		} else if (!moveList.equals(other.moveList))
			return false;
		if (whiteStartState == null) {
			if (other.whiteStartState != null)
				return false;
		} else if (!whiteStartState.equals(other.whiteStartState))
			return false;
		return true;
	}
	
}
