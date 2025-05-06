package client;

import shared.Board;
import shared.Packet;
import client.ui.GameFrame;
import shared.Ship;
import shared.ShipType;



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



    public void processPacket(Packet packet) {
        switch (packet.getType()) {
            case "YOUR_TURN":
                System.out.println("Sıra sizde!");
                playerTurn = true;
                if (gameFrame != null) {
                    gameFrame.updateTurnStatusUI(playerTurn);
                }
                break;
            case "WAIT_TURN":
                System.out.println("Rakibin sırası!");
                playerTurn = false;
                if (gameFrame != null) {
                    gameFrame.updateTurnStatusUI(playerTurn);
                }
                break;
            case "MY_SHIPS":
                processShipPlacements(playerBoard, packet.getData());
                break;
            case "OPPONENT_SHIPS":
                processShipPlacements(opponentBoard, packet.getData());
                break;
            case "GAME_READY":
                startGame();
                break;
            case "SHOT_RESULT":
                processShotResult(packet.getData());
                break;
            case "OPPONENT_SHOT":
                processOpponentShot(packet.getData());
                break;
            case "ERROR":
                showError(packet.getData());
                break;
            case "GAME_OVER":
                processGameOver(packet.getData());
                break;



        }
    }

    public void sendShipsReady(String shipPositions) {
        if (packetHandler != null) {
            packetHandler.sendMessage("SHIPS_READY", shipPositions);
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
                    boolean isHorizontal = parts[3].equalsIgnoreCase("H");

                    ShipType shipType = null;
                    for (ShipType type : ShipType.values()) {
                        if (type.getSize() == size) {
                            shipType = type;
                            break;
                        }
                    }

                    if (shipType == null) {
                        throw new IllegalArgumentException("Geçersiz gemi boyutu: " + size);
                    }

                    Ship newShip = new Ship(row, col, size, isHorizontal, shipType);

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
    private void processShotResult(String data) {
        // Atış sonucunu işle
        String[] parts = data.split(":");
        String result = parts[0]; // "HIT" veya "MISS"
        String cellPosition = parts[1]; // "A1" formatında

        // Hücre pozisyonunu satır ve sütun değerlerine dönüştür
        int row = cellPosition.charAt(0) - 'A';
        int col = Integer.parseInt(cellPosition.substring(1)) - 1;

        String logMessage = "";

        if (result.equals("HIT")) {
            String shipType = parts[2]; // Vurulan gemi tipi
            boolean isSunk = parts.length > 3 && parts[3].equals("SUNK");

            // Rakip tahtasında hücreyi "vuruldu" olarak işaretle
            opponentBoard.markCellAsHit(row, col);

            if (isSunk) {
                System.out.println(shipType + " gemisini batırdınız!");
                if (gameFrame != null) {
                    gameFrame.showMessage(shipType + " gemisini batırdınız!");
                }
            } else {
                System.out.println("İsabet! " + shipType + " gemisine vurdunuz.");
                if (gameFrame != null) {
                    gameFrame.showMessage("İsabet! " + shipType + " gemisine vurdunuz.");
                }
            }
        } else { // MISS
            // Rakip tahtasında hücreyi "ıska" olarak işaretle
            opponentBoard.markCellAsMiss(row, col);
            System.out.println("Iskaladınız: " + cellPosition);
            if (gameFrame != null) {
                gameFrame.showMessage("Iskaladınız: " + cellPosition);
            }
        }

        // Tahtaları güncelle
        if (gameFrame != null) {
            gameFrame.updateBoards();
        }
    }

    private void processOpponentShot(String data) {
        // Rakibin atış sonucunu işle
        String[] parts = data.split(":");
        String cellPosition = parts[0]; // "A1" formatında
        String result = parts[1]; // "HIT" veya "MISS"

        // Hücre pozisyonunu satır ve sütun değerlerine dönüştür
        int row = cellPosition.charAt(0) - 'A';
        int col = Integer.parseInt(cellPosition.substring(1)) - 1;

        if (result.equals("HIT")) {
            String shipType = parts[2]; // Vurulan gemi tipi
            boolean isSunk = parts.length > 3 && parts[3].equals("SUNK");

            // Kendi tahtamızda hücreyi "vuruldu" olarak işaretle
            playerBoard.markCellAsHit(row, col);

            if (isSunk) {
                System.out.println("Rakip " + shipType + " geminizi batırdı!");
                if (gameFrame != null) {
                    gameFrame.showMessage("Rakip " + shipType + " geminizi batırdı!");
                }
            } else {
                System.out.println("Rakip " + shipType + " geminize isabet ettirdi!");
                if (gameFrame != null) {
                    gameFrame.showMessage("Rakip " + shipType + " geminize isabet ettirdi!");
                }
            }
        } else { // MISS
            // Kendi tahtamızda hücreyi "ıska" olarak işaretle
            playerBoard.markCellAsMiss(row, col);
            System.out.println("Rakip " + cellPosition + " koordinatına ateş etti ve ıskaladı.");
            if (gameFrame != null) {
                gameFrame.showMessage("Rakip " + cellPosition + " koordinatına ateş etti ve ıskaladı.");
            }
        }

        // Tahtaları güncelle
        if (gameFrame != null) {
            gameFrame.updateBoards();
        }
    }

    private void showError(String errorMessage) {
        System.err.println("Hata: " + errorMessage);
        if (gameFrame != null) {
            gameFrame.showError(errorMessage);
        }
    }

    private void processGameOver(String result) {
        boolean isWin = result.equals("WIN");

        if (isWin) {
            System.out.println("Tebrikler! Oyunu kazandınız!");
            if (gameFrame != null) {
                gameFrame.showGameOver(true);
            }
        } else {
            System.out.println("Maalesef kaybettiniz.");
            if (gameFrame != null) {
                gameFrame.showGameOver(false);
            }
        }

        // Oyunu sıfırla veya başka bir işlem yap
        playerTurn = false;
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
            gameFrame.updateTurnStatusUI(playerTurn);
            gameFrame.setVisible(true);
        });
    }






    public int getClientId() {
        return clientId;
    }
}