package server;

import shared.Packet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private boolean running;

    // Oyuncuların davet durumlarını takip etmek için
    private Map<Integer, Boolean> playerInviteStatus = new ConcurrentHashMap<>();

    // Track connected clients
    private Map<Integer, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private AtomicInteger nextClientId = new AtomicInteger(1);

    // Track active games
    private Map<String, GameSession> activeGames = new ConcurrentHashMap<>();
    private AtomicInteger gameIdCounter = new AtomicInteger(1);

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            running = true;

            // Accept clients in a loop
            while (running) {
                Socket clientSocket = serverSocket.accept();
                int clientId = nextClientId.getAndIncrement();
                System.out.println("Client " + clientId + " connected");

                // Create and start client handler
                ClientHandler clientHandler = new ClientHandler(clientId, clientSocket, this);
                connectedClients.put(clientId, clientHandler);

                new Thread(clientHandler).start();

                // Send updated client list to all clients
                broadcastClientList();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    public void removeClient(int clientId) {
        System.out.println("Attempting to remove client: " + clientId);

        // Oyuncunun aktif bir oyunda olup olmadığını kontrol et
        GameSession gameSession = findGameSessionByPlayerId(clientId);

        if (gameSession != null) {
            System.out.println("Client " + clientId + " was in game " + gameSession.getGameId());
            // Oyuncu bir oyundaydı
            int opponentId = gameSession.getOpponentId(clientId);
            ClientHandler opponentHandler = gameSession.getPlayerHandler(opponentId); // GameSession'dan handler al

            if (opponentHandler != null) {
                // Rakip oyuncuya diğer oyuncunun ayrıldığını bildir
                opponentHandler.sendPacket(new Packet("OPPONENT_DISCONNECTED", "Rakibiniz oyundan ayrıldı. Oyun bitti."));
                System.out.println("Notified opponent " + opponentId + " about disconnection.");

                // Rakip oyuncuyu lobiye EKLEME. Onun da bağlantısını kapatıyoruz.
                opponentHandler.close(); // Rakibin bağlantısını da kapat
                System.out.println("Closed opponent's (" + opponentId + ") connection as game ended.");

            } else {
                System.out.println("Could not find opponent handler for client " + opponentId);
            }

            // Oyun oturumunu aktif oyunlardan kaldır
            activeGames.remove(gameSession.getGameId());
            System.out.println("Removed game session " + gameSession.getGameId());

            // Ayrılan oyuncunun ClientHandler'ı zaten soket hatası nedeniyle
            // kendi run() metodu içinde kapanacaktır. Rakibin handler'ını yukarıda kapattık.

        } else {
            System.out.println("Client " + clientId + " was not in an active game (disconnecting from lobby).");
            // Oyuncu bir oyunda değildi, normal lobi işlemleri
            ClientHandler handler = connectedClients.remove(clientId); // Lobiden çıkar
            if (handler != null) {
                // Lobiden ayrılan oyuncunun handler'ını kapat
                handler.close(); // Soketi ve stream'leri kapatır
                System.out.println("Closed handler for client " + clientId + " disconnecting from lobby.");
            } else {
                System.out.println("Handler for client " + clientId + " was already removed or null (lobby).");
            }

            // Lobi ile ilgili davet durumlarını temizle
            playerInviteStatus.remove(clientId);
            for (Map.Entry<Integer, Boolean> entry : playerInviteStatus.entrySet()) {
                if (entry.getValue()) {
                    ClientHandler otherPlayer = connectedClients.get(entry.getKey());
                    if (otherPlayer != null) {
                        otherPlayer.sendPacket(new Packet("INVITE_CANCELED", "Davet eden/edilen oyuncu çıkış yaptı."));
                    }
                    entry.setValue(false);
                }
            }
            System.out.println("Cleaned up invite status for client " + clientId);
        }

        // Sadece lobi durumu değiştiğinde listeyi yayınla
        // Eğer oyuncu oyundan ayrıldıysa, lobi listesi değişmedi (çünkü oyundaki oyuncular listede değildi)
        // Eğer oyuncu lobiden ayrıldıysa, liste değişti.
        if (gameSession == null) {
            broadcastClientList(); // Sadece lobiden ayrılma durumunda listeyi güncelle
        }

        System.out.println("Finished removing client " + clientId + " context.");
    }


    public void handleInviteCanceled(int clientId) {
        playerInviteStatus.put(clientId, false);
    }



    public void broadcastClientList() {
        StringBuilder clientList = new StringBuilder();
        for (Integer clientId : connectedClients.keySet()) {
            clientList.append(clientId).append(",");
        }

        // Remove trailing comma if exists
        if (clientList.length() > 0) {
            clientList.setLength(clientList.length() - 1);
        }

        String message = clientList.toString();
        for (ClientHandler handler : connectedClients.values()) {
            handler.sendPacket(new Packet("CLIENT_LIST", message));
        }
    }

    public void handleInvitation(int fromClientId, int toClientId) {
        ClientHandler receiver = connectedClients.get(toClientId);



        // Davet gönderen kişinin durumunu güncelle
        playerInviteStatus.put(fromClientId, true);
        // Davet alan kişinin durumunu güncelle
        playerInviteStatus.put(toClientId, true);

        if (receiver != null) {
            receiver.sendPacket(new Packet("GAME_INVITE", String.valueOf(fromClientId)));
        }
    }
    // Yeni metod ekleyin
    public void handleShipsReady(int clientId, String shipPositions) {
        GameSession session = findGameSessionByPlayerId(clientId);
        if (session != null) {
            session.setPlayerReady(clientId, shipPositions);

            // Eğer her iki oyuncu da hazırsa, oyunu başlat
            if (session.areBothPlayersReady()) {
                session.startGameLogic(); // Oyunun gerçek başlangıç mantığı
            } else {
                // Hazır olan oyuncuya beklemesini söyleyelim
                ClientHandler readyPlayer = connectedClients.get(clientId); // Hata: Oyuncular listeden çıkarılmıştı, GameSession üzerinden almalıyız.
                // Düzeltme: ClientHandler'ı GameSession'dan alalım
                ClientHandler readyPlayerHandler = session.getPlayerHandler(clientId);
                if(readyPlayerHandler != null) {
                    readyPlayerHandler.sendPacket(new Packet("WAIT_OPPONENT", "Rakibin gemilerini yerleştirmesi bekleniyor..."));
                }
            }
        } else {
            System.err.println("Client " + clientId + " için aktif oyun bulunamadı (handleShipsReady).");
        }
    }

    // Oyuncu ID'sine göre GameSession bulan yardımcı metod
    private GameSession findGameSessionByPlayerId(int playerId) {
        for (GameSession session : activeGames.values()) {
            if (session.hasPlayer(playerId)) {
                return session;
            }
        }
        return null;
    }


    public void handleInviteResponse(int fromClientId, int toClientId, boolean accepted) {
        ClientHandler sender = connectedClients.get(toClientId);
        // Her iki oyuncunun davet durumunu sıfırla
        playerInviteStatus.put(fromClientId, false);
        playerInviteStatus.put(toClientId, false);

        if (sender != null) {
            if (accepted) {
                // Create a new game session
                String gameId = "game-" + gameIdCounter.getAndIncrement();
                ClientHandler player1 = connectedClients.get(fromClientId);
                ClientHandler player2 = connectedClients.get(toClientId);

                // Oyuncuları aktif istemciler listesinden geçici olarak çıkaralım
                connectedClients.remove(fromClientId);
                connectedClients.remove(toClientId);

                // Yeni oyun oturumu oluştur
                GameSession gameSession = new GameSession(gameId, player1, player2);
                activeGames.put(gameId, gameSession);

                // Oyun bittiğinde oyuncuları tekrar listeye eklemek için bir referans kaydedelim
                gameSession.setServer(this);
                gameSession.setPlayerIds(fromClientId, toClientId);

                // Notify both players that game has started
                player1.sendPacket(new Packet("GAME_STARTED", gameId + "|1"));
                player2.sendPacket(new Packet("GAME_STARTED", gameId + "|2"));

                // Tüm istemcilere güncellenmiş listeyi gönder
                broadcastClientList();

            } else {
                // Notify that invitation was declined
                sender.sendPacket(new Packet("INVITE_DECLINED", String.valueOf(fromClientId)));
            }
        }
    }

    private void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // Close all client connections
            for (ClientHandler handler : connectedClients.values()) {
                handler.close();
            }
            connectedClients.clear();
        } catch (IOException e) {
            System.out.println("Error shutting down server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}