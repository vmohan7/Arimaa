package utilities;

import utilities.helper_classes.GameInfo;

public interface AbstractGameData {

	public abstract boolean hasNextGame();
	public abstract GameInfo getNextGame();
	public abstract int getNumGames();
	public abstract Mode getMode();
	
	public static enum Mode {
		TRAIN, TEST;
	}
	
}
