package server;

import shared.Board;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ShipPlacementHandler implements Runnable {
    private Socket playerSocket;
    private Board playerBoard;
    private int playerId;

    // Standard ship sizes in Battleship
    private final int[] SHIP_SIZES = {5, 4, 3, 3, 2};
    private final String[] SHIP_NAMES = {"Carrier (5)", "Battleship (4)", "Cruiser (3)", "Submarine (3)", "Destroyer (2)"};

    public ShipPlacementHandler(Socket playerSocket, Board playerBoard, int playerId) {
        this.playerSocket = playerSocket;
        this.playerBoard = playerBoard;
        this.playerId = playerId;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
                PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true)
        ) {
            out.println("SHIP_PLACEMENT_START");
            out.println("Place your ships on the board.");

            // Place each ship
            for (int i = 0; i < SHIP_SIZES.length; i++) {
                int shipSize = SHIP_SIZES[i];
                String shipName = SHIP_NAMES[i];
                boolean placed = false;

                while (!placed) {
                    out.println("PLACE_SHIP|" + shipSize + "|" + shipName);
                    out.println("Enter coordinates for your " + shipName + " (x y orientation): ");

                    String input = in.readLine();
                    String[] parts = input.split(" ");

                    if (parts.length != 3) {
                        out.println("INVALID_FORMAT|Please use format: x y orientation (h/v)");
                        continue;
                    }

                    try {
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        boolean horizontal = parts[2].toLowerCase().startsWith("h");

                        if (playerBoard.placeShip(x, y, shipSize, horizontal)) {
                            placed = true;
                            out.println("SHIP_PLACED|" + shipSize + "|" + x + "|" + y + "|" + horizontal);
                        } else {
                            out.println("INVALID_PLACEMENT|Can't place ship there. Try again.");
                        }
                    } catch (NumberFormatException e) {
                        out.println("INVALID_FORMAT|Please use numbers for coordinates");
                    }
                }
            }

            out.println("SHIP_PLACEMENT_COMPLETE");
            out.println("All ships placed. Waiting for other player...");

        } catch (IOException e) {
            System.err.println("Error handling ship placement for Player " + playerId + ": " + e.getMessage());
        }
    }
}