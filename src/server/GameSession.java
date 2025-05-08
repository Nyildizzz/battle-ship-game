package server;

import shared.Board;
import shared.Ship;
import shared.ShipType;
import shared.Packet;

import java.util.Random; // Random importu ekleyin

public class GameSession {
    private String gameId;
    private ClientHandler player1;
    private ClientHandler player2;
    private int player1Id;
    private int player2Id;
    private String player1Ships;
    private String player2Ships;
    private boolean player1Ready = false;
    private boolean player2Ready = false;
    private Server server; // Server referansı kalsın
    private int currentPlayerId; // Sırası gelen oyuncuyu takip etmek için
    private Board player1Board;
    private Board player2Board;
    private boolean gameOver = false;


    public GameSession(String gameId, ClientHandler player1, ClientHandler player2) {
        this.gameId = gameId;
        this.player1 = player1;
        this.player2 = player2;
        this.player1Board = new Board();
        this.player2Board = new Board();

    }

    public boolean hasPlayer(int playerId) {
        return playerId == player1Id || playerId == player2Id;
    }
    
    // Belirli bir ID'ye sahip ClientHandler'ı döndürür
    public ClientHandler getPlayerHandler(int playerId) {
        if (playerId == player1Id) {
            return player1;
        } else if (playerId == player2Id) {
            return player2;
        }
        return null;
    }
    
    public void setServer(Server server) {
        this.server = server;
    }
    public String getGameId() {
        return gameId;
    }
    public int getOpponentId(int playerId) {
        if (playerId == player1Id) {
            return player2Id;
        } else if (playerId == player2Id) {
            return player1Id;
        }
        return -1;
    }

