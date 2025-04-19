package client.ui;

import client.GameClient;
import client.ui.game.GameBoard;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private GameClient gameClient;

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
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

    }

    public void updateGameState() {
    }

    public void showPlacementPanel(boolean show) {
        validate();
    }
}