package client.ui;

import client.GameClient;
import shared.Board;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private GameClient gameClient;
    private GamePanel gamePanel;
    private StatusPanel statusPanel;
    private ShipPlacementPanel shipPlacementPanel;

    public MainFrame(GameClient gameClient) {
        this.gameClient = gameClient;
        setTitle("Battleship Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initComponents();
        setupLayout();
    }

    private void initComponents() {
        gamePanel = new GamePanel(gameClient);
        statusPanel = new StatusPanel();
        shipPlacementPanel = new ShipPlacementPanel(gameClient);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Ship placement panel will be shown initially and hidden once ships are placed
        add(shipPlacementPanel, BorderLayout.EAST);
    }

    public void updateGameState() {
        gamePanel.repaint();
        statusPanel.updateStatus(gameClient.getGameState());
    }

    public void showPlacementPanel(boolean show) {
        shipPlacementPanel.setVisible(show);
        validate();
    }
}