    public void setPlayerIds(int player1Id, int player2Id) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
    }
    
    public boolean areBothPlayersReady() {
        return player1Ready && player2Ready;
    }

    public void setPlayerReady(int clientId, String shipPositions) {
        if (clientId == player1Id) {
            player1Ships = shipPositions;
            player1Ready = true;
            populateBoard(player1Board, shipPositions); // Board'ı doldur
            System.out.println("Game " + gameId + ": Oyuncu " + player1Id + " hazır.");
        } else if (clientId == player2Id) {
            player2Ships = shipPositions;
            player2Ready = true;
            populateBoard(player2Board, shipPositions); // Board'ı doldur
            System.out.println("Game " + gameId + ": Oyuncu " + player2Id + " hazır.");
        }
    }


    private void populateBoard(Board board, String shipPositions) {
        // Önce board'ı sıfırla
        board.resetBoard();

        System.out.println("Gemi pozisyonları parsing: " + shipPositions);

        String[] shipsData = shipPositions.split(";");
        for (String shipData : shipsData) {
            try {
                String[] parts = shipData.split(",");

                // Format kontrolü - 00,5,H,CARRIER (konum,boyut,yönelim,tip)
                if (parts.length >= 4) {
                    String positionStr = parts[0]; // 00 formatındaki konum (satır,sütun)
                    int size = Integer.parseInt(parts[1]); // Gemi boyutu
                    String orientation = parts[2]; // Yönelim (H/V)
                    String shipTypeStr = parts[3]; // Gemi tipi

                    // Konum ayrıştırma (00 -> ilk rakam satır=0, ikinci rakam sütun=0)
                    if (positionStr.length() != 2 || !Character.isDigit(positionStr.charAt(0)) || !Character.isDigit(positionStr.charAt(1))) {
                        throw new IllegalArgumentException("Geçersiz konum formatı, 00-99 formatında olmalı: " + positionStr);
                    }

                    // İlk rakam satır (row), ikinci rakam sütun (col) olarak al
                    int row = Character.getNumericValue(positionStr.charAt(0)); // İlk rakam (satır)
                    int col = Character.getNumericValue(positionStr.charAt(1)); // İkinci rakam (sütun)

                    if (row < 0 || row > 9 || col < 0 || col > 9) {
                        throw new IllegalArgumentException("Konum, tahta sınırları dışında: " + positionStr);
                    }

                    // Yönelim kontrolü
                    boolean isHorizontal = orientation.equalsIgnoreCase("H");

                    // Gemi tipi kontrolü
                    ShipType type;
                    try {
                        type = ShipType.valueOf(shipTypeStr);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Bilinmeyen gemi tipi: " + shipTypeStr + ", boyut ile eşleştirilmeye çalışılıyor.");
                        // Gemi tipini boyuttan tahmin et
                        switch (size) {
                            case 5:
                                type = ShipType.CARRIER;
                                break;
                            case 4:
                                type = ShipType.BATTLESHIP;
                                break;
                            case 3:
                                // CRUISER veya SUBMARINE olabilir, varsayılan olarak CRUISER
                                type = ShipType.CRUISER;
                                break;
                            case 2:
                                type = ShipType.DESTROYER;
                                break;
                            default:
                                throw new IllegalArgumentException("Geçersiz gemi boyutu: " + size);
                        }
                    }

                    // Gemi tipinin boyutunu kontrol et
                    if (type.getSize() != size) {
                        System.err.println("Uyarı: Gemi tipi (" + type + ") ve boyutu (" + size + ") uyuşmuyor.");
                    }

                    // Tahta sınırları kontrolü
                    if (isHorizontal && col + size > 10) {
                        System.err.println("Gemi yerleştirilemedi: " + shipTypeStr +
                                " - Konum: " + positionStr +
                                " - Yatay konumda tahtaya sığmıyor.");
                        continue;
                    } else if (!isHorizontal && row + size > 10) {
                        System.err.println("Gemi yerleştirilemedi: " + shipTypeStr +
                                " - Konum: " + positionStr +
                                " - Dikey konumda tahtaya sığmıyor.");
                        continue;
                    }

                    // Gemi nesnesini oluştur ve yerleştir
                    Ship ship = new Ship(row, col, size, isHorizontal, type);
                    boolean placed = board.placeShip(ship);

                    if (placed) {
                        System.out.println(shipTypeStr + " gemisi başarıyla yerleştirildi: " + positionStr +
                                ", " + (isHorizontal ? "Yatay" : "Dikey"));
                    } else {
                        System.err.println("Gemi yerleştirilemedi: " + shipTypeStr + " - Konum: " + positionStr +
                                " - Muhtemelen başka bir gemiyle çakışıyor veya temas ediyor.");
                    }
                } else {
                    throw new IllegalArgumentException("Geçersiz veri formatı. Beklenen format: 00,5,H,CARRIER");
                }
            } catch (NumberFormatException e) {
                System.err.println("Geçersiz sayısal değer: " + shipData + " - " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("Geçersiz gemi verisi: " + shipData + " - " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Gemi yerleştirme hatası: " + shipData + " - " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Board dolum işlemi tamamlandı.");
    }

    public void startGameLogic() {
        System.out.println("Game " + gameId + ": Her iki oyuncu da hazır. Oyun başlıyor!");

        Random random = new Random();
        boolean player1GoesFirst = random.nextBoolean();
        currentPlayerId = player1GoesFirst ? player1Id : player2Id;

        System.out.println("Game " + gameId + ": İlk hamle oyuncu " + currentPlayerId + " tarafından yapılacak.");

        ClientHandler p1Handler = getPlayerHandler(player1Id);
        ClientHandler p2Handler = getPlayerHandler(player2Id);

        if (p1Handler != null) {
            System.out.println("Game " + gameId + ": Oyuncu 1'e gemi pozisyonları gönderiliyor.");
            p1Handler.sendPacket(new Packet(player1GoesFirst ? "YOUR_TURN" : "WAIT_TURN", ""));
            p1Handler.sendPacket(new Packet("GAME_READY", player1GoesFirst ? "YOUR_TURN" : "WAIT_TURN"));
            p1Handler.sendPacket(new Packet("MY_SHIPS", player1Ships));
            p1Handler.sendPacket(new Packet("OPPONENT_SHIPS", player2Ships));
        }
        if (p2Handler != null) {
            System.out.println("Game " + gameId + ": Oyuncu 2'ye gemi pozisyonları gönderiliyor.");
            p2Handler.sendPacket(new Packet(player1GoesFirst ? "WAIT_TURN" : "YOUR_TURN", "") );
            p2Handler.sendPacket(new Packet("GAME_READY", ""));
            p2Handler.sendPacket(new Packet("MY_SHIPS", player2Ships));
            p2Handler.sendPacket(new Packet("OPPONENT_SHIPS", player1Ships));
        }
    }
    public void processFireCommand(int shooterId, int row, int col) {
        // Eğer oyun bittiyse veya sıra atış yapan oyuncuda değilse işlemi reddet
        System.out.println("Game " + gameId + ": Oyuncu " + shooterId + " atış yapmaya çalışıyor: " + row + "," + col);
        if (gameOver || currentPlayerId != shooterId) {
            ClientHandler shooter = getPlayerHandler(shooterId);
            if (shooter != null) {
                shooter.sendPacket(new Packet("ERROR", "Sıra sizde değil!"));
            }
            return;
        }

        // Atış yapan oyuncu ve hedef oyuncuyu belirle
        ClientHandler attacker = getPlayerHandler(shooterId);
        int targetId = getOpponentId(shooterId);
        ClientHandler target = getPlayerHandler(targetId);

        if (attacker == null || target == null) {
            System.err.println("GameSession: Oyuncu bulunamadı! ID: " + shooterId + " veya " + targetId);
            return;
        }

        // Atışın hangi tahtaya yapılacağını belirle
        Board targetBoard = (targetId == player1Id) ? player1Board : player2Board;

        // Daha önce atış yapılmış bir hücre mi kontrol et
        if (targetBoard.isAlreadyShot(row, col)) {
            attacker.sendPacket(new Packet("ERROR", "Bu hücreye zaten ateş edilmiş!"));
            return;
        }

        // Atışı işle (vuruş mu ıska mı?)
        boolean isHit = targetBoard.processShot(row, col);

        // Koordinatları 00 formatına çevir (0-9 formatı)
        String cellPosition = col + "" + row;

        if (isHit) {
            // İsabet - Vurulan geminin tipini ve batıp batmadığını kontrol et
            String shipType = targetBoard.getShipTypeAt(row, col);
            boolean isSunk = targetBoard.isShipSunk(row, col);

            // Atış yapana sonucu bildir
            if (isSunk) {
                attacker.sendPacket(new Packet("SHOT_RESULT", "HIT:" + cellPosition + ":" + shipType + ":SUNK"));
            } else {
                attacker.sendPacket(new Packet("SHOT_RESULT", "HIT:" + cellPosition + ":" + shipType));
            }

            // Atış yapılana sonucu bildir
            if (isSunk) {
                target.sendPacket(new Packet("OPPONENT_SHOT", cellPosition + ":HIT:" + shipType + ":SUNK"));
            } else {
                target.sendPacket(new Packet("OPPONENT_SHOT", cellPosition + ":HIT:" + shipType));
            }

            // Tüm gemiler batmış mı kontrol et (oyun sonu)
            if (targetBoard.areAllShipsSunk()) {
                gameOver = true;

                // Oyun sonu bilgilerini her iki oyuncuya da gönder
                attacker.sendPacket(new Packet("GAME_OVER", "WIN"));
                target.sendPacket(new Packet("GAME_OVER", "LOSE"));

                System.out.println("Game " + gameId + ": Oyun bitti! Kazanan: Oyuncu " + shooterId);
                return; // Oyun bitti, fonksiyonu sonlandır
            }

            // İsabet durumunda da sırayı rakibe geç
            currentPlayerId = targetId;

            // Yeni sıra bilgisini oyunculara gönder
            attacker.sendPacket(new Packet("WAIT_TURN", ""));
            target.sendPacket(new Packet("YOUR_TURN", ""));

            System.out.println("Game " + gameId + ": İsabet! Oyuncu " + shooterId +
                    " -> (" + col + "," + row + "), sıra oyuncu " + targetId + "'e geçti");
        } else {
            // Iskalama durumu
            attacker.sendPacket(new Packet("SHOT_RESULT", "MISS:" + cellPosition));
            target.sendPacket(new Packet("OPPONENT_SHOT", cellPosition + ":MISS"));

            // Iskalamadan sonra sıra rakibe geçer
            currentPlayerId = targetId;

            // Yeni sıra bilgisini oyunculara gönder
            attacker.sendPacket(new Packet("WAIT_TURN", ""));
            target.sendPacket(new Packet("YOUR_TURN", ""));

            System.out.println("Game " + gameId + ": Iskalama! Oyuncu " + shooterId +
                    " -> (" + col + "," + row + "), sıra oyuncu " + targetId + "'e geçti");
        }
    }




}