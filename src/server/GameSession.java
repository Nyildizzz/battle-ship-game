package server;

import shared.Board;
import shared.Packet;

public class GameSession {
    private String gameId;
    private ClientHandler player1;
    private ClientHandler player2;
    private int player1Id;
    private int player2Id;
    private boolean player1Ready = false;
    private boolean player2Ready = false;
    private Server server;

    public GameSession(String gameId, ClientHandler player1, ClientHandler player2) {
        this.gameId = gameId;
        this.player1 = player1;
        this.player2 = player2;

        // Initialize game state
        Board player1Board = new Board();
        Board player2Board = new Board();
    }

    public boolean hasPlayer(int playerId) {
        return playerId == player1Id || playerId == player2Id;
    }

    public void start() {
        // Ask players to place ships
        player1.sendPacket(new Packet("PLACE_SHIPS", ""));
        player2.sendPacket(new Packet("PLACE_SHIPS", ""));
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setPlayerIds(int player1Id, int player2Id) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
    }


}