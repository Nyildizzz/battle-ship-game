package client;

import shared.Board;
import shared.Packet;
import client.ui.GameFrame;
import shared.Ship;


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
    private GameFrame gameFrame;

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

    public GameFrame getGameFrame() {
        return gameFrame;
    }
    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
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
                System.out.println("Sıra sizde!");
                playerTurn = true;
                // UI'de oyuncunun sırasını göster
                break;
            case "WAIT_TURN":
                System.out.println("Rakibin sırası!");
                playerTurn = false;
                // UI'de rakibin sırasını göster
                break;
            case "MY_SHIPS":
                processShipPlacements(playerBoard, packet.getData());
                break;
            case "OPPONENT_SHIPS":
                processShipPlacements(opponentBoard, packet.getData());
                break;
            case "GAME_READY":
                startGame();


        }
    }



    private void processShipPlacements(Board targetBoard, String data) {
        if (data == null || data.isEmpty()) {
            System.out.println("Gemi yerleştirme verisi boş.");
            return;
        }
        targetBoard.resetBoard(); // Yerleştirmeden önce tahtayı temizle (varsa)

        String[] shipsData = data.split(";");

        for (String shipData : shipsData) {
            String[] parts = shipData.split(",");
            if (parts.length == 4) {
                try {
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    int size = Integer.parseInt(parts[2]);
                    boolean isHorizontal = parts[3].equalsIgnoreCase("H"); // Büyük/küçük harf duyarsız



                    // 1. Ship nesnesi oluştur
                    Ship newShip = new Ship(size);
                    // 2. Pozisyon ve yönelimi ayarla
                    newShip.setPosition(row, col);
                    newShip.setOrientation(isHorizontal);

                    // 3. Board'a Ship nesnesini kullanarak yerleştir
                    if (targetBoard.placeShip(newShip)) { // Değişiklik burada
                    } else {
                        System.err.println("  -> UYARI: Gemi yerleştirilemedi! Çakışma veya sınır dışı.");
                        // Hata durumunda ne yapılacağına karar verilmeli.
                        // Belki sunucuya hata mesajı gönderilebilir veya oyun başlatılamaz.
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Hata: Geçersiz gemi verisi: " + shipData + " - " + e.getMessage());
                }
            } else {
                System.err.println("Hata: Eksik veya hatalı gemi verisi formatı: " + shipData);
            }
        }
        System.out.println("Gemi yerleştirme işlemi tamamlandı.");
    }



    public void sendFireCommand(int row, int col) {
        if (packetHandler != null) {
            String command = row + "," + col;
            packetHandler.sendMessage("FIRE", command);
        }
    }

    private void startGame() {
        GameClient client = this;
        java.awt.EventQueue.invokeLater(() -> {
            GameFrame gameFrame = new GameFrame(client);
            this.setGameFrame(gameFrame);
            gameFrame.setVisible(true);
        });
    }






    public int getClientId() {
        return clientId;
    }
}