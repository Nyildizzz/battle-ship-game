package client.ui;

import client.GameClient;
// Board importu gerekiyorsa ekleyin: import shared.Board;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameFrame extends JFrame {
    private final GameClient gameClient;
    private BoardPanel playerBoardPanel;
    private BoardPanel opponentBoardPanel;
    private JLabel statusLabel;

    public GameFrame(GameClient gameClient) {
        this.gameClient = gameClient;
        initializeFrame();
        updateTitleWithClientId();
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    }

    private void initializeFrame() {
        String initialTitle = "Amiral Battı - Bağlanılıyor..."; // Varsayılan başlık
        setTitle(initialTitle);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500); // Sabit boyuta geri dönüldü
        setLocationRelativeTo(null); // Ekranın ortasında başlar
        setLayout(new BorderLayout(10, 10)); // Ana layout, bileşenler arası 10px boşluk




        // --- Durum Etiketi ---
        statusLabel = new JLabel("Oyun başlıyor...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(statusLabel, BorderLayout.NORTH);

        JPanel boardsPanel = new JPanel(new GridLayout(1, 2, 20, 10)); // Önceki boşluk değerleri

        // Oyuncu tahtası
        playerBoardPanel = new BoardPanel(gameClient.getPlayerBoard(), false, gameClient);
        // Rakip tahtası (tıklanabilir)
        opponentBoardPanel = new BoardPanel(gameClient.getOpponentBoard(), true, gameClient);

        boardsPanel.add(createTitledPanel("Senin Alanın", playerBoardPanel));
        boardsPanel.add(createTitledPanel("Rakip Alanı", opponentBoardPanel));

        add(boardsPanel, BorderLayout.CENTER);

    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Mesaj", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage, "Hata", JOptionPane.ERROR_MESSAGE);
    }
    public void showGameOver(boolean isWin) {
        String message = isWin ? "Tebrikler! Oyunu kazandınız!" : "Maalesef kaybettiniz.";
        JOptionPane.showMessageDialog(this, message, "Oyun Bitti", JOptionPane.INFORMATION_MESSAGE);
        // Oyun bitince pencereyi kapatabiliriz
        dispose();
    }

    public void updateBoards() {
        if (playerBoardPanel != null) {
            playerBoardPanel.refreshBoard();
        }
        if (opponentBoardPanel != null) {
            opponentBoardPanel.refreshBoard();
        }
    }

    // Panellere başlık eklemek için yardımcı metodun önceki hali
    private JPanel createTitledPanel(String title, JPanel contentPanel) {
        JPanel titledPanel = new JPanel(new BorderLayout());
        // Sadece TitledBorder kullanılıyor, ekstra boşluk veya font yok
        titledPanel.setBorder(BorderFactory.createTitledBorder(title));
        // contentPanel'e eklenen border kaldırıldı.
        titledPanel.add(contentPanel, BorderLayout.CENTER);
        return titledPanel;
    }

     /**
      * Pencere başlığını istemci ID'si ile günceller.
      * GameClient tarafından ID atandıktan sonra çağrılır.
      */
     public void updateTitleWithClientId() {
         // SwingUtilities.invokeLater GUI güncellemelerinin EDT üzerinde yapılmasını sağlar.
         javax.swing.SwingUtilities.invokeLater(() -> {
            int clientId = gameClient.getClientId();
            if (clientId != -1) {
                 setTitle("Amiral Battı - Oyuncu " + clientId);
            } else {
                 // Başlangıçta veya ID alınamazsa gösterilecek başlık
                 // initializeFrame'deki mantıkla tutarlı olabilir.
                 setTitle("Amiral Battı - ID Bekleniyor...");
            }
         });
     }

    /**
     * Oyun sırası değiştiğinde veya UI'ın güncellenmesi gerektiğinde çağrılır.
     * Durum etiketini günceller ve rakip tahtasının tıklanabilirliğini ayarlar.
     * @param isMyTurn Sıranın mevcut oyuncuda olup olmadığı.
     */
    public void updateTurnStatusUI(boolean isMyTurn) {
         javax.swing.SwingUtilities.invokeLater(() -> {
            // isGameOver kontrolü kaldırıldı (varsa)
            // Renk değişiklikleri kaldırıldı
             System.out.println("Sıra durumu güncelleniyor: " + (isMyTurn ? "Senin Sıra" : "Rakip Sırası"));
            String statusText;
            if (isMyTurn) {
                statusText = "Sıra Sende! Rakip tahtasına ateş et.";
            } else {
                // isOpponentReady kontrolü kaldırıldı (varsa)
                statusText = "Rakibin Sırası Bekleniyor...";
            }
            statusLabel.setText(statusText);
            statusLabel.setForeground(Color.BLACK); // Rengi varsayılana döndür

            // Rakip tahtasının tıklanabilirliğini ayarla
            if (opponentBoardPanel != null) {
                 opponentBoardPanel.setTurnActive(isMyTurn);
            }
         });
    }

}