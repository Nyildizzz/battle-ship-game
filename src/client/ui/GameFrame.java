package client.ui;

import client.GameClient;
import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private final GameClient gameClient;

    public GameFrame(GameClient gameClient) {
        this.gameClient = gameClient;
        initializeFrame();
    }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         

    private void initializeFrame() {
        setTitle("Amiral Battı - Oyun");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Geçici bir label ekleyelim
        JLabel label = new JLabel("Oyun başladı!", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        add(label);
    }
}