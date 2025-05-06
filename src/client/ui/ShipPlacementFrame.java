package client.ui;

import client.GameClient;
import shared.Ship;
import shared.ShipType;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.GradientPaint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShipPlacementFrame extends JFrame {
    private final GameClient gameClient;
    private JPanel boardPanel;
    private JButton[][] boardButtons;
    private JPanel shipSelectionPanel;
    private JLabel statusLabel;
    private JButton readyButton;
    private JComboBox<String> orientationComboBox;

    private static final int BOARD_SIZE = 10;

    // Gemi boyutları ve sayıları
    private final int[] shipSizes = {5, 4, 3, 3, 2};
    private final String[] shipNames = {
            "Uçak Gemisi (" + ShipType.CARRIER.getSize() + ")",
            "Savaş Gemisi (" + ShipType.BATTLESHIP.getSize() + ")",
            "Kruvazör (" + ShipType.CRUISER.getSize() + ")",
            "Denizaltı (" + ShipType.SUBMARINE.getSize() + ")",
            "Mayın Gemisi (" + ShipType.DESTROYER.getSize() + ")"
    };

    private int selectedShipIndex = 0;

    // Yerleştirilen gemilerin pozisyonlarını tutar
    private List<Ship> placedShips = new ArrayList<>();

    // Gemi yerleştirme ilgili durum
    private boolean[][] boardState = new boolean[BOARD_SIZE][BOARD_SIZE];

	private boolean[] shipPlaced; // Hangi gemilerin yerleştirildiğini takip etmek için

    // Mevcut alanlar...
    
    private Color lightBlue = new Color(173, 216, 230);
    private Color darkBlue = new Color(25, 25, 112);
    private Color hoverBlue = new Color(135, 206, 250);
    private Color waterBackground = new Color(0, 105, 148);

    public ShipPlacementFrame(GameClient gameClient) {
        Objects.requireNonNull(gameClient, "GameClient cannot be null");
        this.gameClient = gameClient;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setTitle("Amiral Battı - Gemi Yerleştirme");
        setIconImage(createGameIcon().getImage());

		// Mevcut başlatmalar...
		shipPlaced = new boolean[shipSizes.length];
		// ...

        initComponents();
        setupLayout();
        
        // Arka plan rengi
        getContentPane().setBackground(new Color(240, 248, 255));
    }
    
    private ImageIcon createGameIcon() {
        int size = 32;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Kenar yumuşatma
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Daire çiz
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

    private void initComponents() {
        // Durum etiketi
        statusLabel = new JLabel("Lütfen gemilerinizi yerleştirin", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusLabel.setForeground(darkBlue);
        statusLabel.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200)), 
            new EmptyBorder(8, 0, 8, 0)
        ));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(240, 248, 255));

        // Tahta paneli
        boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Deniz arka planı - dalga efekti
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(0, 105, 148), 
                    0, h, new Color(0, 150, 200)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        boardPanel.setBorder(new LineBorder(darkBlue, 2));

        // Koordinat göstergeleri
        JPanel boardWithCoordinates = new JPanel(new BorderLayout(0, 0));
        JPanel hCoord = new JPanel(new GridLayout(1, BOARD_SIZE));
        JPanel vCoord = new JPanel(new GridLayout(BOARD_SIZE, 1));
        
        for (int i = 0; i < BOARD_SIZE; i++) {
            JLabel hLabel = new JLabel(Character.toString((char)('A' + i)), JLabel.CENTER);
            hLabel.setFont(new Font("Arial", Font.BOLD, 14));
            hLabel.setForeground(darkBlue);
            hCoord.add(hLabel);
            
            JLabel vLabel = new JLabel(Integer.toString(i+1), JLabel.CENTER);
            vLabel.setFont(new Font("Arial", Font.BOLD, 14));
            vLabel.setForeground(darkBlue);
            vCoord.add(vLabel);
        }
        
        hCoord.setBackground(new Color(230, 240, 250));
        vCoord.setBackground(new Color(230, 240, 250));
        
        boardWithCoordinates.add(hCoord, BorderLayout.NORTH);
        boardWithCoordinates.add(vCoord, BorderLayout.WEST);
        boardWithCoordinates.add(boardPanel, BorderLayout.CENTER);
        
        boardButtons = new JButton[BOARD_SIZE][BOARD_SIZE];

        // Tahta butonlarını oluştur
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                final int row = i;
                final int col = j;
                boardButtons[i][j] = new JButton();
                boardButtons[i][j].setPreferredSize(new Dimension(45, 45));
                boardButtons[i][j].setBackground(lightBlue);
                boardButtons[i][j].setFocusPainted(false);
                boardButtons[i][j].setBorderPainted(true);
                boardButtons[i][j].setBorder(new LineBorder(new Color(100, 100, 100), 1));
                boardButtons[i][j].addActionListener(e -> handleBoardClick(row, col));
                
                // Fare üzerine gelme efekti
                boardButtons[i][j].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!boardState[row][col]) {
                            boardButtons[row][col].setBackground(hoverBlue);
                            previewShipPlacement(row, col);
                        }
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!boardState[row][col]) {
                            boardButtons[row][col].setBackground(lightBlue);
                            clearShipPreview();
                        }
                    }
                });
                
                boardPanel.add(boardButtons[i][j]);
            }
        }

        // Gemi seçim paneli
        shipSelectionPanel = new JPanel();
        shipSelectionPanel.setLayout(new BoxLayout(shipSelectionPanel, BoxLayout.Y_AXIS));
        shipSelectionPanel.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(darkBlue, 2), 
            "Filo", 
            TitledBorder.CENTER, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16),
            darkBlue
        ));
        shipSelectionPanel.setBackground(new Color(240, 248, 255));

        JPanel shipButtonsPanel = new JPanel(new GridLayout(shipNames.length, 1, 5, 10));
        shipButtonsPanel.setOpaque(false);
        
        // Gemi resimleri için ikonları ve butonları hazırla
        for (int i = 0; i < shipNames.length; i++) {
            final int index = i;
            JButton shipButton = new JButton(shipNames[i]);
            shipButton.setFont(new Font("Arial", Font.BOLD, 14));
            shipButton.setForeground(darkBlue);
            shipButton.setIcon(createShipButtonIcon(shipSizes[i]));
            shipButton.setHorizontalTextPosition(SwingConstants.RIGHT);
            shipButton.setIconTextGap(10);
            shipButton.addActionListener(e -> selectShip(index));
            shipButton.setFocusPainted(false);
            shipButton.setBorder(new CompoundBorder(
                new LineBorder(new Color(100, 100, 100), 1),
                new EmptyBorder(5, 10, 5, 10)
            ));

            // İlk gemi seçili olarak başlar
            if (i == 0) {
                shipButton.setBackground(new Color(144, 238, 144)); // Açık yeşil
            } else {
                shipButton.setBackground(new Color(240, 240, 240));
            }

            shipButtonsPanel.add(shipButton);
        }

        // Yön seçimi
        JPanel orientationPanel = new JPanel();
        orientationPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        orientationPanel.setOpaque(false);
        orientationPanel.add(new JLabel("Yerleştirme Yönü:"));
        orientationComboBox = new JComboBox<>(new String[]{"Yatay", "Dikey"});
        orientationComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        orientationComboBox.setPreferredSize(new Dimension(100, 30));
        orientationComboBox.addActionListener(e -> clearShipPreview());
        orientationPanel.add(orientationComboBox);

        // Rastgele yerleştirme butonu
        JButton randomPlaceButton = new JButton("Rastgele Yerleştir");
        randomPlaceButton.setFont(new Font("Arial", Font.BOLD, 14));
        randomPlaceButton.addActionListener(e -> randomPlaceShips());
        randomPlaceButton.setBackground(new Color(255, 215, 0)); // Altın rengi
        randomPlaceButton.setForeground(new Color(139, 69, 19)); // Koyu kahverengi
        randomPlaceButton.setFocusPainted(false);
        randomPlaceButton.setBorder(new CompoundBorder(
            new LineBorder(new Color(139, 69, 19), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        // Temizle butonu
        JButton clearButton = new JButton("Temizle");
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.addActionListener(e -> clearAllShips());
        clearButton.setBackground(new Color(255, 99, 71)); // Domates rengi
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setBorder(new CompoundBorder(
            new LineBorder(new Color(178, 34, 34), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));

        // Hazır butonu
        readyButton = new JButton("Hazırım");
        readyButton.setFont(new Font("Arial", Font.BOLD, 16));
        readyButton.setEnabled(false); // Başlangıçta devre dışı
        readyButton.addActionListener(e -> sendReady());
        readyButton.setBackground(new Color(50, 205, 50)); // Parlak yeşil
        readyButton.setForeground(Color.WHITE);
        readyButton.setFocusPainted(false);
        readyButton.setBorder(new CompoundBorder(
            new LineBorder(new Color(34, 139, 34), 1),
            new EmptyBorder(8, 15, 8, 15)
        ));

        JPanel actionButtonsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionButtonsPanel.setOpaque(false);
        actionButtonsPanel.add(randomPlaceButton);
        actionButtonsPanel.add(clearButton);
        
        JPanel readyButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        readyButtonPanel.setOpaque(false);
        readyButtonPanel.add(readyButton);

        shipSelectionPanel.add(shipButtonsPanel);
        shipSelectionPanel.add(Box.createVerticalStrut(20));
        shipSelectionPanel.add(orientationPanel);
        shipSelectionPanel.add(Box.createVerticalStrut(15));
        shipSelectionPanel.add(actionButtonsPanel);
        shipSelectionPanel.add(Box.createVerticalStrut(30));
        shipSelectionPanel.add(readyButtonPanel);
    }

    private ImageIcon createShipButtonIcon(int shipSize) {
        int height = 30;
        int width = shipSize * 10;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Gemi gövdesi
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillRoundRect(0, 5, width, 20, 10, 10);
        
        // Komuta merkezi
        g2d.setColor(new Color(120, 120, 120));
        g2d.fillOval(width/2 - 8, height/2 - 8, 16, 16);
        
        g2d.dispose();
        return new ImageIcon(image);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Ana bileşenleri ekle
        add(statusLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(20, 0));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.setOpaque(false);

        // Oyun tahtası paneli
        JPanel boardWrapper = new JPanel(new BorderLayout());
        boardWrapper.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(darkBlue, 2), 
            "Savaş Alanı", 
            TitledBorder.CENTER, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16),
            darkBlue
        ));
        boardWrapper.setOpaque(false);
        boardWrapper.add(boardPanel, BorderLayout.CENTER);
        centerPanel.add(boardWrapper, BorderLayout.CENTER);

        // Gemi seçim panelini sağda göster
        centerPanel.add(shipSelectionPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);
        
        // Alt bilgi paneli
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        infoPanel.setBackground(new Color(240, 255, 255));
        
        JLabel infoLabel = new JLabel("İpucu: Gemiler birbirine temas edemez. Gemi yerleştirmek için bir kareye tıklayın.");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        infoLabel.setForeground(new Color(70, 130, 180));
        infoPanel.add(infoLabel);
        
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    // Fare üzerinde geçici önizleme göster
    private void previewShipPlacement(int row, int col) {
        int shipSize = shipSizes[selectedShipIndex];
        boolean isHorizontal = orientationComboBox.getSelectedIndex() == 0;
        
        if (!shipPlaced[selectedShipIndex] && canPlaceShip(row, col, shipSize, isHorizontal)) {
            showTemporaryShipPreview(row, col, shipSize, isHorizontal);
        }
    }
    
    private void showTemporaryShipPreview(int row, int col, int shipSize, boolean isHorizontal) {
        // Geçici gemi önizlemesi
        for (int i = 0; i < shipSize; i++) {
            int r = isHorizontal ? row : row + i;
            int c = isHorizontal ? col + i : col;
            
            if (r < BOARD_SIZE && c < BOARD_SIZE && !boardState[r][c]) {
                boardButtons[r][c].setBackground(new Color(144, 238, 144, 150)); // Yarı-saydam yeşil
            }
        }
    }
    
    private void clearShipPreview() {
        // Tüm geçici önizlemeleri temizle
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (!boardState[i][j]) {
                    boardButtons[i][j].setBackground(lightBlue);
                }
            }
        }
    }
    
    // Rastgele gemi yerleştirme
    private void randomPlaceShips() {
        // Önce tüm gemileri temizle
        clearAllShips();
        
        // Her gemi için rastgele yerleştirme dene
        for (int i = 0; i < shipSizes.length; i++) {
            selectedShipIndex = i;
            boolean placed = false;
            int maxAttempts = 100; // Sonsuz döngüye girmemek için
            
            while (!placed && maxAttempts > 0) {
                boolean isHorizontal = Math.random() > 0.5;
                int maxRow = isHorizontal ? BOARD_SIZE - 1 : BOARD_SIZE - shipSizes[i];
                int maxCol = isHorizontal ? BOARD_SIZE - shipSizes[i] : BOARD_SIZE - 1;
                
                int row = (int)(Math.random() * (maxRow + 1));
                int col = (int)(Math.random() * (maxCol + 1));
                
                if (canPlaceShip(row, col, shipSizes[i], isHorizontal)) {
                    placeShip(row, col, shipSizes[i], isHorizontal, ShipType.values()[i]);
                    shipPlaced[i] = true;
                    placed = true;
                }
                
                maxAttempts--;
            }
        }
        
        // Tüm gemileri yerleştirme kontrolü
        checkAllShipsPlaced();
    }
    
    // Tüm gemileri temizle
    private void clearAllShips() {
        // Tüm gemi yerleşimlerini sıfırla
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardState[i][j] = false;
                boardButtons[i][j].setIcon(null);
                boardButtons[i][j].setOpaque(true);
                boardButtons[i][j].setContentAreaFilled(true);
                boardButtons[i][j].setBorderPainted(true);
                boardButtons[i][j].setBackground(lightBlue);
                boardButtons[i][j].setEnabled(true);
            }
        }
        
        // Gemi butonlarını etkinleştir
        JPanel shipButtonsPanel = (JPanel) shipSelectionPanel.getComponent(0);
        for (int i = 0; i < shipPlaced.length; i++) {
            shipButtonsPanel.getComponent(i).setEnabled(true);
            shipPlaced[i] = false;
        }
        
        // Yerleştirilen gemileri temizle
        placedShips.clear();
        
        // İlk gemiyi seç
        selectShip(0);
        
        // Hazır butonunu devre dışı bırak
        readyButton.setEnabled(false);
        
        statusLabel.setText("Tüm gemiler temizlendi. Lütfen gemilerinizi yerleştirin.");
    }

