package server.utils;

public class Constants {
    // Server constants
    public static final int SERVER_PORT = 12345;

    // Packet types
    public static final String CONNECT = "CONNECT";
    public static final String PLACE_SHIP = "PLACE_SHIP";
    public static final String ATTACK = "ATTACK";
    public static final String ATTACK_RESULT = "ATTACK_RESULT";
    public static final String GAME_START = "GAME_START";
    public static final String GAME_OVER = "GAME_OVER";
    public static final String YOUR_TURN = "YOUR_TURN";
    public static final String WAIT_TURN = "WAIT_TURN";

    // Attack results
    public static final String HIT = "HIT";
    public static final String MISS = "MISS";
    public static final String SUNK = "SUNK";
}