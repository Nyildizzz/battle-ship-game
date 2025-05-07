package client.ui;

import client.GameClient;
import shared.Board;
import shared.CellStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class BoardPanel extends JPanel implements ActionListener {

    private static final int GRID_SIZE = 10; // Tahta boyutu (10x10)
    private final Board board; // Bu panelin gösterdiği tahta verisi
    private final boolean isOpponentBoard; // Bu panel rakip tahtası mı? (Tıklanabilirlik için)
    private final GameClient gameClient; // Oyun mantığına erişim için
    private final JButton[][] buttons; // Grid hücrelerini temsil eden butonlar
    
    // Oyun tahtası görsel özellikleri
    private static final Color WATER_COLOR = new Color(31, 97, 141);
    private static final Color WATER_HIGHLIGHT = new Color(76, 154, 206);
    private static final Color WATER_DARK = new Color(16, 52, 78);
    private static final Color MISS_COLOR = new Color(200, 200, 255);
    private static final Color HIT_COLOR = new Color(255, 100, 100);
    private static final Color SHIP_COLOR = new Color(80, 80, 80);
    private static final Color SUNK_COLOR = new Color(50, 50, 50);
    private static final Color GRID_LINE_COLOR = new Color(150, 200, 255, 100);
    
    // Koordinatları tutmak için paneller
    private JPanel columnHeadersPanel;
    private JPanel rowHeadersPanel;
    
    // İkonlar için önbellek
    private Map<String, ImageIcon> iconCache = new HashMap<>();
    
    // Animasyon için zamanlayıcı
    private Timer waterAnimationTimer;
    private int waterAnimationOffset = 0;
    
    public BoardPanel(Board board, boolean isOpponentBoard, GameClient gameClient) {
        this.board = board;
        this.isOpponentBoard = isOpponentBoard;
        this.gameClient = gameClient;
        this.buttons = new JButton[GRID_SIZE][GRID_SIZE];
        
        setLayout(new BorderLayout(0, 0));
        
        // Kenarları yuvarlak deniz tahtası oluştur
        JPanel gridPanel = createGridPanel();
        
        // Koordinat etiketlerini oluştur
        createCoordinateLabels();
        
        // Bileşenleri yerleştir
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(columnHeadersPanel, BorderLayout.NORTH);
        centerPanel.add(gridPanel, BorderLayout.CENTER);
        
        add(rowHeadersPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        
        // Arka plan için animasyon zamanlayıcısı
        setupWaterAnimation();
        
        // Başlangıç durumunu butonlara yansıt
        refreshBoard();
    }
    
    private JPanel createGridPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 2, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Deniz arka planı
                GradientPaint waterGradient = new GradientPaint(
                    0, 0, WATER_DARK,
                    0, getHeight(), WATER_COLOR
                );
                g2d.setPaint(waterGradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Dalga çizgileri
                g2d.setColor(GRID_LINE_COLOR);
                
                // Yatay dalga çizgileri
                for (int y = waterAnimationOffset % 20; y < getHeight(); y += 20) {
                    for (int x = 0; x < getWidth(); x += 5) {
                        g2d.drawLine(x, y, x + 3, y);
                    }
                }
                
                // Dikey dalga çizgileri
                for (int x = (waterAnimationOffset / 2) % 20; x < getWidth(); x += 20) {
                    for (int y = 0; y < getHeight(); y += 5) {
                        g2d.drawLine(x, y, x, y + 3);
                    }
                }
            }
        };
        
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        
        // Tahtayı butonlarla doldur
        initializeButtons(gridPanel);
        
        return gridPanel;
    }
    
    private void setupWaterAnimation() {
        waterAnimationTimer = new Timer(100, e -> {
            waterAnimationOffset = (waterAnimationOffset + 1) % 100;
            repaint();
        });
        waterAnimationTimer.start();
    }

    private void createCoordinateLabels() {
        // Sütun başlıkları (0-9)
        columnHeadersPanel = new JPanel(new GridLayout(1, GRID_SIZE));
        columnHeadersPanel.setOpaque(false);

        for (int col = 0; col < GRID_SIZE; col++) {
            JLabel label = new JLabel(Integer.toString(col), JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            label.setForeground(Color.WHITE);
            columnHeadersPanel.add(label);
        }

        // Satır başlıkları (0-9)
        rowHeadersPanel = new JPanel(new GridLayout(GRID_SIZE, 1));
        rowHeadersPanel.setOpaque(false);

        for (int row = 0; row < GRID_SIZE; row++) {
            JLabel label = new JLabel(" " + row + " ", JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            label.setForeground(Color.WHITE);
            rowHeadersPanel.add(label);
        }
    }


    private void initializeButtons(JPanel gridPanel) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                final int currentRow = row;
                final int currentCol = col;
                
                JButton button = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        // Kenarları yuvarlak dikdörtgen çiz
                        RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                            0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                        
                        g2d.setPaint(getBackground());
                        g2d.fill(roundRect);
                        
                        // İkon varsa çiz
                        if (getIcon() != null) {
                            getIcon().paintIcon(this, g, 
                                (getWidth() - getIcon().getIconWidth()) / 2, 
                                (getHeight() - getIcon().getIconHeight()) / 2);
                        }
                        
                        // Kenar çizgisi
                        g2d.setColor(new Color(0, 0, 0, 80));
                        g2d.draw(roundRect);
                    }
                };
                
                button.setPreferredSize(new Dimension(40, 40));
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
                button.setFocusPainted(false);
                button.setOpaque(false);
                button.setBackground(WATER_COLOR);
                button.setActionCommand(row + "," + col);
                
                if (isOpponentBoard) {
                    button.addActionListener(this);
                    button.setEnabled(false); // Başlangıçta pasif
                    
                    // Fare üzerine gelme efekti
                    button.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            if (button.isEnabled()) {
                                button.setBackground(WATER_HIGHLIGHT);
                                button.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                                button.setIcon(getTargetIcon());
                                button.repaint();
                            }
                        }
                        
                        @Override
                        public void mouseExited(MouseEvent e) {
                            CellStatus status = board.getCellStatus(currentRow, currentCol);
                            updateButtonAppearance(button, status, gameClient.isPlayerTurn());
                        }
                    });
                } else {
                    button.setEnabled(false); // Kendi tahtamız her zaman pasif
                }
                
                buttons[row][col] = button;
                gridPanel.add(button);
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isOpponentBoard || !gameClient.isPlayerTurn()) {
            return;
        }
        
        String command = e.getActionCommand();
        String[] parts = command.split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        
        CellStatus currentStatus = board.getCellStatus(row, col);
        if (currentStatus == CellStatus.HIT || currentStatus == CellStatus.MISS || currentStatus == CellStatus.SUNK) {
            showTooltipNearComponent("Bu hücreye zaten ateş ettiniz!", buttons[row][col]);
            return;
        }
        
        // Atış animasyonu
        playFireAnimation(row, col);
        
        System.out.println("Ateş ediliyor: (" + row + ", " + col + ")");
        gameClient.sendFireCommand(row, col);
        setTurnActive(false);
    }
    
    private void playFireAnimation(int row, int col) {
        final JButton button = buttons[row][col];
        final Timer timer = new Timer(50, null);
        final int[] frame = {0};
        final Color[] animColors = {
            new Color(255, 255, 0),
            new Color(255, 200, 0),
            new Color(255, 150, 0),
            new Color(255, 100, 0),
            new Color(255, 50, 0)
        };
        
        timer.addActionListener(e -> {
            if (frame[0] < animColors.length) {
                button.setBackground(animColors[frame[0]]);
                frame[0]++;
            } else {
                timer.stop();
            }
        });
        
        timer.start();
    }
    
    private void showTooltipNearComponent(String message, JComponent component) {
        JToolTip tooltip = component.createToolTip();
        tooltip.setTipText(message);
        
        PopupFactory factory = PopupFactory.getSharedInstance();
        Point p = component.getLocationOnScreen();
        
        final Popup popup = factory.getPopup(component, tooltip, 
            p.x + component.getWidth()/2, p.y - 30);
        popup.show();
        
        // 2 saniye sonra otomatik kapat
        Timer timer = new Timer(2000, e -> popup.hide());
        timer.setRepeats(false);
        timer.start();
    }
    
    public void refreshBoard() {
        boolean isMyTurn = gameClient.isPlayerTurn();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                CellStatus status = board.getCellStatus(row, col);
                updateButtonAppearance(buttons[row][col], status, isMyTurn);
            }
        }
        revalidate();
        repaint();
    }
    
    private void updateButtonAppearance(JButton button, CellStatus status, boolean isPlayerTurnNow) {
        button.setText("");
        button.setIcon(null);
        boolean enableButton = false;
        
        switch (status) {
            case EMPTY:
                button.setBackground(WATER_COLOR);
                enableButton = isOpponentBoard && isPlayerTurnNow;
                break;
            case SHIP:
                if (!isOpponentBoard) {
                    button.setBackground(SHIP_COLOR);
                    button.setIcon(getShipIcon());
                } else {
                    button.setBackground(WATER_COLOR);
                    enableButton = isPlayerTurnNow;
                }
                break;
            case HIT:
                button.setBackground(HIT_COLOR);
                button.setIcon(getExplosionIcon());
                break;
            case MISS:
                button.setBackground(MISS_COLOR);
                button.setIcon(getSplashIcon());
                break;
            case SUNK:
                button.setBackground(SUNK_COLOR);
                button.setIcon(getShipSunkIcon());
                break;
            default:
                button.setBackground(WATER_COLOR);
                break;
        }
        
        button.setEnabled(enableButton);
        button.repaint();
    }
    
    public void setTurnActive(boolean isActive) {
        if (!isOpponentBoard) return;
        refreshBoard();
    }
    
    // İkonlar için lazy-loading metotları
    private ImageIcon getTargetIcon() {
        return getCachedIcon("target", () -> {
            int size = 24;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Hedef işareti
            g2d.setColor(new Color(255, 0, 0, 180));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawOval(4, 4, size - 8, size - 8);
            
            // + işareti
            g2d.drawLine(size/2, 2, size/2, size/2 - 4);
            g2d.drawLine(size/2, size/2 + 4, size/2, size - 2);
            g2d.drawLine(2, size/2, size/2 - 4, size/2);
            g2d.drawLine(size/2 + 4, size/2, size - 2, size/2);
            
            g2d.dispose();
            return new ImageIcon(image);
        });
    }
    
    private ImageIcon getExplosionIcon() {
        return getCachedIcon("explosion", () -> {
            int size = 24;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Patlama efekti
            g2d.setColor(new Color(255, 220, 0));
            g2d.fillOval(size/4, size/4, size/2, size/2);
            
            g2d.setColor(new Color(255, 60, 0));
            int rays = 8;
            for (int i = 0; i < rays; i++) {
                double angle = 2 * Math.PI * i / rays;
                int x1 = size/2 + (int)(size/4 * Math.cos(angle));
                int y1 = size/2 + (int)(size/4 * Math.sin(angle));
                int x2 = size/2 + (int)(size/2 * Math.cos(angle));
                int y2 = size/2 + (int)(size/2 * Math.sin(angle));
                g2d.drawLine(x1, y1, x2, y2);
            }
            
            g2d.dispose();
            return new ImageIcon(image);
        });
    }
    
    private ImageIcon getSplashIcon() {
        return getCachedIcon("splash", () -> {
            int size = 24;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Su sıçraması
            g2d.setColor(new Color(255, 255, 255, 220));
            
            // Birkaç damlacık çiz
            g2d.fillOval(size/2 - 2, size/2 - 2, 4, 4);
            g2d.fillOval(size/2 - 6, size/2 + 2, 3, 3);
            g2d.fillOval(size/2 + 4, size/2 - 4, 3, 3);
            g2d.fillOval(size/2 + 2, size/2 + 6, 2, 2);
            g2d.fillOval(size/2 - 8, size/2 - 5, 2, 2);
            
            g2d.dispose();
            return new ImageIcon(image);
        });
    }
    
    private ImageIcon getShipIcon() {
        return getCachedIcon("ship", () -> {
            int size = 24;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Gemi gövdesi
            g2d.setColor(new Color(60, 60, 60));
            g2d.fillRoundRect(2, size/3, size - 4, size/3, 5, 5);
            
            // Komuta kulesi
            g2d.setColor(new Color(90, 90, 90));
            g2d.fillOval(size/2 - 4, size/2 - 4, 8, 8);
            
            g2d.dispose();
            return new ImageIcon(image);
        });
    }
    
    private ImageIcon getShipSunkIcon() {
        return getCachedIcon("shipSunk", () -> {
            int size = 24;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Batmış gemi gövdesi
            g2d.setColor(new Color(50, 50, 50));
            g2d.fillRoundRect(2, size/3, size - 4, size/3, 5, 5);
            
            // X işareti
            g2d.setColor(new Color(200, 0, 0));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(6, 6, size - 6, size - 6);
            g2d.drawLine(size - 6, 6, 6, size - 6);
            
            g2d.dispose();
            return new ImageIcon(image);
        });
    }
    
    // İkon önbellek mekanizması
    private ImageIcon getCachedIcon(String key, IconSupplier supplier) {
        if (!iconCache.containsKey(key)) {
            iconCache.put(key, supplier.get());
        }
        return iconCache.get(key);
    }
    
    // İkon oluşturucu fonksiyonel arayüz
    @FunctionalInterface
    private interface IconSupplier {
        ImageIcon get();
    }
    
    // Component-specific paint desteği için override
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Arka plan denizi
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        GradientPaint gp = new GradientPaint(
            0, 0, new Color(10, 30, 70), 
            0, getHeight(), new Color(0, 70, 120));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}