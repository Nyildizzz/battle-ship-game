package server;

import shared.Board;
import shared.GameState;

import java.io.*;
import java.net.Socket;

public class GameHandler implements Runnable {
    private Socket playerSocket;
    private Socket opponentSocket;
    private GameState gameState;
    private int playerId;

    public GameHandler(Socket playerSocket, Socket opponentSocket, GameState gameState, int playerId) {
        this.playerSocket = playerSocket;
        this.opponentSocket = opponentSocket;
        this.gameState = gameState;
        this.playerId = playerId;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
                PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
                PrintWriter opponentOut = new PrintWriter(opponentSocket.getOutputStream(), true);
        ) {
            out.println("Welcome Player " + playerId + "!");

            while (!gameState.isGameOver()) {
                if (gameState.getCurrentPlayer() == playerId) {
                    out.println("Your turn! Enter attack coordinates (x y):");
                    String[] input = in.readLine().split(" ");
                    int x = Integer.parseInt(input[0]);
                    int y = Integer.parseInt(input[1]);

                    Board opponentBoard = gameState.getBoard(3 - playerId);
                    String result = opponentBoard.attack(x, y);
                    out.println("Result: " + result);
                    opponentOut.println("Opponent attacked (" + x + ", " + y + "): " + result);

                    if (opponentBoard.allShipsSunk()) {
                        gameState.setWinner(playerId);
                        out.println("You win!");
                        opponentOut.println("You lose!");
                        break;
                    }

                    gameState.switchTurn();
                } else {
                    out.println("Waiting for opponent's move...");
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling player " + playerId + ": " + e.getMessage());
        }
    }
}