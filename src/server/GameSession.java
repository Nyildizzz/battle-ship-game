package server;

import shared.Board;
import shared.GameState;
import shared.Packet;

public class GameSession {
    private String gameId;
    private ClientHandler player1;
    private ClientHandler player2;
    private GameState gameState;

    public GameSession(String gameId, ClientHandler player1, ClientHandler player2) {
        this.gameId = gameId;
        this.player1 = player1;
        this.player2 = player2;
        this.gameState = new GameState();

        // Initialize game state
        Board player1Board = new Board();
        Board player2Board = new Board();
        gameState.addPlayer(1, player1Board);
        gameState.addPlayer(2, player2Board);
    }

    public void start() {
        // Ask players to place ships
        player1.sendPacket(new Packet("PLACE_SHIPS", ""));
        player2.sendPacket(new Packet("PLACE_SHIPS", ""));
    }

    public void handleGameMessage(int playerId, Packet packet) {
        // Handle various game messages (ship placement, attacks, etc.)
        switch (packet.getType()) {
            case "SHIPS_READY":
                handleShipsReady(playerId);
                break;

            case "ATTACK":
                handleAttack(playerId, packet.getData());
                break;
        }
    }

    private void handleShipsReady(int playerId) {
        // Logic to handle when a player has placed all ships
    }

    private void handleAttack(int playerId, String data) {
        // Logic to handle player attacks
    }
}