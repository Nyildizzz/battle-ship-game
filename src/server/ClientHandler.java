package server;

import shared.Packet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private int clientId;
    private Socket socket;
    private Server server;
    private BufferedReader in;
    private PrintWriter out;
    private boolean running;
    private GameSession currentGameSession;

    public ClientHandler(int clientId, Socket socket, Server server) {
        this.clientId = clientId;
        this.socket = socket;
        this.server = server;
        this.running = true;

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error setting up client handler: " + e.getMessage());
            running = false;
        }
    }

    @Override
    public void run() {
        try {
            // Send the client their ID
            sendPacket(new Packet("CLIENT_ID", Integer.toString(clientId)));

            // Process client messages
            String message;
            while (running && (message = in.readLine()) != null) {
                processMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Error handling client " + clientId + ": " + e.getMessage());
        } finally {
            close();
            server.removeClient(clientId);
        }
    }

    private void processMessage(String message) {
        try {
            Packet packet = Packet.deserialize(message);

            switch (packet.getType()) {
                case "INVITE":
                    int toClientId = Integer.parseInt(packet.getData());
                    server.handleInvitation(clientId, toClientId);
                    break;

                case "INVITE_RESPONSE":
                    String[] parts = packet.getData().split("\\|");
                    int fromClientId = Integer.parseInt(parts[0]);
                    boolean accepted = Boolean.parseBoolean(parts[1]);
                    server.handleInviteResponse(fromClientId, clientId, accepted);
                    break;

                case "PLAYER_READY":
                    // Eğer bu client bir oyun oturumunda ise, hazır olma durumunu işle
                    if (currentGameSession != null) {
                        currentGameSession.handlePlayerReady(this);
                    }
                    break;

                // Game related packets will be handled in GameSession
                default:
                    break;
        }
    } catch (Exception e) {
        System.err.println("Error processing message from client " + clientId + ": " + e.getMessage());
    }
}



    public void sendPacket(Packet packet) {
        if (out != null && !socket.isClosed()) {
            out.println(packet.serialize());
        }
    }
    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    public void close() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing client handler: " + e.getMessage());
        }
    }

    public int getClientId() {
        return clientId;
    }
}