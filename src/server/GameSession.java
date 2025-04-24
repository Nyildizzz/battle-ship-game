package server;

import shared.Board;
import shared.GameState;
import shared.Packet;

public class GameSession {
    private String gameId;
    private ClientHandler player1;
    private ClientHandler player2;
    private GameState gameState;
    private int player1Id;
    private int player2Id;
    private boolean player1Ready = false;
    private boolean player2Ready = false;
    private Server server;

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
    public void handlePlayerReady(ClientHandler player) {
        if (player == player1) {
            player1Ready = true;
        } else if (player == player2) {
            player2Ready = true;
        }

        // İki oyuncu da hazır mı kontrol et
        if (player1Ready && player2Ready) {
            // Oyunu başlat
            startActualGame();
        }
    }
    private void startActualGame() {
        // İki oyuncuya da oyunun başladığını bildir
        player1.sendPacket(new Packet("START_ACTUAL_GAME", ""));
        player2.sendPacket(new Packet("START_ACTUAL_GAME", ""));
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

    // Oyun bittiğinde çağrılacak metot
    public void gameEnded() {
        // Oyuncuları tekrar online listeye ekle
        if (server != null) {
            server.addClientBack(player1Id, player1);
            server.addClientBack(player2Id, player2);
        }

        // Aktif oyunlar listesinden bu oyunu çıkar
        if (server != null) {
            server.removeGame(gameId);
        }
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
    public void handleGameEnd(int winnerId) {
        // Kazanan oyuncuyu bildir
        player1.sendPacket(new Packet("GAME_OVER", String.valueOf(winnerId)));
        player2.sendPacket(new Packet("GAME_OVER", String.valueOf(winnerId)));

        // Oyunu sonlandır ve oyuncuları tekrar online listeye ekle
        gameEnded();
    }

}