package client.ui;

import client.GameClient;
// Board importu gerekiyorsa ekleyin: import shared.Board;

import javax.swing.*;
import java.awt.*;

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

        // Ana content pane'e eklenen dış kenar boşluğu kaldırıldı.

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

    /**
     * Her iki tahtayı da yenilemek için çağrılır (örn. bir vuruş sonrası).
     */
    public void refreshBoards() {
        // Bu metodun içeriği genellikle aynı kalır, BoardPanel'leri yeniler.
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (playerBoardPanel != null) {
                playerBoardPanel.refreshBoard();
            }
            if (opponentBoardPanel != null) {
                opponentBoardPanel.refreshBoard();
            }
            // Sıra durumu label'ı tahta yenilendikten sonra güncellensin
             if (gameClient != null) { // gameClient null check eklendi
                  updateTurnStatusUI(gameClient.isPlayerTurn());
             }
        });
    }

    // Oyun bittiğinde çağrılacak metod
    public void showGameOver(String message) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            // setGameOver kaldırıldı (varsa)
            statusLabel.setText("Oyun Bitti!");
            statusLabel.setForeground(Color.RED); // Oyun sonu rengi kalabilir veya siyah yapılabilir
             if (opponentBoardPanel != null) { // Null check eklendi
                 opponentBoardPanel.setTurnActive(false); // Tıklamayı kapat
             }
            JOptionPane.showMessageDialog(this, message, "Oyun Sonu", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // Rakip ayrıldığında çağrılacak metod
    public void showOpponentLeft() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            // setGameOver kaldırıldı (varsa)
            statusLabel.setText("Rakip Oyundan Ayrıldı.");
            statusLabel.setForeground(Color.MAGENTA); // Renk kalabilir veya siyah yapılabilir
             if (opponentBoardPanel != null) { // Null check eklendi
                 opponentBoardPanel.setTurnActive(false); // Tıklamayı kapat
             }
            JOptionPane.showMessageDialog(this, "Rakibiniz oyundan ayrıldı. Oyun sona erdi.", "Rakip Ayrıldı", JOptionPane.WARNING_MESSAGE);
        });
    }

    // Gemi yerleştirme veya başlangıç ekranından oyun ekranına geçiş
    public void showGameScreen() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            System.out.println("Oyun ekranı gösteriliyor.");
            // Zaten görünür olduğu için belki sadece durumu güncellemek yeterli
             if (gameClient != null) { // Null check eklendi
                 updateTurnStatusUI(gameClient.isPlayerTurn());
             }
            // pack() kaldırıldı
            repaint(); // Repaint kalabilir
        });
    }
}