private void selectShip(int index) {
    selectedShipIndex = index;

    // Tüm gemi butonlarını normal renge getir
    JPanel shipButtonsPanel = (JPanel) shipSelectionPanel.getComponent(0);
    for (int i = 0; i < shipButtonsPanel.getComponentCount(); i++) {
        JButton button = (JButton) shipButtonsPanel.getComponent(i);
        button.setBackground(new Color(240, 240, 240)); // Default background color
    }

    // Seçili gemi butonunu vurgula
    JButton selectedButton = (JButton) shipButtonsPanel.getComponent(selectedShipIndex);
    selectedButton.setBackground(new Color(144, 238, 144)); // Açık yeşil

    statusLabel.setText(shipNames[selectedShipIndex] + " gemisini yerleştirin");
}

private void handleBoardClick(int row, int col) {
    if (shipPlaced[selectedShipIndex]) {
        statusLabel.setText("Bu gemi zaten yerleştirildi! Başka bir gemi seçin.");
        return;
    }

    int shipSize = shipSizes[selectedShipIndex];
    boolean isHorizontal = orientationComboBox.getSelectedIndex() == 0;

    // Geçici olarak geminin görünümünü göster
    if (canPlaceShip(row, col, shipSize, isHorizontal)) {
        showTemporaryShip(row, col, shipSize, isHorizontal);

        int response = JOptionPane.showConfirmDialog(this,
            "Gemiyi buraya yerleştirmek istiyor musunuz?",
            "Yerleştirme Onayı",
            JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            placeShip(row, col, shipSize, isHorizontal, ShipType.values()[selectedShipIndex]);
            shipPlaced[selectedShipIndex] = true;

            // Sonraki yerleştirilmemiş gemiyi seç
            selectNextUnplacedShip();
        } else {
            // Geçici görünümü temizle
            clearTemporaryShip(row, col, shipSize, isHorizontal);
        }
    } else {
        statusLabel.setText("Gemi buraya yerleştirilemez! Başka bir yer deneyin.");
    }
}

