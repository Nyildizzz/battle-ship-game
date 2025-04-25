package client;

import shared.Board;
import shared.Packet;
import client.ui.GameFrame;


import java.sql.SQLOutput;
import java.util.Random;

public class GameClient {
    private int clientId;
    private Board playerBoard;
    private Board opponentBoard;
    private PacketHandler packetHandler;
    private boolean playerTurn;
    private int[] selectedCell;
private boolean opponentReady = false;

// Getter ve setter ekleyelim
public boolean isOpponentReady() {
    return opponentReady;
}

public void setOpponentReady(boolean ready) {
    this.opponentReady = ready;
}

    public GameClient() {
        playerBoard = new Board();
        opponentBoard = new Board();
        playerTurn = false;
    }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public Board getPlayerBoard() {
        return playerBoard;
    }

    public Board getOpponentBoard() {
        return opponentBoard;
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

    public void resetBoard() {
        playerBoard = new Board();
    }

    public void sendShipsReady(String shipPositions) {
        if (packetHandler != null) {
            packetHandler.sendMessage("SHIPS_READY", shipPositions);
        }
    }

    public void processPacket(Packet packet) {
        switch (packet.getType()) {
            case "YOUR_TURN":
                playerTurn = true;
                break;
            case "WAIT_TURN":
                playerTurn = false;
                break;
            case "SHIP_PLACEMENT":
                packetHandler.sendMessage("PLACE_SHIPS", packet.getData());
                processShipPlacements(packet.getData());
                break;
            case "GAME_READY":
                startGame();


        }
    }



private void processShipPlacements(String data) {
    System.out.println("Gemi yerleşimleri işleniyor...");
    String[] ships = data.split(";");

    for (String shipData : ships) {
        String[] parts = shipData.split(",");
        if (parts.length == 4) {
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            int size = Integer.parseInt(parts[2]);
            boolean isHorizontal = parts[3].equals("H");

            System.out.printf("Gemi yerleştiriliyor: Satır=%d, Sütun=%d, Boyut=%d, Yatay mı=%b%n",
                            row, col, size, isHorizontal);

            if (opponentBoard.placeShip(row, col, size, isHorizontal)) {
                System.out.println("Gemi başarıyla yerleştirildi!");
            } else {
                System.out.println("UYARI: Gemi yerleştirilemedi!");
            }
        }
    }
}

    private void startGame() {
        GameClient client = this;
        java.awt.EventQueue.invokeLater(() -> {
            GameFrame gameFrame = new GameFrame(client);
            gameFrame.setVisible(true);
        });
    }


    public int getClientId() {
        return clientId;
    }
}