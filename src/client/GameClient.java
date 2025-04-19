package client;

import shared.Board;
import shared.GameState;
import shared.Packet;

import java.util.Random;

public class GameClient {
    private Board playerBoard;
    private Board opponentBoard;
    private GameState gameState;
    private PacketHandler packetHandler;
    private boolean playerTurn;
    private int[] selectedCell;

    public GameClient() {
        playerBoard = new Board();
        opponentBoard = new Board();
        gameState = new GameState();
        playerTurn = false;
    }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    public Board getPlayerBoard() {
        return playerBoard;
    }

    public Board getOpponentBoard() {
        return opponentBoard;
    }

    public GameState getGameState() {
        return gameState;
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(boolean playerTurn) {
        this.playerTurn = playerTurn;
    }

    public void setSelectedCell(int x, int y) {
        this.selectedCell = new int[]{x, y};
    }

    public int[] getSelectedCell() {
        return selectedCell;
    }

    public boolean placeShip(int x, int y, int size, boolean horizontal) {
        return playerBoard.placeShip(x, y, size, horizontal);
    }

    public boolean randomShipPlacement() {
        // Clear the board first
        resetBoard();

        Random random = new Random();
        int[] shipSizes = {5, 4, 3, 3, 2};

        for (int size : shipSizes) {
            boolean placed = false;
            int maxAttempts = 100;
            int attempts = 0;

            while (!placed && attempts < maxAttempts) {
                int x = random.nextInt(10);
                int y = random.nextInt(10);
                boolean horizontal = random.nextBoolean();

                if (playerBoard.placeShip(x, y, size, horizontal)) {
                    placed = true;
                }

                attempts++;
            }

            if (!placed) {
                // If we couldn't place a ship after max attempts, reset and try again
                resetBoard();
                return false;
            }
        }

        return true;
    }

    public void resetBoard() {
        playerBoard = new Board();
    }

    public void sendShipPlacement() {
        if (packetHandler != null) {
            packetHandler.sendMessage("SHIPS_READY", "");
        }
    }

    public void sendAttack(int x, int y) {
        if (packetHandler != null && playerTurn) {
            packetHandler.sendMessage("ATTACK", x + "," + y);
        }
    }

    public void processPacket(Packet packet) {
        switch (packet.getType()) {
            case "GAME_START":
                playerTurn = packet.getData().equals("YOUR_TURN");
                break;

            case "YOUR_TURN":
                playerTurn = true;
                break;

            case "WAIT_TURN":
                playerTurn = false;
                break;

            case "ATTACK_RESULT":
                processAttackResult(packet.getData());
                break;

            case "OPPONENT_ATTACK":
                processOpponentAttack(packet.getData());
                break;

            case "GAME_OVER":
                gameState.setGameOver(true);
                gameState.setWinner(packet.getData().equals("WIN") ? 1 : 2);
                break;
        }
    }

    private void processAttackResult(String data) {
        String[] parts = data.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        String result = parts[2];

        if (result.equals("HIT")) {
            opponentBoard.markHit(x, y);
        } else if (result.equals("MISS")) {
            opponentBoard.markMiss(x, y);
        }
    }

    private void processOpponentAttack(String data) {
        String[] parts = data.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
    }
}