package game_phase;

public enum GamePhase {
    BEGINNING(0), MIDDLE(1), END(2);
    private int value;

    private GamePhase(int value) {
    	this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}; 
