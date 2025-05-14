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

            while (running) {
                Socket clientSocket = serverSocket.accept();
                int clientId = nextClientId.getAndIncrement();
                System.out.println("Client " + clientId + " connected");

                ClientHandler clientHandler = new ClientHandler(clientId, clientSocket, this);
                connectedClients.put(clientId, clientHandler);

                new Thread(clientHandler).start();

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

        GameSession gameSession = findGameSessionByPlayerId(clientId);

        if (gameSession != null) {
            System.out.println("Client " + clientId + " was in game " + gameSession.getGameId());
            int opponentId = gameSession.getOpponentId(clientId);
            ClientHandler opponentHandler = gameSession.getPlayerHandler(opponentId);

            if (opponentHandler != null) {
                opponentHandler.sendPacket(new Packet("OPPONENT_DISCONNECTED", "Rakibiniz oyundan ayrıldı. Oyun bitti."));
                System.out.println("Notified opponent " + opponentId + " about disconnection.");
            } else {
                System.out.println("Could not find opponent handler for client " + opponentId);
            }

            // Hem çıkan oyuncuyu hem de rakibinin bağlantısını kapat
            connectedClients.remove(clientId);

            // Rakibin ClientHandler'ını kapat (oyundan çık ama bağlantıyı kesme)
            if (opponentHandler != null) {
                System.out.println("Cleaning up game resources for opponent " + opponentId);
            }

            activeGames.remove(gameSession.getGameId());
            System.out.println("Removed game session " + gameSession.getGameId());
        } else {
            System.out.println("Client " + clientId + " was not in an active game (disconnecting from lobby).");
            ClientHandler handler = connectedClients.remove(clientId);
            if (handler != null) {
                handler.close();
                System.out.println("Closed handler for client " + clientId + " disconnecting from lobby.");
            } else {
                System.out.println("Handler for client " + clientId + " was already removed or null (lobby).");
            }

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

        // Oyuncu bağlantısını kapat (eğer hala connectedClients içinde varsa)
        if (connectedClients.containsKey(clientId)) {
            ClientHandler handler = connectedClients.remove(clientId);
            if (handler != null) {
                handler.close();
            }
        }

        System.out.println("Removing client " + clientId + " from connected clients.");
        broadcastClientList();

        System.out.println("Finished removing client " + clientId + " context.");
    }


    public void handleInviteCanceled(int clientId) {
        playerInviteStatus.put(clientId, false);
    }



    public void broadcastClientList() {
        StringBuilder clientList = new StringBuilder();
        for (Integer clientId : connectedClients.keySet()) {
            // Check if the client is in an active game
            GameSession session = findGameSessionByPlayerId(clientId);
            if (session == null || session.isGameOver()) {
                // Only add clients who are not in active games
                clientList.append(clientId).append(",");
            }
        }

        if (clientList.length() > 0) {
            clientList.setLength(clientList.length() - 1);
        }

        String message = clientList.toString();
        for (ClientHandler handler : connectedClients.values()) {
            handler.sendPacket(new Packet("CLIENT_LIST", message));
        }
        System.out.println("Broadcasting client list: " + message);
    }

    public void handleInvitation(int fromClientId, int toClientId) {
        ClientHandler sender = connectedClients.get(fromClientId);
        ClientHandler receiver = connectedClients.get(toClientId);

        // Davet edilen oyuncunun zaten aktif bir oyunda olup olmadığını kontrol et
        GameSession receiverSession = findGameSessionByPlayerId(toClientId);
        if (receiverSession != null && !receiverSession.isGameOver()) {
            // Davet edilen oyuncu zaten oyunda, davet eden oyuncuya bildir
            if (sender != null) {
                sender.sendPacket(new Packet("INVITE_ERROR", "Davet ettiğiniz oyuncu şu anda başka bir oyunda."));
            }
            return;
        }

        // Davet eden oyuncunun zaten aktif bir oyunda olup olmadığını kontrol et
        GameSession senderSession = findGameSessionByPlayerId(fromClientId);
        if (senderSession != null && !senderSession.isGameOver()) {
            // Davet eden oyuncu zaten oyunda, kendisine bildir
            if (sender != null) {
                sender.sendPacket(new Packet("INVITE_ERROR", "Aktif bir oyununuz varken başka bir oyuncuyu davet edemezsiniz."));
            }
            return;
        }

        // Uygun durumda ise davet işlemine devam et
        playerInviteStatus.put(fromClientId, true);
        playerInviteStatus.put(toClientId, true);

        if (receiver != null) {
            receiver.sendPacket(new Packet("GAME_INVITE", String.valueOf(fromClientId)));
        }
    }

    public void handleShipsReady(int clientId, String shipPositions) {
        GameSession session = findGameSessionByPlayerId(clientId);
        if (session != null) {
            session.setPlayerReady(clientId, shipPositions);

            if (session.areBothPlayersReady()) {
                session.startGameLogic();

                // Oyun başladığında client listesini güncelle
                broadcastClientList();
            } else {
                ClientHandler readyPlayerHandler = session.getPlayerHandler(clientId);
                if(readyPlayerHandler != null) {
                    readyPlayerHandler.sendPacket(new Packet("WAIT_OPPONENT", "Rakibin gemilerini yerleştirmesi bekleniyor..."));
                }
            }
        } else {
            System.err.println("Client " + clientId + " için aktif oyun bulunamadı (handleShipsReady).");
        }
    }

    public GameSession findGameSessionByPlayerId(int playerId) {
        for (GameSession session : activeGames.values()) {
            if (session.hasPlayer(playerId)) {
                return session;
            }
        }
        return null;
    }

    public void handleGameOver(int clientId, int winnerId) {
        GameSession session = findGameSessionByPlayerId(clientId);
        if (session != null) {
            ClientHandler winnerHandler = session.getPlayerHandler(winnerId);
            ClientHandler loserHandler = session.getPlayerHandler(clientId);

            // Oyun oturumunu aktif oyunlardan kaldır
            activeGames.remove(session.getGameId());
        }
        broadcastClientList();
    }
    public void handleOpponentLeft(int clientId, int opponentId) {
        GameSession session = findGameSessionByPlayerId(clientId);
        if (session != null) {
            ClientHandler opponentHandler = session.getPlayerHandler(opponentId);
            if (opponentHandler != null) {
                opponentHandler.sendPacket(new Packet("OPPONENT_LEFT", "Rakibiniz oyundan ayrıldı."));
                System.out.println("Rakip oyuncu " + opponentId + " oyundan ayrıldı.");
            }

            // Oyun oturumunu aktif oyunlardan kaldır
            activeGames.remove(session.getGameId());

            // Client listesini güncelle
            broadcastClientList();
        }
    }

    public void handleRematchRequest(int requestingPlayerId, int clientId) {
        // Önce ilgili oyun oturumunu bul
        GameSession session = findGameSessionByPlayerId(clientId);

        if (session != null) {
            // Rakibi bul
            int opponentId = session.getOpponentId(clientId);
            ClientHandler opponent = session.getPlayerHandler(opponentId);

            if (opponent != null) {
                // Rakibe yeniden oyun isteği gönder
                opponent.sendPacket(new Packet("REMATCH_OFFER", String.valueOf(clientId)));
                System.out.println("Game " + session.getGameId() + ": Oyuncu " + clientId +
                        " yeniden oyun teklif etti, Oyuncu " + opponentId + "'e soruluyor.");
            } else {
                // Rakip bulunamadı, isteği gönderene bilgi ver
                ClientHandler requester = session.getPlayerHandler(clientId);
                if (requester != null) {
                    requester.sendPacket(new Packet("REMATCH_REJECTED", "Rakip artık bağlı değil."));
                }
            }
        } else {
            // Oyun oturumu bulunamadı
            ClientHandler requester = connectedClients.get(clientId);
            if (requester != null) {
                requester.sendPacket(new Packet("REMATCH_REJECTED", "Aktif bir oyun bulunamadı."));
            }
        }
    }

    // Yeniden oyun yanıtını işleyen metot
    public void handleRematchResponse(int fromClientId, int toClientId, boolean accepted) {
        GameSession oldSession = findGameSessionByPlayerId(fromClientId);

        if (oldSession != null && oldSession.hasPlayer(toClientId)) {
            if (accepted) {
                // Yeni bir oyun oturumu oluştur
                String gameId = "game-" + gameIdCounter.getAndIncrement();
                ClientHandler player1 = oldSession.getPlayerHandler(fromClientId);
                ClientHandler player2 = oldSession.getPlayerHandler(toClientId);

                // Eski oturumu aktif oyunlardan kaldır
                activeGames.remove(oldSession.getGameId());

                // Yeni oyun oturumu oluştur
                GameSession newSession = new GameSession(gameId, player1, player2);
                activeGames.put(gameId, newSession);

                // Sunucu referansını ayarla ve oyuncu ID'lerini belirle
                newSession.setServer(this);
                newSession.setPlayerIds(fromClientId, toClientId);

                // Her iki oyuncuya da yeni oyunun başladığını bildir
                player1.sendPacket(new Packet("REMATCH_ACCEPTED", gameId));
                player2.sendPacket(new Packet("REMATCH_ACCEPTED", gameId));

                System.out.println("Yeni rematch oyunu başladı: " + gameId +
                        " (Oyuncu " + fromClientId + " ve Oyuncu " + toClientId + ")");

                // Güncellenmiş listeyi gönder
                broadcastClientList();
            } else {
                // Yeniden oyun reddedildi, istek gönderen oyuncuya bildir
                ClientHandler requester = oldSession.getPlayerHandler(toClientId);
                if (requester != null) {
                    requester.sendPacket(new Packet("REMATCH_REJECTED", "Rakip yeniden oynamak istemiyor."));
                }
            }
        }
    }



    public void handleInviteResponse(int fromClientId, int toClientId, boolean accepted) {
        ClientHandler sender = connectedClients.get(toClientId);
        ClientHandler receiver = connectedClients.get(fromClientId);

        // Her iki oyuncunun davet durumunu sıfırla
        playerInviteStatus.put(fromClientId, false);
        playerInviteStatus.put(toClientId, false);
        if (sender != null && receiver != null) {
            if (accepted) {
                // İlk olarak bu oyuncular arasında önceki bir oyun olup olmadığını kontrol et
                GameSession existingGame = null;

                for (GameSession game : activeGames.values()) {
                    if ((game.hasPlayer(fromClientId) && game.hasPlayer(toClientId)) &&
                            game.isGameOver()) {
                        existingGame = game;
                        break;
                    }
                }

                String gameId;
                GameSession gameSession;

                if (existingGame != null) {
                    // Mevcut oyunu yeniden başlat
                    gameId = existingGame.getGameId();
                    System.out.println("Mevcut oyun devam ettiriliyor: " + gameId);

                    // Oyunu sıfırla ve yeniden başlat
                    existingGame.resetGame();
                    gameSession = existingGame;
                } else {
                    // Mevcut oyun yoksa yeni bir oyun oturumu oluştur
                    gameId = "game-" + gameIdCounter.getAndIncrement();

                    // Önceki davranıştan farklı olarak oyuncuları listeden çıkarmıyoruz
                    // Bunun yerine sadece yeni bir oyun oturumu oluşturuyoruz
                    gameSession = new GameSession(gameId, receiver, sender);
                    activeGames.put(gameId, gameSession);

                    // Sunucu referansını ve oyuncu ID'lerini ayarla
                    gameSession.setServer(this);
                    gameSession.setPlayerIds(fromClientId, toClientId);
                }

                // Her iki oyuncuya da oyunun başladığını bildir
                receiver.sendPacket(new Packet("GAME_STARTED", gameId + "|1"));
                sender.sendPacket(new Packet("GAME_STARTED", gameId + "|2"));

                System.out.println("Oyun " + gameId + ": başladı! Oyuncu " + fromClientId +
                        " ve Oyuncu " + toClientId + " arasında");

                // Oyuncuların durumlarını güncelle (isteğe bağlı)
                // playerGameStatus.put(fromClientId, gameId);
                // playerGameStatus.put(toClientId, gameId);

                // Güncellenmiş listeyi gönder
                broadcastClientList();
            } else {
                // Davet reddedildi
                receiver.sendPacket(new Packet("INVITE_DECLINED", String.valueOf(toClientId)));
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
