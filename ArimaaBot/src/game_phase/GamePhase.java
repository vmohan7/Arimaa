package game_phase;

public enum GamePhase {
    AGNOSTIC(0), BEGINNING(1), MIDDLE(2), END(3);
    private int value;

    private GamePhase(int value) {
    	this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}; 
