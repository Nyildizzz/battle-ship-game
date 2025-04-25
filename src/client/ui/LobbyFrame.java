package client.ui;

import client.Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

public class LobbyFrame extends JFrame {
    private Client client;
    private DefaultListModel<String> clientListModel;
    private JList<String> clientList;
    private JButton inviteButton;
    private JLabel statusLabel;
    private JPanel mainPanel;
    private List<Integer> clientsList;

    // Colors
    private final Color NAVY_BLUE = new Color(0, 48, 73);
    private final Color LIGHT_BLUE = new Color(214, 237, 255);
    private final Color ACCENT_COLOR = new Color(255, 103, 0);
    private final Color WATER_COLOR = new Color(173, 216, 230);

    // Ship definitions
    private final String[] SHIP_NAMES = {"carrier", "battleship", "cruiser", "submarine", "destroyer"};
    private final int[] SHIP_LENGTHS = {5, 4, 3, 3, 2};
    private final Color[] SHIP_COLORS = {
            new Color(60, 60, 60),    // Carrier (dark gray)
            new Color(80, 80, 80),    // Battleship
            new Color(100, 100, 100), // Cruiser
            new Color(120, 120, 120), // Submarine
            new Color(140, 140, 140)  // Destroyer (light gray)
    };

    public LobbyFrame(Client client) {
        this.client = client;

        setTitle("Battleship Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create app icon programmatically
        BufferedImage appIcon = createAppIcon();
        setIconImage(appIcon);

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }

        initComponents();
    }

    private BufferedImage createAppIcon() {
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setColor(NAVY_BLUE);
        g.fillRect(0, 0, 32, 32);
        g.setColor(LIGHT_BLUE);
        g.fillRect(8, 12, 16, 4); // Battleship silhouette
        g.setColor(ACCENT_COLOR);
        g.fillOval(14, 6, 4, 4);  // Radar
        g.dispose();
        return icon;
    }

    private void initComponents() {
        // Main panel with border layout
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(LIGHT_BLUE);

        // Add header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Main content panel with player list and game board
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);

        // Left panel for player list
        JPanel leftPanel = createPlayerListPanel();
        contentPanel.add(leftPanel, BorderLayout.WEST);

        // Right panel with login_screen image
        JPanel rightPanel = createRightPanel();
        contentPanel.add(rightPanel, BorderLayout.CENTER);

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

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        // Try to load login_screen image
        ImageIcon loginImage = null;
        try {
            java.net.URL imgURL = getClass().getResource("/resources/login_screen.png");
            if (imgURL != null) {
                loginImage = new ImageIcon(imgURL);
                // Scale image if it's too large
                if (loginImage.getIconWidth() > 500 || loginImage.getIconHeight() > 400) {
                    Image img = loginImage.getImage();
                    Image scaledImg = img.getScaledInstance(500, -1, Image.SCALE_SMOOTH);
                    loginImage = new ImageIcon(scaledImg);
                }
                JLabel imageLabel = new JLabel(loginImage, SwingConstants.CENTER);
                rightPanel.add(imageLabel, BorderLayout.CENTER);
            } else {
                System.err.println("Couldn't find login_screen.png in resources directory");

                // Fallback if image not found
                createShips();
            }
        } catch (Exception e) {
            System.err.println("Error loading login image: " + e.getMessage());
        }

        return rightPanel;
    }

    private JPanel createPlayerListPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(200, 400));

        JLabel listLabel = new JLabel("Select a player to invite:");
        listLabel.setForeground(NAVY_BLUE);
        listLabel.setFont(new Font("Arial", Font.BOLD, 14));

        createPlayerList();
        JScrollPane scrollPane = new JScrollPane(clientList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NAVY_BLUE, 2, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        leftPanel.add(listLabel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        return leftPanel;
    }

    private void createShips() {
        // Create programmatic ship images and place them on the board
        for (int i = 0; i < SHIP_NAMES.length; i++) {
            BufferedImage shipImg = new BufferedImage(
                    SHIP_LENGTHS[i] * 40, 40, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = shipImg.createGraphics();

            // Fill ship shape
            g.setColor(SHIP_COLORS[i]);
            g.fillRect(0, 0, SHIP_LENGTHS[i] * 40, 40);

            // Draw outline
            g.setColor(Color.WHITE);
            g.drawRect(0, 0, SHIP_LENGTHS[i] * 40 - 1, 39);

            // Add details
            g.setColor(Color.BLACK);
            for (int j = 1; j < SHIP_LENGTHS[i]; j++) {
                g.drawLine(j * 40, 0, j * 40, 39);
            }

            // Add a circle in the middle for visual interest
            g.setColor(ACCENT_COLOR);
            g.fillOval(shipImg.getWidth()/2 - 8, 12, 16, 16);

            g.dispose();

        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);


        JLabel subtitleLabel = new JLabel("MULTIPLAYER LOBBY");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        subtitleLabel.setForeground(NAVY_BLUE);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);


        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
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
            if (!e.getValueIsAdjusting()) {
                inviteButton.setEnabled(clientList.getSelectedIndex() != -1);
            }
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


        buttonPanel.add(inviteButton);

        return buttonPanel;
    }


    private void sendInvite() {
        int selectedIndex = clientList.getSelectedIndex();
        if (clientsList != null && selectedIndex >= 0 && selectedIndex < clientsList.size()) {
            try {
                int targetClientId = clientsList.get(selectedIndex);
                // Seçilen kişi kendisi mi kontrolü
                if (targetClientId == client.getClientId()) {
                    JOptionPane.showMessageDialog(this,
                            "Kendinize davet gönderemezsiniz!",
                            "Davet Hatası", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                client.sendInvite(targetClientId);
                statusLabel.setText("Oyuncu " + targetClientId + "'e davet gönderildi...");
            } catch (IndexOutOfBoundsException e) {
                JOptionPane.showMessageDialog(this,
                    "Seçili kullanıcı bilgisine erişilemedi.",
                    "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void updateClientList(List<Integer> clients, int myClientId) {
        // LobbyFrame sınıfına List<Integer> clientsList değişkeni eklediğinizden emin olun
        clientsList = new ArrayList<>(clients);

        clientListModel.clear();
        for (Integer clientId : clients) {
            if (clientId == myClientId) {
                clientListModel.addElement("Oyuncu " + clientId + " (Ben)");
            } else {
                clientListModel.addElement("Oyuncu " + clientId);
            }
        }
    }

    // Davet durumunu güncellemek için yeni bir metot
    public void updateInviteState(boolean isInviting) {
        // Eğer aktif bir davet varsa davet butonunu devre dışı bırak
        inviteButton.setEnabled(!isInviting);

        if (isInviting) {
            statusLabel.setText("Davet bekleniyor...");
        } else {
            statusLabel.setText("Çevrimiçi oyunculardan birini seçip davet gönderin.");
        }
    }

    // Custom cell renderer for player list
    private class PlayerListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            // Create player icon programmatically
            BufferedImage playerIcon = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = playerIcon.createGraphics();
            g.setColor(NAVY_BLUE);
            g.fillOval(4, 2, 16, 16);  // Head
            g.fillRect(8, 18, 8, 6);   // Body
            g.dispose();

            label.setIcon(new ImageIcon(playerIcon));
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
}