package client;

import shared.Board;
import client.ui.ShipPlacementFrame;
import client.ui.LobbyFrame;
import shared.Packet;
import client.ui.GameFrame;
import shared.Ship;
import shared.ShipType;



public class GameClient{
    private int clientId;
    private Board playerBoard;
    private Board opponentBoard;
    private PacketHandler packetHandler;
    private boolean playerTurn;
    private GameFrame gameFrame;
    private ShipPlacementFrame shipPlacementFrame;
    private boolean rematchOffered;
    private LobbyFrame lobbyFrame;


    public GameClient() {
        playerBoard = new Board();
        opponentBoard = new Board();
        playerTurn = false;
        rematchOffered = false;

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

    public GameFrame getGameFrame() {
        return gameFrame;
    }
    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }

    public boolean setRematchOffered(boolean rematchOffered) {
        this.rematchOffered = rematchOffered;
        return this.rematchOffered;
    }
    public boolean isRematchOffered() {
        return rematchOffered;
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
                setRematchOffered(false);
                processGameOver(packet.getData());
                break;
            case "REMATCH_OFFER":
                int fromPlayerId = Integer.parseInt(packet.getData());
                processRematchOffer(fromPlayerId);
                break;
            case "REMATCH_ACCEPTED":
                setRematchOffered(true);
                System.out.println("Yeniden oyun isteği kabul edildi.");
                showShipPlacementFrame(packet.getData());
                break;
            case "REMATCH_REJECTED":
                // Sunucu yeniden oyun isteğini reddettiyse
                System.out.println("Yeniden oyun isteği reddedildi: " + packet.getData());
                // Uyarı göster
                if (gameFrame != null) {
                    gameFrame.showError("Yeniden oyun isteği reddedildi: " + packet.getData());
                    gameFrame.dispose();
                    System.exit(0);
                }
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
            try {
                int row, col, size;
                boolean isHorizontal;
                ShipType shipType = null;

                // Sadece 00,5,H,CARRIER formatını destekle
                if (parts.length >= 4 && parts[0].length() == 2 &&
                        Character.isDigit(parts[0].charAt(0)) && Character.isDigit(parts[0].charAt(1))) {
                    // 00 formatı: konum,boyut,yönelim,tip
                    String positionStr = parts[0];

                    // İlk rakam satır (row), ikinci rakam sütun (col)
                    row = Character.getNumericValue(positionStr.charAt(0)); // İlk rakam (satır)
                    col = Character.getNumericValue(positionStr.charAt(1)); // İkinci rakam (sütun)

                    // Diğer değerler
                    size = Integer.parseInt(parts[1]);
                    isHorizontal = parts[2].equalsIgnoreCase("H");

                    // Gemi tipi
                    if (parts.length >= 4) {
                        try {
                            shipType = ShipType.valueOf(parts[3]);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Tanınmayan gemi tipi: " + parts[3] + ", boyut ile eşleştirme deneniyor.");
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Geçersiz veri formatı. Beklenen format: 00,5,H,CARRIER");
                }

                // Gemi tipini boyutla eşleştir (eğer belirtilmemişse)
                if (shipType == null) {
                    for (ShipType type : ShipType.values()) {
                        if (type.getSize() == size) {
                            shipType = type;
                            break;
                        }
                    }
                }

                if (shipType == null) {
                    throw new IllegalArgumentException("Geçersiz gemi boyutu: " + size);
                }

                // Koordinat sınır kontrolü
                if (row < 0 || row > 9 || col < 0 || col > 9) {
                    throw new IllegalArgumentException("Geçersiz koordinatlar: (" + row + "," + col + ")");
                }

                Ship newShip = new Ship(row, col, size, isHorizontal, shipType);

                // Board'a Ship nesnesini kullanarak yerleştir
                if (targetBoard.placeShip(newShip)) {
                    System.out.println("  -> " + shipType + " gemisi başarıyla yerleştirildi: (" + row + "," + col + ")" +
                            ", " + (isHorizontal ? "Yatay" : "Dikey"));
                } else {
                    System.err.println("  -> UYARI: Gemi yerleştirilemedi! Çakışma veya sınır dışı: (" + row + "," + col + ")");
                }
            } catch (NumberFormatException e) {
                System.err.println("Hata: Geçersiz gemi verisi: " + shipData + " - " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("Hata: Geçersiz gemi verisi: " + shipData + " - " + e.getMessage());
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Hata: Dizi sınırları dışında erişim: " + shipData);
            } catch (Exception e) {
                System.err.println("Hata: Gemi işleme hatası: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Gemi yerleştirme işlemi tamamlandı.");
    }

    private void processShotResult(String data) {
        // Atış sonucunu işle
        String[] parts = data.split(":");
        String result = parts[0]; // "HIT" veya "MISS"
        String cellPosition = parts[1]; // "00" formatında (sütun,satır)

        // Hücre pozisyonunu satır ve sütun değerlerine dönüştür
        int col = Character.getNumericValue(cellPosition.charAt(0));
        int row = Character.getNumericValue(cellPosition.charAt(1));

        String logMessage = "";
        System.out.println("Atış sonucu: " + result + " - (" + row + "," + col + ")");
        if (result.equals("HIT")) {
            String shipType = parts[2]; // Vurulan gemi tipi
            boolean isSunk = parts.length > 3 && parts[3].equals("SUNK");

            // İstatistik güncelleme - isabet
            if (gameFrame != null) {
                gameFrame.recordHit(true); // Oyuncunun isabeti
            }

            // Gemi boyutunu ekle (mesaj içeriğini zenginleştir)
            int shipSize = getShipSize(shipType);

            // Rakip tahtasında hücreyi "vuruldu" olarak işaretle
            opponentBoard.markCellAsHit(row, col);

            if (isSunk) {
                System.out.println(shipType + " gemisini batırdınız! (Boyut: " + shipSize + ")");
                if (gameFrame != null) {
                    gameFrame.showMessage(shipType + " gemisini batırdınız! (Boyut: " + shipSize + ")");
                }
            } else {
                System.out.println("İsabet!");
                if (gameFrame != null) {
                    gameFrame.showMessage("İsabet!");
                }
            }
        } else { // MISS
            // İstatistik güncelleme - ıska
            if (gameFrame != null) {
                gameFrame.recordMiss(true); // Oyuncunun ıskaları
            }

            // Rakip tahtasında hücreyi "ıska" olarak işaretle
            opponentBoard.markCellAsMiss(row, col);
            System.out.println("Iskaladınız: (" + row + "," + col + ")");
            if (gameFrame != null) {
                gameFrame.showMessage("Iskaladınız: (" + row + "," + col + ")");
            }
        }

        // Sırayı karşı tarafa geç (her türlü)
        playerTurn = false;
        if (gameFrame != null) {
            gameFrame.updateTurnStatusUI(playerTurn);
        }

        // Tahtaları güncelle
        if (gameFrame != null) {
            gameFrame.updateBoards();
        }
    }

    private void processOpponentShot(String data) {
        // Rakibin atış sonucunu işle
        String[] parts = data.split(":");
        String cellPosition = parts[0]; // "00" formatında (sütun,satır)
        String result = parts[1]; // "HIT" veya "MISS"

        // Hücre pozisyonunu satır ve sütun değerlerine dönüştür
        int col = Character.getNumericValue(cellPosition.charAt(0)); // İlk rakam sütun
        int row = Character.getNumericValue(cellPosition.charAt(1)); // İkinci rakam satır

        if (result.equals("HIT")) {
            String shipType = parts[2]; // Vurulan gemi tipi
            boolean isSunk = parts.length > 3 && parts[3].equals("SUNK");

            // İstatistik güncelleme - rakip isabet
            if (gameFrame != null) {
                gameFrame.recordHit(false); // Rakibin isabeti
            }

            // Gemi boyutunu ekle
            int shipSize = getShipSize(shipType);

            // Kendi tahtamızda hücreyi "vuruldu" olarak işaretle
            playerBoard.markCellAsHit(row, col);

            if (isSunk) {
                System.out.println("Rakip " + shipType + " geminizi batırdı! (Boyut: " + shipSize + ")");
                if (gameFrame != null) {
                    gameFrame.showMessage("Rakip geminizi batırdı!");
                }
            } else {
                System.out.println("Rakip " + shipType + " geminize isabet ettirdi! (Boyut: " + shipSize + ")");
                if (gameFrame != null) {
                    gameFrame.showMessage("Rakip geminize isabet ettirdi!");
                }
            }
        } else { // MISS
            // İstatistik güncelleme - rakip ıska
            if (gameFrame != null) {
                gameFrame.recordMiss(false); // Rakibin ıskaları
            }

            // Kendi tahtamızda hücreyi "ıska" olarak işaretle
            playerBoard.markCellAsMiss(row, col);
            System.out.println("Rakip (" + row + "," + col + ") koordinatına ateş etti ve ıskaladı.");
            if (gameFrame != null) {
                gameFrame.showMessage("Rakip (" + row + "," + col + ") koordinatına ateş etti ve ıskaladı.");
            }
        }

        // Sırayı size al (her türlü)
        playerTurn = true;
        if (gameFrame != null) {
            gameFrame.updateTurnStatusUI(playerTurn);
        }

        // Tahtaları güncelle
        if (gameFrame != null) {
            gameFrame.updateBoards();
        }
    }

    public void sendFireCommand(int row, int col) {
        if (packetHandler != null) {
            // Yeni format kullanımı: 0-9 formatında doğrudan koordinatları gönder
            String command = row + "," + col; // İlk satır, sonra sütun

            System.out.println("Ateş ediliyor: (" + row + "," + col + ")");

            packetHandler.sendMessage("FIRE", command);
        }
    }

// Gemi boyutunu döndüren yardımcı metot
private int getShipSize(String shipType) {
    try {
        ShipType type = ShipType.valueOf(shipType);
        return type.getSize();
    } catch (IllegalArgumentException e) {
        return 0; // Tip bulunamazsa
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
    public void showShipPlacementFrame(String game) {
        if (shipPlacementFrame != null) {
            shipPlacementFrame.dispose();
        }
        shipPlacementFrame = new ShipPlacementFrame(this);
        shipPlacementFrame.setTitle("Battleship -" + game + " - Player " + clientId);
        shipPlacementFrame.setVisible(true);

        packetHandler.sendMessage("SHIP_PLACEMENT_REQUEST", Integer.toString(clientId));
    }
    public void requestRematch() {

        // Önce varsa GameFrame'i kapat
        if (gameFrame != null) {
            gameFrame.dispose();
            gameFrame = null;
        }

        // Tahtaları sıfırla
        playerBoard.resetBoard();
        opponentBoard.resetBoard();

        // Sunucuya yeniden oyun isteği gönder
        if (packetHandler != null) {
            packetHandler.sendMessage("REMATCH_REQUEST", Integer.toString(clientId));
        }

    }
    private void processRematchOffer(int fromPlayerId) {
        if (gameFrame != null) {
            int response = javax.swing.JOptionPane.showConfirmDialog(
                    gameFrame,
                    "Rakip yeniden oynamak istiyor. Kabul ediyor musunuz?",
                    "Yeniden Oyun Teklifi",
                    javax.swing.JOptionPane.YES_NO_OPTION
            );

            boolean accepted = response == javax.swing.JOptionPane.YES_OPTION;

            // Yanıtı sunucuya gönder
            if (packetHandler != null) {
                packetHandler.sendMessage("REMATCH_RESPONSE", fromPlayerId + "|" + accepted);
            }

            // Kabul edildiyse gameFrame'i kapat (yeni ekran açılacak)
            if (accepted && gameFrame != null) {
                gameFrame.dispose();
                gameFrame = null;
            }
        }
    }
    public void sendGameOver() {
        if (packetHandler != null) {
            packetHandler.sendMessage("GAME_OVER", Integer.toString(clientId));
        }
    }



    private void startGame() {
        if(isRematchOffered()){
            shipPlacementFrame.dispose();
        }
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