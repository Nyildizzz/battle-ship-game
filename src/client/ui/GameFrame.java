package client.ui;

import client.GameClient;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameFrame extends JFrame {
    private final GameClient gameClient;
    private BoardPanel playerBoardPanel;
    private BoardPanel opponentBoardPanel;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JTextArea gameLogArea;
    private JPanel statusPanel;
    
    // UI Renkleri
    private static final Color BACKGROUND_COLOR = new Color(12, 30, 64);
    private static final Color PANEL_COLOR = new Color(22, 56, 108);
    private static final Color TITLE_COLOR = new Color(220, 220, 255);
    private static final Color ACCENT_COLOR = new Color(65, 105, 225);
    private static final Color TEXT_COLOR = new Color(240, 240, 255);
    private static final Color STATUS_PLAYER_TURN = new Color(100, 180, 100);
    private static final Color STATUS_OPPONENT_TURN = new Color(180, 100, 100);
    
    // Oyun istatistikleri
    private int playerHits = 0;
    private int playerMisses = 0;
    private int opponentHits = 0;
    private int opponentMisses = 0;

    public GameFrame(GameClient gameClient) {
        this.gameClient = gameClient;
        initializeFrame();
        updateTitleWithClientId();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Pencere kapatılırken onay sor
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                    GameFrame.this,
                    "Oyundan çıkmak istediğinize emin misiniz?",
                    "Çıkış Onayı",
                    JOptionPane.YES_NO_OPTION);
                
                if (result == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });
    }

    private void initializeFrame() {
        String initialTitle = "Amiral Battı - Savaşa Hazırlanıyor...";
        setTitle(initialTitle);
        
        // Pencere ayarları
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));
        
        // Özel ikon
        setIconImage(createGameIcon().getImage());
        
        // Ana panel oluştur ve gradient arka plan ayarla
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Degrade arka plan
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(5, 20, 40),
                    0, h, new Color(15, 40, 80)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
                
                // Dekoratif dalga çizgileri
                g2d.setColor(new Color(40, 80, 120, 50));
                for (int y = 0; y < h; y += 20) {
                    int waveHeight = 4;
                    int waveLength = 30;
                    for (int x = 0; x < w; x += waveLength) {
                        g2d.drawLine(x, y, x + waveLength/2, y - waveHeight);
                        g2d.drawLine(x + waveLength/2, y - waveHeight, x + waveLength, y);
                    }
                }
            }
        };
        
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Durum paneli
        statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.NORTH);
        
        // Oyun tahtaları
        JPanel boardsPanel = createBoardsPanel();
        mainPanel.add(boardsPanel, BorderLayout.CENTER);
        
        // Oyun logu ve istatistikler
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Ana paneli pencereye ekle
        setContentPane(mainPanel);
        
        // Log alanına başlangıç mesajı
        addToGameLog("Oyuna Hoş Geldiniz! Bağlantı kuruluyor...");
    }

    private ImageIcon createGameIcon() {
        int size = 32;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Daire arka plan
        g2d.setColor(new Color(0, 87, 183));
        g2d.fillOval(0, 0, size, size);
        
        // Gemi simgesi
        g2d.setColor(Color.WHITE);
        int shipWidth = size - 10;
        int shipHeight = size/3;
        g2d.fillRect(5, size/3, shipWidth, shipHeight);
        g2d.fillPolygon(
            new int[]{5 + shipWidth/2, 5 + shipWidth, 5 + shipWidth/2},
            new int[]{size/3, size/3, size/6},
            3
        );
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        
        // Oyun durumu etiketi
        statusLabel = new JLabel("Oyun Yükleniyor...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setBorder(createRoundedBorder(ACCENT_COLOR));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(22, 56, 108, 200));
        
        // Skor etiketi
        scoreLabel = new JLabel("Skor: 0 - 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setForeground(TEXT_COLOR);
        scoreLabel.setBorder(createRoundedBorder(ACCENT_COLOR));
        scoreLabel.setOpaque(true);
        scoreLabel.setBackground(new Color(22, 56, 108, 200));
        

        panel.add(statusLabel, BorderLayout.CENTER);
        
        // Yan panel için alt panel
        JPanel sidePanel = new JPanel(new BorderLayout(10, 0));
        sidePanel.setOpaque(false);

        panel.add(sidePanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private Border createRoundedBorder(Color color) {
        return new CompoundBorder(
            new LineBorder(color, 1),
            new EmptyBorder(8, 15, 8, 15)
        );
    }
    
    private JPanel createBoardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 10));
        panel.setOpaque(false);
        
        // Oyuncu tahtası
        playerBoardPanel = new BoardPanel(gameClient.getPlayerBoard(), false, gameClient);
        
        // Rakip tahtası
        opponentBoardPanel = new BoardPanel(gameClient.getOpponentBoard(), true, gameClient);
        
        panel.add(createTitledPanel("Senin Deniz Alanın", playerBoardPanel, true));
        panel.add(createTitledPanel("Düşman Deniz Alanı", opponentBoardPanel, false));
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        
        // Oyun logu
        gameLogArea = new JTextArea(6, 30);
        gameLogArea.setEditable(false);
        gameLogArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        gameLogArea.setBackground(new Color(10, 25, 50));
        gameLogArea.setForeground(TEXT_COLOR);
        gameLogArea.setLineWrap(true);
        gameLogArea.setWrapStyleWord(true);
        gameLogArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        gameLogArea.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(gameLogArea);
        scrollPane.setBorder(createStyledTitleBorder("Savaş Günlüğü"));
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = ACCENT_COLOR;
                this.trackColor = new Color(10, 25, 50);
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
        
        // İstatistik paneli
        JPanel statsPanel = createStatsPanel();
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(statsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(createStyledTitleBorder("Savaş İstatistikleri"));
        panel.setPreferredSize(new Dimension(220, 0));
        
        // Oyuncu istatistikleri
        JLabel playerStatsHeader = createStyledLabel("Senin Sonuçların:", Font.BOLD, 14);
        JPanel playerStatsPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        playerStatsPanel.setOpaque(false);
        
        JLabel playerHitsLabel = createStyledLabel("İsabetler:", Font.PLAIN, 13);
        JLabel playerHitsValue = createStyledLabel("0", Font.BOLD, 13);
        playerHitsValue.setForeground(new Color(100, 200, 100));
        
        JLabel playerMissesLabel = createStyledLabel("Iskalamalar:", Font.PLAIN, 13);
        JLabel playerMissesValue = createStyledLabel("0", Font.BOLD, 13);
        playerMissesValue.setForeground(new Color(200, 100, 100));
        
        playerStatsPanel.add(playerHitsLabel);
        playerStatsPanel.add(playerHitsValue);
        playerStatsPanel.add(playerMissesLabel);
        playerStatsPanel.add(playerMissesValue);
        
        // Rakip istatistikleri
        JLabel opponentStatsHeader = createStyledLabel("Düşman Sonuçları:", Font.BOLD, 14);
        JPanel opponentStatsPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        opponentStatsPanel.setOpaque(false);
        
        JLabel opponentHitsLabel = createStyledLabel("İsabetler:", Font.PLAIN, 13);
        JLabel opponentHitsValue = createStyledLabel("0", Font.BOLD, 13);
        opponentHitsValue.setForeground(new Color(200, 100, 100));
        
        JLabel opponentMissesLabel = createStyledLabel("Iskalamalar:", Font.PLAIN, 13);
        JLabel opponentMissesValue = createStyledLabel("0", Font.BOLD, 13);
        opponentMissesValue.setForeground(new Color(100, 200, 100));
        
        opponentStatsPanel.add(opponentHitsLabel);
        opponentStatsPanel.add(opponentHitsValue);
        opponentStatsPanel.add(opponentMissesLabel);
        opponentStatsPanel.add(opponentMissesValue);
        
        // İsabet oranı
        JLabel accuracyHeader = createStyledLabel("İsabet Oranları:", Font.BOLD, 14);
        JPanel accuracyPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        accuracyPanel.setOpaque(false);
        
        JLabel playerAccuracyLabel = createStyledLabel("Senin Oranın:", Font.PLAIN, 13);
        JLabel playerAccuracyValue = createStyledLabel("0%", Font.BOLD, 13);
        
        JLabel opponentAccuracyLabel = createStyledLabel("Düşman Oranı:", Font.PLAIN, 13);
        JLabel opponentAccuracyValue = createStyledLabel("0%", Font.BOLD, 13);
        
        accuracyPanel.add(playerAccuracyLabel);
        accuracyPanel.add(playerAccuracyValue);
        accuracyPanel.add(opponentAccuracyLabel);
        accuracyPanel.add(opponentAccuracyValue);
        
        // İstatistikleri güncellemek için timer
        Timer statsTimer = new Timer(2000, e -> {
            // İstatistik değerlerini güncelle
            playerHitsValue.setText(Integer.toString(playerHits));
            playerMissesValue.setText(Integer.toString(playerMisses));
            opponentHitsValue.setText(Integer.toString(opponentHits));
            opponentMissesValue.setText(Integer.toString(opponentMisses));
            
            // İsabet oranlarını hesapla
            int playerTotal = playerHits + playerMisses;
            int opponentTotal = opponentHits + opponentMisses;
            
            int playerAccuracy = playerTotal > 0 ? (playerHits * 100) / playerTotal : 0;
            int opponentAccuracy = opponentTotal > 0 ? (opponentHits * 100) / opponentTotal : 0;
            
            playerAccuracyValue.setText(playerAccuracy + "%");
            opponentAccuracyValue.setText(opponentAccuracy + "%");
            
            // Skor güncellemesi
            scoreLabel.setText("Skor: " + playerHits + " - " + opponentHits);
        });
        statsTimer.start();
        
        // Tüm panelleri ana panele ekle
        panel.add(playerStatsHeader);
        panel.add(Box.createVerticalStrut(5));
        panel.add(playerStatsPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(opponentStatsHeader);
        panel.add(Box.createVerticalStrut(5));
        panel.add(opponentStatsPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(accuracyHeader);
        panel.add(Box.createVerticalStrut(5));
        panel.add(accuracyPanel);
        
        return panel;
    }
    
    private JLabel createStyledLabel(String text, int fontStyle, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", fontStyle, fontSize));
        label.setForeground(TEXT_COLOR);
        return label;
    }
    
    private Border createStyledTitleBorder(String title) {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 1),
            title
        );
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        titledBorder.setTitleColor(TITLE_COLOR);
        titledBorder.setTitlePosition(TitledBorder.ABOVE_TOP);
        titledBorder.setTitleJustification(TitledBorder.CENTER);
        
        return new CompoundBorder(
            titledBorder,
            new EmptyBorder(8, 8, 8, 8)
        );
    }

    private JPanel createTitledPanel(String title, JPanel contentPanel, boolean isPlayerBoard) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Yuvarlak köşeli dikdörtgen çiz
                RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
                    0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                // Gradyan arka plan
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(isPlayerBoard ? 20 : 30, 40, 80, 200),
                    0, getHeight(), new Color(isPlayerBoard ? 40 : 50, 70, 110, 200)
                );
                
                g2d.setPaint(gp);
                g2d.fill(roundedRect);
                
                // Kenar çizgisi
                g2d.setColor(isPlayerBoard ? 
                    new Color(100, 150, 200) : 
                    new Color(170, 120, 120));
                g2d.setStroke(new BasicStroke(2f));
                g2d.draw(roundedRect);
            }
        };
        
        panel.setOpaque(false);
        
        // Başlık etiketi
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(
            this, 
            message, 
            "Mesaj", 
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // Log alanına da ekle
        addToGameLog("Bilgi: " + message);
    }

    public void showError(String errorMessage) {
        JOptionPane.showMessageDialog(
            this, 
            errorMessage, 
            "Hata", 
            JOptionPane.ERROR_MESSAGE
        );
        
        // Log alanına da ekle
        addToGameLog("Hata: " + errorMessage);
    }
    
    public void showGameOver(boolean isWin) {
        String message = isWin ? 
            "Tebrikler! Düşman filosunu yok ettiniz. Zafer sizin!" : 
            "Maalesef... Tüm filonuz batırıldı. Bir sonraki sefere!";
        
        ImageIcon icon = createGameOverIcon(isWin);
        
        JOptionPane.showMessageDialog(
            this, 
            message, 
            isWin ? "Zafer!" : "Mağlubiyet!", 
            JOptionPane.INFORMATION_MESSAGE,
            icon
        );
        
        // Log alanına da ekle
        addToGameLog("Oyun Sonu: " + message);
        
        // Oyun bitti durumunu göster
        updateTurnStatusUI(false);
        statusLabel.setText(isWin ? "ZAFER! Tüm düşman gemileri batırıldı!" : "HEZMET! Filonuz yok edildi!");
        statusLabel.setForeground(isWin ? new Color(120, 255, 120) : new Color(255, 120, 120));
    }
    
    private ImageIcon createGameOverIcon(boolean isWin) {
        int size = 64;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (isWin) {
            // Zafer ikonu - Altın madalya
            g2d.setColor(new Color(255, 215, 0));
            g2d.fillOval(4, 4, size - 8, size - 8);
            
            g2d.setColor(new Color(218, 165, 32));
            g2d.fillOval(10, 10, size - 20, size - 20);
            
            g2d.setColor(new Color(255, 215, 0));
            g2d.setFont(new Font("Arial", Font.BOLD, 32));
            g2d.drawString("V", 23, 43);
        } else {
            // Mağlubiyet ikonu - Batık gemi
            g2d.setColor(new Color(50, 50, 50));
            g2d.fillRoundRect(10, size/2, size - 20, size/4, 5, 5);
            
            g2d.setColor(new Color(80, 80, 80));
            g2d.fillRect(20, size/3, 10, size/6);
            g2d.fillRect(35, size/3 - 5, 10, size/5);
            
            // Suda batma efekti
            g2d.setColor(new Color(30, 100, 200));
            g2d.fillRect(0, size/2 + size/8, size, size/2);
            
            g2d.setColor(new Color(150, 200, 255, 180));
            for (int i = 0; i < 4; i++) {
                g2d.fillOval(10 + i*15, size/2 + 5, 10, 6);
            }
        }
        
        g2d.dispose();
        return new ImageIcon(image);
    }

    public void updateBoards() {
        SwingUtilities.invokeLater(() -> {
            if (playerBoardPanel != null) {
                playerBoardPanel.refreshBoard();
            }
            if (opponentBoardPanel != null) {
                opponentBoardPanel.refreshBoard();
            }
        });
    }

    public void updateTitleWithClientId() {
        SwingUtilities.invokeLater(() -> {
            int clientId = gameClient.getClientId();
            if (clientId != -1) {
                setTitle("Amiral Battı - Komutan #" + clientId);
            } else {
                setTitle("Amiral Battı - Bağlantı Bekleniyor...");
            }
        });
    }
    
    public void updateTurnStatusUI(boolean isMyTurn) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Sıra durumu güncelleniyor: " + (isMyTurn ? "Senin Sıra" : "Rakip Sırası"));
            
            String statusText;
            Color statusBackground;
            
            if (isMyTurn) {
                statusText = "✷ SIRA SİZDE ✷  Düşman gemilerini batırmak için ateş ediniz!";
                statusBackground = new Color(0, 60, 0, 200);
                
                // Log mesajı
                addToGameLog("Sıra size geçti. Ateş edebilirsiniz!");
            } else {
                statusText = "⚠ RAKİP ATIŞI ⚠  Düşman hareketini bekleyiniz...";
                statusBackground = new Color(60, 0, 0, 200);
                
                // Log mesajı
                addToGameLog("Sıra rakibe geçti. Bekleyiniz.");
            }
            
            statusLabel.setText(statusText);
            statusLabel.setBackground(statusBackground);
            
            // Rakip tahtasının tıklanabilirliğini ayarla
            if (opponentBoardPanel != null) {
                opponentBoardPanel.setTurnActive(isMyTurn);
            }
        });
    }
    
    // İstatistik güncellemeleri için metotlar
    public void recordHit(boolean isPlayerHit) {
        if (isPlayerHit) {
            playerHits++;
        } else {
            opponentHits++;
        }
    }
    
    public void recordMiss(boolean isPlayerMiss) {
        if (isPlayerMiss) {
            playerMisses++;
        } else {
            opponentMisses++;
        }
    }
    
    // Oyun kayıt alanına mesaj eklemek için metot
    public void addToGameLog(String message) {
        SwingUtilities.invokeLater(() -> {
            // Tarih damgasıyla birlikte ekle
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timestamp = sdf.format(new Date());
            
            gameLogArea.append("[" + timestamp + "] " + message + "\n");
            
            // En altta görünmesi için otomatik kaydır
            gameLogArea.setCaretPosition(gameLogArea.getDocument().getLength());
        });
    }
}