private void placeShip(int startRow, int startCol, int shipSize, boolean isHorizontal, ShipType shipType) {
    // Yeni gemiyi oluştur
    Ship ship = new Ship(startRow, startCol, shipSize, isHorizontal, shipType);
    placedShips.add(ship);

    // Gemi desenini oluştur ve butonlarda göster
    for (int i = 0; i < shipSize; i++) {
        int row = isHorizontal ? startRow : startRow + i;
        int col = isHorizontal ? startCol + i : startCol;
        boardState[row][col] = true;
        
        JButton button = boardButtons[row][col];
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        
        // Gemi görünümünü ayarla
        button.setIcon(createShipIcon(i, shipSize, isHorizontal));
    }

    // Bu gemi butonunu devre dışı bırak çünkü yerleştirildi
    JPanel shipButtonsPanel = (JPanel) shipSelectionPanel.getComponent(0);
    shipButtonsPanel.getComponent(selectedShipIndex).setEnabled(false);
}

// Gemi ikonlarını oluştur
private ImageIcon createShipIcon(int position, int shipSize, boolean isHorizontal) {
    int width = boardButtons[0][0].getWidth();
    int height = boardButtons[0][0].getHeight();
    
    if (width <= 0) width = 45;
    if (height <= 0) height = 45;
    
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = image.createGraphics();
    
    // Kenar yumuşatma
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    // Gemi gövdesi için koyu gri renk
    Color shipColor = new Color(80, 80, 80);
    g2d.setColor(shipColor);
    
    int margin = 4;
    int shipWidth = isHorizontal ? width - 2 * margin : width - 2 * margin;
    int shipHeight = isHorizontal ? height - 2 * margin : height - 2 * margin;
    
    // Gemi gövdesini çiz
    if (isHorizontal) {
        if (position == 0) { // Başlangıç (baş kısmı)
            g2d.fillRoundRect(margin, margin, shipWidth, shipHeight, 10, 10);
            g2d.fillRect(shipWidth / 2, margin, shipWidth / 2, shipHeight);
        } else if (position == shipSize - 1) { // Bitiş (kıç kısmı)
            g2d.fillRoundRect(margin, margin, shipWidth, shipHeight, 10, 10);
            g2d.fillRect(margin, margin, shipWidth / 2, shipHeight);
        } else { // Orta kısım
            g2d.fillRect(margin, margin, shipWidth, shipHeight);
        }
    } else { // Dikey
        if (position == 0) { // Üst kısım
            g2d.fillRoundRect(margin, margin, shipWidth, shipHeight, 10, 10);
            g2d.fillRect(margin, shipHeight / 2, shipWidth, shipHeight / 2);
        } else if (position == shipSize - 1) { // Alt kısım
            g2d.fillRoundRect(margin, margin, shipWidth, shipHeight, 10, 10);
            g2d.fillRect(margin, margin, shipWidth, shipHeight / 2);
        } else { // Orta kısım
            g2d.fillRect(margin, margin, shipWidth, shipHeight);
        }
    }
    
    // Geminin ortasına yuvarlak bir komuta merkezi ekle
    if ((position == shipSize / 2) || (shipSize > 3 && position == 1)) {
        // Daha açık gri ton
        g2d.setColor(new Color(120, 120, 120));
        int circleSize = Math.min(width, height) / 3;
        int circleX = (width - circleSize) / 2;
        int circleY = (height - circleSize) / 2;
        g2d.fillOval(circleX, circleY, circleSize, circleSize);
    }
    
    g2d.dispose();
    return new ImageIcon(image);
}

