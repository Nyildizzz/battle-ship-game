package client.ui;

import client.GameClient;
import shared.Board;
import shared.CellStatus;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BoardPanel extends JPanel implements ActionListener {

    private static final int GRID_SIZE = 10; // Tahta boyutu (10x10)
    private final Board board; // Bu panelin gösterdiği tahta verisi
    private final boolean isOpponentBoard; // Bu panel rakip tahtası mı? (Tıklanabilirlik için)
    private final GameClient gameClient; // Oyun mantığına erişim için
    private final JButton[][] buttons; // Grid hücrelerini temsil eden butonlar

    public BoardPanel(Board board, boolean isOpponentBoard, GameClient gameClient) {
        this.board = board;
        this.isOpponentBoard = isOpponentBoard; // 'clickable' yerine daha açıklayıcı bir isim
        this.gameClient = gameClient;
        this.buttons = new JButton[GRID_SIZE][GRID_SIZE];

        setLayout(new GridLayout(GRID_SIZE, GRID_SIZE));
        initializeButtons();
        refreshBoard(); // Başlangıç durumunu butonlara yansıt
    }

    private void initializeButtons() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(35, 35));
                button.setFont(new Font("Arial", Font.BOLD, 16));
                button.setActionCommand(row + "," + col);

                if (isOpponentBoard) {
                    button.addActionListener(this);
                    // Başlangıçta butonların etkinliği setTurnActive ile ayarlanacak
                    button.setEnabled(false); // Başlangıçta pasif yapalım
                } else {
                    button.setEnabled(false); // Kendi tahtamız her zaman pasif
                }

                buttons[row][col] = button;
                add(button);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Bu kontrol hala önemli, çift tıklama veya beklenmedik durumlar için
        if (!isOpponentBoard || !gameClient.isPlayerTurn()) {
            // İsteğe bağlı: Burada tekrar mesaj göstermeye gerek olmayabilir,
            // çünkü buton zaten etkin olmayacak. Ama güvenlik için kalabilir.
             JOptionPane.showMessageDialog(this, "Sıra rakipte!", "Bekle", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String command = e.getActionCommand();
        String[] parts = command.split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);

        CellStatus currentStatus = board.getCellStatus(row, col);
        if (currentStatus == CellStatus.HIT || currentStatus == CellStatus.MISS || currentStatus == CellStatus.SUNK) {
             JOptionPane.showMessageDialog(this, "Bu hücreye zaten ateş ettiniz!", "Geçersiz Hamle", JOptionPane.WARNING_MESSAGE);
            // Buton zaten pasif olmalı, bu mesaj da isteğe bağlı.
            return;
        }

        System.out.println("Ateş ediliyor: (" + row + ", " + col + ")");
        gameClient.sendFireCommand(row, col);
        // Ateş ettikten sonra sıra değişene kadar tekrar tıklamayı engellemek için
        // tüm tahtayı pasif hale getirebiliriz (opsiyonel ama iyi bir pratik).
         setTurnActive(false); // Sunucudan cevap gelene kadar tekrar tıklamayı önler
    }

    public void refreshBoard() {
        boolean isMyTurn = gameClient.isPlayerTurn(); // Mevcut sıra durumunu al
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                CellStatus status = board.getCellStatus(row, col);
                JButton button = buttons[row][col];
                updateButtonAppearance(button, status, isMyTurn); // Sıra bilgisini de gönder
            }
        }
        revalidate();
        repaint();
    }

    // updateButtonAppearance metodunu sıra bilgisini alacak şekilde güncelle
    private void updateButtonAppearance(JButton button, CellStatus status, boolean isPlayerTurnNow) {
        button.setText("");
        button.setBackground(null); // Reset background
        boolean enableButton = false; // Buton etkinleştirilecek mi?

        switch (status) {
            case EMPTY:
            case SHIP: // Rakip tahtasındaki SHIP, oyuncu için EMPTY gibidir
                button.setBackground(Color.CYAN);
                // Sadece rakip tahtasındaysa, boşsa ve sıra oyuncudaysa etkinleştir
                enableButton = isOpponentBoard && isPlayerTurnNow;
                break;
            case HIT:
                button.setBackground(Color.RED);
                button.setText("X");
                enableButton = false; // Vurulan yerler her zaman pasif
                break;
            case MISS:
                button.setBackground(Color.BLUE);
                button.setText("O");
                enableButton = false; // Iska geçilen yerler her zaman pasif
                break;
            case SUNK:
                button.setBackground(Color.DARK_GRAY);
                button.setText("X");
                enableButton = false; // Batırılan yerler her zaman pasif
                break;
            default:
                button.setBackground(null);
                enableButton = false;
                break;
        }

        // Kendi tahtamızdaysa gemileri farklı göster (isteğe bağlı)
        if (!isOpponentBoard && status == CellStatus.SHIP) {
            button.setBackground(Color.GRAY);
        }

        button.setEnabled(enableButton);
    }

    /**
     * Rakip tahtasındaki tıklanabilir butonların etkinliğini ayarlar.
     * Bu metod genellikle sıra değiştiğinde GameClient veya GameFrame tarafından çağrılır.
     * @param isActive Sıra mevcut oyuncudaysa true, değilse false.
     */
    public void setTurnActive(boolean isActive) {
        if (!isOpponentBoard) return; // Sadece rakip tahtası için geçerli

        // refreshBoard'u çağırmak, her butonun durumunu
        // hem hücre durumuna hem de sıra durumuna göre doğru şekilde ayarlayacaktır.
        refreshBoard();

        // Alternatif (Sadece enable/disable yapmak isterseniz):
        /*
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                CellStatus status = board.getCellStatus(row, col);
                // Sadece vurulmamış hücrelerin etkinliğini değiştir
                boolean canBeClicked = (status == CellStatus.EMPTY || status == CellStatus.SHIP);
                if (canBeClicked) {
                    buttons[row][col].setEnabled(isActive);
                } else {
                    buttons[row][col].setEnabled(false); // Vurulmuşlar her zaman pasif
                }
            }
        }
        */
    }
}