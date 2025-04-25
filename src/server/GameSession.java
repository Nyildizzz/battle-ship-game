package server;

import shared.Board; // Board kullanılıyorsa import kalsın
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


    public GameSession(String gameId, ClientHandler player1, ClientHandler player2) {
        this.gameId = gameId;
        this.player1 = player1;
        this.player2 = player2;

        // Initialize game state (Varsa Board vs.)
        // Board player1Board = new Board();
        // Board player2Board = new Board();
    }

    // Oyuncu ID'sinin bu session'da olup olmadığını kontrol eder
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

    // Mevcut start() metodunu kaldırabilir veya sadece loglama için bırakabilirsiniz.
    // Oyun başlangıcı artık SHIPS_READY ile tetiklenecek.
    /*
    public void start() {
        // Eskiden burada PLACE_SHIPS gönderiliyordu, artık Server.handleInviteResponse içinde yapılıyor.
        System.out.println("GameSession " + gameId + " başlatıldı, gemi yerleştirme bekleniyor.");
    }
    */

    public void setServer(Server server) {
        this.server = server;
    }

    public void setPlayerIds(int player1Id, int player2Id) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
    }
    
    // Her iki oyuncunun da hazır olup olmadığını kontrol eder
    public boolean areBothPlayersReady() {
        return player1Ready && player2Ready;
    }

    // Oyuncunun hazır olduğunu ve gemi pozisyonlarını ayarlar
    public void setPlayerReady(int clientId, String shipPositions) {
        if (clientId == player1Id) {
            player1Ships = shipPositions;
            player1Ready = true;
            System.out.println("Game " + gameId + ": Oyuncu " + player1Id + " hazır.");
        } else if (clientId == player2Id) {
            player2Ships = shipPositions;
            player2Ready = true;
            System.out.println("Game " + gameId + ": Oyuncu " + player2Id + " hazır.");
        }
    }

    // YENİ METOD: Oyunun gerçek başlangıç mantığı
    public void startGameLogic() {
        System.out.println("Game " + gameId + ": Her iki oyuncu da hazır. Oyun başlıyor!");

        // İlk sırayı rastgele belirle
        Random random = new Random();
        boolean player1GoesFirst = random.nextBoolean();
        currentPlayerId = player1GoesFirst ? player1Id : player2Id;

        System.out.println("Game " + gameId + ": İlk hamle oyuncu " + currentPlayerId + " tarafından yapılacak.");

        // Oyunculara oyunun başladığını ve kimin ilk sırada olduğunu bildir
        ClientHandler p1Handler = getPlayerHandler(player1Id);
        ClientHandler p2Handler = getPlayerHandler(player2Id);

        if (p1Handler != null) {
            p1Handler.sendPacket(new Packet("GAME_READY", player1GoesFirst ? "YOUR_TURN" : "WAIT_TURN"));
        }
        if (p2Handler != null) {
            p2Handler.sendPacket(new Packet("GAME_READY", player1GoesFirst ? "WAIT_TURN" : "YOUR_TURN"));
        }
    }



}