package client.ui;

import client.Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class LobbyFrame extends JFrame {
    private Client client;
    private DefaultListModel<String> clientListModel;
    private JList<String> clientList;
    private JButton inviteButton;
    private JLabel statusLabel;
    private JPanel mainPanel;

    // Colors
    private final Color NAVY_BLUE = new Color(0, 48, 73);
    private final Color LIGHT_BLUE = new Color(214, 237, 255);
    private final Color ACCENT_COLOR = new Color(255, 103, 0);

    public LobbyFrame(Client client) {
        this.client = client;

        setTitle("Battleship Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setIconImage(createImageIcon("/resources/battleship_icon.png", "Battleship").getImage());

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }

        initComponents();
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(LIGHT_BLUE);

        // Header panel with logo and title
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);

        // Player list with custom renderer
        createPlayerList();
        JScrollPane scrollPane = new JScrollPane(clientList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NAVY_BLUE, 2, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JPanel listPanel = new JPanel(new BorderLayout(5, 5));
        listPanel.setOpaque(false);
        listPanel.add(new JLabel("Select a player to invite:"), BorderLayout.NORTH);
        listPanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(listPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel("Connected to server. Waiting for players...");
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusLabel.setForeground(NAVY_BLUE);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("BATTLESHIP");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(NAVY_BLUE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitleLabel = new JLabel("MULTIPLAYER LOBBY");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        subtitleLabel.setForeground(NAVY_BLUE);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        return headerPanel;
    }

    private void createPlayerList() {
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientList.setFont(new Font("Arial", Font.PLAIN, 14));
        clientList.setFixedCellHeight(40);
        clientList.setCellRenderer(new PlayerListCellRenderer());

        // Add selection listener
        clientList.addListSelectionListener(e -> {
            inviteButton.setEnabled(clientList.getSelectedIndex() != -1);
        });
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        inviteButton = new JButton("Invite to Game");
        inviteButton.setBackground(ACCENT_COLOR);
        inviteButton.setForeground(Color.WHITE);
        inviteButton.setFont(new Font("Arial", Font.BOLD, 14));
        inviteButton.setFocusPainted(false);
        inviteButton.setEnabled(false);
        inviteButton.setMnemonic(KeyEvent.VK_I); // Alt+I shortcut

        inviteButton.addActionListener(e -> sendInvite());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshButton.setFocusPainted(false);

        buttonPanel.add(refreshButton);
        buttonPanel.add(inviteButton);

        return buttonPanel;
    }

    private void sendInvite() {
        String selected = clientList.getSelectedValue();
        if (selected != null) {
            int clientId = Integer.parseInt(selected.split(" ")[1]);
            client.sendInvite(clientId);

            statusLabel.setText("Invitation sent to Player " + clientId + ". Waiting for response...");
            inviteButton.setEnabled(false);
        }
    }

    public void updateClientList(List<Integer> activeClients) {
        clientListModel.clear();

        if (activeClients.isEmpty()) {
            statusLabel.setText("No other players online. Waiting for players to connect...");
        } else {
            statusLabel.setText(activeClients.size() + " player(s) online. Select a player to invite.");
        }

        for (Integer clientId : activeClients) {
            clientListModel.addElement("Player " + clientId);
        }
    }

    // Custom cell renderer for player list
    private class PlayerListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            label.setIcon(createImageIcon("/resources/player_icon.png", "Player"));
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            if (isSelected) {
                label.setBackground(NAVY_BLUE);
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(NAVY_BLUE);
            }

            return label;
        }
    }

    // Helper method to create ImageIcon from resource path
    private ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            // Return a default icon
            return new ImageIcon(new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB), description);
        }
    }
}