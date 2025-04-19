package shared;

/**
 * Represents a network packet for communication between client and server.
 */
public class Packet {
    private String type;
    private String data;

    /**
     * Creates a new packet with the specified type and data.
     *
     * @param type The type of packet (e.g., "ATTACK", "PLACE_SHIP")
     * @param data The data payload for the packet
     */
    public Packet(String type, String data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Get the packet type.
     */
    public String getType() {
        return type;
    }

    /**
     * Get the packet data.
     */
    public String getData() {
        return data;
    }

    /**
     * Serializes the packet to a string for network transmission.
     * Format: "type|data"
     */
    public String serialize() {
        return type + "|" + data;
    }

    /**
     * Deserializes a string into a Packet object.
     *
     * @param rawPacket The serialized packet string
     * @return A new Packet object
     */
    public static Packet deserialize(String rawPacket) {
        String[] parts = rawPacket.split("\\|", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid packet format: " + rawPacket);
        }

        return new Packet(parts[0], parts[1]);
    }
}