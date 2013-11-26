package utilities;

import utilities.helper_classes.GameInfo;

public interface AbstractGameData {

	public abstract boolean hasNextGame();
	public GameInfo getNextGame();
	
}