private void showTemporaryShip(int row, int col, int shipSize, boolean isHorizontal) {
    // Geminin geçici görünümünü oluştur
    for (int i = 0; i < shipSize; i++) {
        int r = isHorizontal ? row : row + i;
        int c = isHorizontal ? col + i : col;
        
        if (r < BOARD_SIZE && c < BOARD_SIZE) {
            JButton button = boardButtons[r][c];
            if (!boardState[r][c]) {
                button.setIcon(createShipIcon(i, shipSize, isHorizontal));
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
            }
        }
    }
}

private void clearTemporaryShip(int row, int col, int shipSize, boolean isHorizontal) {
    for (int i = 0; i < shipSize; i++) {
        int r = isHorizontal ? row : row + i;
        int c = isHorizontal ? col + i : col;
        
        if (r < BOARD_SIZE && c < BOARD_SIZE) {
            JButton button = boardButtons[r][c];
            if (!boardState[r][c]) { // Eğer hücrede başka gemi yoksa
                button.setIcon(null);
                button.setOpaque(true);
                button.setContentAreaFilled(true);
                button.setBorderPainted(true);
                button.setBackground(lightBlue);
            }
        }
    }
}

private void selectNextUnplacedShip() {
    for (int i = 0; i < shipPlaced.length; i++) {
        if (!shipPlaced[i]) {
            selectShip(i);
            return;
        }
    }
    // Tüm gemiler yerleştirilmiş
    checkAllShipsPlaced();
}

    private boolean canPlaceShip(int startRow, int startCol, int shipSize, boolean isHorizontal) {
        // Tahtanın sınırları içinde mi kontrol et
        if (isHorizontal) {
            if (startCol + shipSize > BOARD_SIZE) return false;
        } else {
            if (startRow + shipSize > BOARD_SIZE) return false;
        }

        // Diğer gemilerle çakışma kontrolü
        for (int i = 0; i < shipSize; i++) {
            int row = isHorizontal ? startRow : startRow + i;
            int col = isHorizontal ? startCol + i : startCol;

            // Hücre zaten kullanılmış mı veya komşu hücrelerde gemi var mı kontrol et
            if (boardState[row][col] || hasAdjacentShip(row, col)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasAdjacentShip(int row, int col) {
        // Komşu hücrelerde gemi var mı kontrol et (çapraz hücreler dahil)
        for (int r = Math.max(0, row - 1); r <= Math.min(BOARD_SIZE - 1, row + 1); r++) {
            for (int c = Math.max(0, col - 1); c <= Math.min(BOARD_SIZE - 1, col + 1); c++) {
                if (boardState[r][c]) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkAllShipsPlaced() {
    boolean allPlaced = true;
    for (boolean placed : shipPlaced) {
        if (!placed) {
            allPlaced = false;
            break;
        }
    }

    readyButton.setEnabled(allPlaced);

    if (allPlaced) {
        statusLabel.setText("Tüm gemiler yerleştirildi! 'Hazırım' butonuna tıklayabilirsiniz.");
    }
}

private void sendReady() {
    if (gameClient == null) {
        statusLabel.setText("Hata: GameClient başlatılmamış!");
        return;
    }

    int clientId = gameClient.getClientId();

    StringBuilder shipData = new StringBuilder();

    for (Ship ship : placedShips) {
        String shipInfo = String.format("%d,%d,%d,%s",
            ship.getRow(), ship.getCol(), ship.getLength(), ship.isHorizontal() ? "H" : "V");
        shipData.append(shipInfo).append(";");

        System.out.printf("Gemi gönderiliyor: Satır=%d, Sütun=%d, Boyut=%d, Yatay mı=%b%n",
            ship.getRow(), ship.getCol(), ship.getLength(), ship.isHorizontal());
    }

    if (shipData.length() > 0 && shipData.charAt(shipData.length() - 1) == ';') {
        shipData.setLength(shipData.length() - 1);
    }

    String finalData = shipData.toString();
    System.out.println("Gönderen Client ID: " + clientId); // Loglama için kalabilir
    System.out.println("Gönderilen veri: " + finalData); // Artık clientId| içermeyecek
    gameClient.sendShipsReady(finalData);

    statusLabel.setText("Gemi yerleşimleri gönderildi. Rakip bekleniyor...");
    readyButton.setEnabled(false);
    disableBoard();
}

    private void disableBoard() {
        // Tahta hücrelerini devre dışı bırak
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardButtons[i][j].setEnabled(false);
            }
        }
    }



}