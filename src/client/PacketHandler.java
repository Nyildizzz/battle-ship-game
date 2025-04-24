package client;

import shared.Packet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class PacketHandler {
    private BufferedReader in;
    private PrintWriter out;

    public PacketHandler(BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
    }

    public void sendPacket(Packet packet) {
        out.println(packet.serialize());
    }

    public Packet receivePacket() throws IOException {
        String rawPacket = in.readLine();
        if (rawPacket == null) {
            throw new IOException("Connection closed");
        }
        return Packet.deserialize(rawPacket);
    }

    public void sendMessage(String type, String data) {
        Packet packet = new Packet(type, data);
        sendPacket(packet);
    }

}