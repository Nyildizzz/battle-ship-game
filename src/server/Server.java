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
        connectedClients.remove(clientId);
        playerInviteStatus.remove(clientId); // Davet durumunu temizle

        // Eğer bu oyuncu bir davet gönderdiyse veya aldıysa, diğer tarafın durumunu da temizleyelim
        for (Map.Entry<Integer, Boolean> entry : playerInviteStatus.entrySet()) {
            if (entry.getValue()) {
                // Diğer oyuncuya davet iptal edildi bildirimi gönder
                ClientHandler otherPlayer = connectedClients.get(entry.getKey());
                if (otherPlayer != null) {
                    otherPlayer.sendPacket(new Packet("INVITE_CANCELED", "Davet eden oyuncu çıkış yaptı."));
                }
                entry.setValue(false); // Durumu sıfırla
            }
        }

        System.out.println("Client " + clientId + " disconnected");
        broadcastClientList();
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

        // Eğer oyuncu zaten davet almış veya davet etmiş durumda ise
        if (playerInviteStatus.getOrDefault(toClientId, false)) {
            // Davet gönderen kişiye, hedef kişinin meşgul olduğunu bildir
            ClientHandler sender = connectedClients.get(fromClientId);
            if (sender != null) {
                sender.sendPacket(new Packet("INVITE_ERROR", "Oyuncu " + toClientId + " şu anda başka bir davet ile meşgul."));
            }
            return;
        }

        // Davet gönderen kişinin durumunu güncelle
        playerInviteStatus.put(fromClientId, true);
        // Davet alan kişinin durumunu güncelle
        playerInviteStatus.put(toClientId, true);

        if (receiver != null) {
            receiver.sendPacket(new Packet("GAME_INVITE", String.valueOf(fromClientId)));
        }
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

                // Start the game
                gameSession.start();
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
    public void addClientBack(int clientId, ClientHandler handler) {
        // Eğer istemci hala bağlıysa, onu tekrar listeye ekle
        if (handler.isConnected()) {
            connectedClients.put(clientId, handler);
            broadcastClientList();
        }
    }

    public void removeGame(String gameId) {
        activeGames.remove(gameId);
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}