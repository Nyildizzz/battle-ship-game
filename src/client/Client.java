package client;

import client.ui.LobbyFrame;
import client.ui.ShipPlacementFrame;
import shared.Packet;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    private boolean isInvited = false; // Davet durumunu izlemek için
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private GameClient gameClient;
    private LobbyFrame lobbyFrame;
    private ShipPlacementFrame gameFrame;
    private PacketHandler packetHandler;
    private boolean running;

    private int clientId;
    private List<Integer> activeClients = new ArrayList<>();
    private boolean inGame = false;

    public Client() {
        gameClient = new GameClient();
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            lobbyFrame = new LobbyFrame(this);
            lobbyFrame.setVisible(true);
        });

        connectToServer();
    }



    private void connectToServer() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            packetHandler = new PacketHandler(in, out);
            gameClient.setPacketHandler(packetHandler);

            // Start listening for server messages
            startListening();

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_HOST);
            JOptionPane.showMessageDialog(lobbyFrame,
                    "Cannot connect to server: Host not found",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + SERVER_HOST);
            JOptionPane.showMessageDialog(lobbyFrame,
                    "Cannot connect to server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startListening() {
        running = true;
        new Thread(() -> {
            try {
                String message;
                while (running && (message = in.readLine()) != null) {
                    final Packet packet = Packet.deserialize(message);
                    SwingUtilities.invokeLater(() -> processPacket(packet));
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Connection to server lost: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(lobbyFrame,
                                "Connection to server lost",
                                "Connection Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            } finally {
                cleanup();
            }
        }).start();
    }
    private void processPacket(Packet packet) {
        switch (packet.getType()) {
            case "CLIENT_ID":
                clientId = Integer.parseInt(packet.getData());
                gameClient.setClientId(clientId);
                lobbyFrame.setTitle("Battleship Lobby - Player " + clientId);
                break;

            case "CLIENT_LIST":
                updateClientList(packet.getData());
                break;

            case "GAME_INVITE":
                handleGameInvite(packet.getData());
                break;

            case "INVITE_DECLINED":
                JOptionPane.showMessageDialog(lobbyFrame,
                        "Player " + packet.getData() + " declined your invitation",
                        "Invitation Declined", JOptionPane.INFORMATION_MESSAGE);
                break;

            case "GAME_STARTED":
                handleGameStart(packet.getData());
                break;

            case "INVITE_ERROR":
                JOptionPane.showMessageDialog(lobbyFrame,
                        packet.getData(),
                        "Davet Hatası", JOptionPane.WARNING_MESSAGE);
                break;

            case "INVITE_CANCELED":
                JOptionPane.showMessageDialog(lobbyFrame,
                        "Davet iptal edildi: " + packet.getData(),
                        "Davet İptal", JOptionPane.INFORMATION_MESSAGE);
                break;


            default:
                // Game related packets
                if (inGame && gameClient != null) {
                    gameClient.processPacket(packet);
                    if (gameFrame != null) {
                        gameFrame.updateGameState();
                    }
                }
                break;
        }
    }

    private void updateClientList(String data) {
        activeClients.clear();
        if (!data.isEmpty()) {
            String[] clientIds = data.split(",");
            for (String id : clientIds) {
                activeClients.add(Integer.parseInt(id));
            }
        }
        lobbyFrame.updateClientList(activeClients, clientId);
    }

    private void handleGameInvite(String fromClientId) {
        int from = Integer.parseInt(fromClientId);
        int response = JOptionPane.showConfirmDialog(
                lobbyFrame,
                "Player " + from + " has invited you to a game. Accept?",
                "Game Invitation",
                JOptionPane.YES_NO_OPTION
        );

        boolean accepted = response == JOptionPane.YES_OPTION;
        packetHandler.sendMessage("INVITE_RESPONSE", fromClientId + "|" + accepted);
    }

    private void handleGameStart(String data) {
        String[] parts = data.split("\\|");
        String gameId = parts[0];
        int playerNumber = Integer.parseInt(parts[1]);

        inGame = true;

        SwingUtilities.invokeLater(() -> {
            lobbyFrame.setVisible(false);
            gameFrame = new ShipPlacementFrame(gameClient);
            gameFrame.setTitle("Battleship - Game " + gameId + " - Player " + playerNumber);
            gameFrame.setVisible(true);
        });
    }

    public void sendInvite(int toClientId) {
        // Eğer zaten bir davet sürecinde ise yeni davet gönderilmesini engelle
        if (isInvited) {
            JOptionPane.showMessageDialog(lobbyFrame,
                    "Şu anda aktif bir davet işleminiz var. Lütfen önce mevcut davet işlemini tamamlayın.",
                    "Davet Hatası", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kullanıcının kendisine davet göndermesini engelle
        if (toClientId == this.clientId) {
            JOptionPane.showMessageDialog(lobbyFrame,
                    "Kendinize davet gönderemezsiniz!",
                    "Davet Hatası", JOptionPane.WARNING_MESSAGE);
            return;
        }

        packetHandler.sendMessage("INVITE", String.valueOf(toClientId));
        setInviteState(true); // Davet durumunu güncelle
    }

    private void cleanup() {
        running = false;
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    public void setInviteState(boolean state) {
        this.isInvited = state;
        if (lobbyFrame != null) {
            lobbyFrame.updateInviteState(state);
        }
    }

    public int getClientId() {
        return clientId;
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}