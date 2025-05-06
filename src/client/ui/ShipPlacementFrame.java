package client.ui;

import client.GameClient;
import shared.Ship;
import shared.ShipType;

import javax.swing.*;
import java.awt.*;
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

    public ShipPlacementFrame(GameClient gameClient) {
        Objects.requireNonNull(gameClient, "GameClient cannot be null");
        this.gameClient = gameClient;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

		// Mevcut başlatmalar...
		shipPlaced = new boolean[shipSizes.length];
		// ...

        initComponents();
        setupLayout();
    }

private void initComponents() {
    // Durum etiketi
    statusLabel = new JLabel("Lütfen gemilerinizi yerleştirin", JLabel.CENTER);
    statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
    statusLabel.setForeground(new Color(25, 25, 112)); // Koyu mavi

    // Tahta paneli
    boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
    boardButtons = new JButton[BOARD_SIZE][BOARD_SIZE];

    // Tahta butonlarını oluştur
    for (int i = 0; i < BOARD_SIZE; i++) {
        for (int j = 0; j < BOARD_SIZE; j++) {
            final int row = i;
            final int col = j;
            boardButtons[i][j] = new JButton();
            boardButtons[i][j].setPreferredSize(new Dimension(40, 40));
            boardButtons[i][j].setBackground(new Color(173, 216, 230)); // Açık mavi
            boardButtons[i][j].setFocusPainted(false);
            boardButtons[i][j].setBorderPainted(true);
            boardButtons[i][j].addActionListener(e -> handleBoardClick(row, col));
            boardPanel.add(boardButtons[i][j]);
        }
    }

    // Gemi seçim paneli
    shipSelectionPanel = new JPanel();
    shipSelectionPanel.setLayout(new BoxLayout(shipSelectionPanel, BoxLayout.Y_AXIS));
    shipSelectionPanel.setBorder(BorderFactory.createTitledBorder("Gemiler"));

    JPanel shipButtonsPanel = new JPanel(new GridLayout(shipNames.length, 1, 5, 5));
    for (int i = 0; i < shipNames.length; i++) {
        final int index = i;
        JButton shipButton = new JButton(shipNames[i]);
        shipButton.addActionListener(e -> selectShip(index));

        // İlk gemi seçili olarak başlar
        if (i == 0) {
            shipButton.setBackground(new Color(144, 238, 144)); // Açık yeşil
        }

        shipButtonsPanel.add(shipButton);
    }

    // Yön seçimi
    JPanel orientationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    orientationPanel.add(new JLabel("Yön:"));
    orientationComboBox = new JComboBox<>(new String[]{"Yatay", "Dikey"});
    orientationPanel.add(orientationComboBox);

    // Hazır butonu
    readyButton = new JButton("Hazırım");
    readyButton.setEnabled(false); // Başlangıçta devre dışı
    readyButton.addActionListener(e -> sendReady());

    JPanel controlPanel = new JPanel(new BorderLayout());
    controlPanel.add(orientationPanel, BorderLayout.NORTH);
    controlPanel.add(readyButton, BorderLayout.SOUTH);

    shipSelectionPanel.add(shipButtonsPanel);
    shipSelectionPanel.add(Box.createVerticalStrut(20));
    shipSelectionPanel.add(controlPanel);
}





    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Ana bileşenleri ekle
        add(statusLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(20, 0));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tahta panelini ortada göster
        JPanel boardWrapper = new JPanel(new BorderLayout());
        boardWrapper.setBorder(BorderFactory.createTitledBorder("Oyun Tahtası"));
        boardWrapper.add(boardPanel, BorderLayout.CENTER);
        centerPanel.add(boardWrapper, BorderLayout.CENTER);

        // Gemi seçim panelini sağda göster
        centerPanel.add(shipSelectionPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void selectShip(int index) {
        selectedShipIndex = index;

        // Tüm gemi butonlarını normal renge getir
        for (int i = 0; i < shipSelectionPanel.getComponentCount(); i++) {
            if (shipSelectionPanel.getComponent(i) instanceof JPanel) {
                JPanel panel = (JPanel) shipSelectionPanel.getComponent(i);
                for (int j = 0; j < panel.getComponentCount(); j++) {
                    if (panel.getComponent(j) instanceof JButton) {
                        panel.getComponent(j).setBackground(null);
                    }
                }
            }
        }

        // Seçili gemi butonunu vurgula
        JPanel shipButtonsPanel = (JPanel) shipSelectionPanel.getComponent(0);
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
    
    if (width <= 0) width = 40;
    if (height <= 0) height = 40;
    
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
                button.setBackground(new Color(173, 216, 230)); // Açık mavi
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