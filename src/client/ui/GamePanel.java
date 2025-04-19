package client.ui;

import client.GameClient;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private GameClient gameClient;
    private BoardPanel playerBoardPanel;
    private BoardPanel opponentBoardPanel;
    private JLabel playerLabel;
    private JLabel opponentLabel;

    public GamePanel(GameClient gameClient) {
        this.gameClient = gameClient;
        setLayout(new GridLayout(2, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        setupListeners();
    }

    private void initComponents() {
        playerLabel = new JLabel("Your Board", SwingConstants.CENTER);
        opponentLabel = new JLabel("Opponent's Board", SwingConstants.CENTER);

        playerBoardPanel = new BoardPanel(gameClient.getPlayerBoard(), true);
        opponentBoardPanel = new BoardPanel(gameClient.getOpponentBoard(), false);

        add(playerLabel);
        add(opponentLabel);
        add(playerBoardPanel);
        add(opponentBoardPanel);
    }

    private void setupListeners() {
        opponentBoardPanel.setCellClickListener((x, y) -> {
            if (gameClient.isPlayerTurn()) {
                gameClient.sendAttack(x, y);
            }
        });
    }

    public void updateBoards() {
        playerBoardPanel.repaint();
        opponentBoardPanel.repaint();
    }
}