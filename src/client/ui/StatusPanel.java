package client.ui;

import shared.GameState;

import javax.swing.*;
import java.awt.*;

public class StatusPanel extends JPanel {
    private JLabel statusLabel;
    private JLabel turnLabel;

    public StatusPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        setPreferredSize(new Dimension(800, 40));

        statusLabel = new JLabel("Welcome to Battleship!", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        turnLabel = new JLabel("Wait for game to start", SwingConstants.CENTER);

        add(statusLabel, BorderLayout.CENTER);
        add(turnLabel, BorderLayout.EAST);
    }

    public void updateStatus(GameState gameState) {
        if (gameState == null) {
            statusLabel.setText("Connecting to server...");
            turnLabel.setText("");
            return;
        }

        if (gameState.isGameOver()) {
            String winner = gameState.getWinner() == gameState.getCurrentPlayer() ? "You win!" : "You lose!";
            statusLabel.setText("Game Over! " + winner);
            turnLabel.setText("");
        } else {
            boolean isMyTurn = gameState.getCurrentPlayer() == gameState.getCurrentPlayer();
            statusLabel.setText(isMyTurn ? "Your turn" : "Opponent's turn");
            turnLabel.setText("Player " + gameState.getCurrentPlayer() + "'s turn");
        }
    }

    public void setMessage(String message) {
        statusLabel.setText(message);
    }
}