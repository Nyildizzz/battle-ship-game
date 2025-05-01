package shared;

import java.util.Arrays;

/**
 * Amiral Battı oyun tahtasını temsil eder.
 * Hücre durumlarını ve gemi yerleştirme/saldırı mantığını yönetir.
 */
public class Board {
    private static final int SIZE = 10; // Tahta boyutu (sabit)
    private final CellStatus[][] grid; // Hücre durumlarını tutan 2D dizi

    /**
     * Yeni bir Board nesnesi oluşturur ve tüm hücreleri EMPTY olarak başlatır.
     */
    public Board() {
        grid = new CellStatus[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            Arrays.fill(grid[i], CellStatus.EMPTY); // Tüm hücreleri başlangıçta EMPTY yap
        }
    }

    /**
     * Tahtanın boyutunu döndürür (genellikle 10).
     * @return Tahtanın boyutu.
     */
    public int getSize() {
        return SIZE;
    }

    /**
     * Belirtilen koordinattaki hücrenin durumunu döndürür.
     * Geçersiz koordinatlar için EMPTY döndürür.
     * @param row Satır indeksi (0-9).
     * @param col Sütun indeksi (0-9).
     * @return Hücrenin durumu (CellStatus).
     */
    public CellStatus getCellStatus(int row, int col) {
        if (isValidCoordinate(row, col)) {
            return grid[row][col];
        }
        // Geçersiz koordinat durumunda hata vermek yerine EMPTY döndürmek
        // bazı durumlarda daha güvenli olabilir. Veya exception fırlatılabilir.
        return CellStatus.EMPTY; // Veya null veya exception
    }

    /**
     * Belirtilen koordinata bir gemi yerleştirmeye çalışır.
     * Geminin sınırlara taşıp taşmadığını ve diğer gemilerle veya kenarlarıyla çakışıp çakışmadığını kontrol eder.
     *
     * @param ship   Yerleştirilecek gemi nesnesi (uzunluk ve pozisyon bilgisi içerir)
     * @return Yerleştirme başarılıysa true, değilse false.
     */
    public boolean placeShip(Ship ship) {
        int row = ship.getRow();
        int col = ship.getCol();
        int length = ship.getLength();
        boolean horizontal = ship.isHorizontal();

        // 1. Sınır Kontrolü
        if (!isValidPlacement(row, col, length, horizontal)) {
            return false;
        }

        // 2. Çakışma ve Komşuluk Kontrolü
        if (!canPlaceShipAt(row, col, length, horizontal)) {
            return false;
        }

        // 3. Yerleştirme
        for (int i = 0; i < length; i++) {
            if (horizontal) {
                grid[row][col + i] = CellStatus.SHIP;
                ship.addOccupiedCell(row, col + i); // Gemiye hangi hücreleri kapladığını söyle
            } else {
                grid[row + i][col] = CellStatus.SHIP;
                 ship.addOccupiedCell(row+i, col); // Gemiye hangi hücreleri kapladığını söyle
            }
        }
        return true;
    }

     /**
     * Belirtilen başlangıç noktası, uzunluk ve yöne göre geminin tahta sınırları içinde kalıp kalmadığını kontrol eder.
     */
    private boolean isValidPlacement(int row, int col, int length, boolean horizontal) {
        if (row < 0 || col < 0 || row >= SIZE || col >= SIZE) {
            return false; // Başlangıç noktası geçersiz
        }
        if (horizontal) {
            return col + length <= SIZE; // Yatayda sığıyor mu?
        } else {
            return row + length <= SIZE; // Dikeyde sığıyor mu?
        }
    }

    /**
    * Belirtilen konuma geminin yerleştirilip yerleştirilemeyeceğini kontrol eder.
    * (Başka gemiyle veya komşu hücrelerle çakışma var mı?)
    */
   private boolean canPlaceShipAt(int row, int col, int length, boolean horizontal) {
       for (int i = 0; i < length; i++) {
           int currentRow = horizontal ? row : row + i;
           int currentCol = horizontal ? col + i : col;

           // Yerleştirilecek hücrenin ve çevresindeki 8 komşusunun EMPTY olup olmadığını kontrol et
           for (int rOffset = -1; rOffset <= 1; rOffset++) {
               for (int cOffset = -1; cOffset <= 1; cOffset++) {
                   int checkRow = currentRow + rOffset;
                   int checkCol = currentCol + cOffset;

                   if (isValidCoordinate(checkRow, checkCol)) {
                       if (grid[checkRow][checkCol] != CellStatus.EMPTY) {
                           return false; // Başka bir gemi veya gemi komşuluğu var
                       }
                   }
               }
           }
       }
       return true;
   }



    /**
     * Belirtilen koordinata saldırı yapar.
     * Hücrenin durumunu günceller ve sonucu döndürür.
     *
     * @param row Satır indeksi (0-9).
     * @param col Sütun indeksi (0-9).
     * @return Saldırı sonucu (HIT, MISS veya zaten vurulmuşsa mevcut durum). Geçersiz koordinat için EMPTY döner.
     */
    public CellStatus attack(int row, int col) {
        if (!isValidCoordinate(row, col)) {
            return CellStatus.EMPTY; // Veya exception fırlat
        }

        CellStatus currentStatus = grid[row][col];

        if (currentStatus == CellStatus.SHIP) {
            grid[row][col] = CellStatus.HIT;
            // TODO: Gemi batıp batmadığını kontrol et ve gerekirse SUNK yap.
            // Bu, hangi hücrenin hangi gemiye ait olduğunu bilmeyi gerektirir.
            // Şimdilik sadece HIT döndürüyoruz.
            return CellStatus.HIT;
        } else if (currentStatus == CellStatus.EMPTY) {
            grid[row][col] = CellStatus.MISS;
            return CellStatus.MISS;
        } else {
            // Zaten vurulmuş bir hücre (HIT, MISS veya SUNK)
            return currentStatus; // Tekrar saldırmak bir şeyi değiştirmez, mevcut durumu döndür
        }
    }

    /**
     * Tahtadaki tüm gemilerin batıp batmadığını kontrol eder.
     * (Vurulmamış SHIP hücresi kalmış mı?)
     * @return Tüm gemiler battıysa true, en az bir gemi parçası kaldıysa false.
     */
    public boolean allShipsSunk() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == CellStatus.SHIP) {
                    return false; // Hala vurulmamış gemi parçası var
                }
            }
        }
        return true; // Vurulmamış gemi parçası kalmadı
    }

    /**
     * Belirtilen hücrenin durumunu doğrudan ayarlar.
     * Sunucudan gelen bilgileri (örn. rakibin atışı) tahtaya işlemek için kullanılabilir.
     * @param row Satır indeksi.
     * @param col Sütun indeksi.
     * @param status Ayarlanacak yeni durum.
     */
    public void updateCellStatus(int row, int col, CellStatus status) {
        if (isValidCoordinate(row, col)) {
            grid[row][col] = status;
        }
    }

    /**
     * Koordinatların tahta sınırları içinde olup olmadığını kontrol eder.
     * @param row Satır indeksi.
     * @param col Sütun indeksi.
     * @return Koordinatlar geçerliyse true, değilse false.
     */
    private boolean isValidCoordinate(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    /**
     * Tahtanın geçerli durumunu konsola yazdırmak için yardımcı metod (debug amaçlı).
     */
    public void printBoard() {
        System.out.print("  ");
        for (int i = 0; i < SIZE; i++) System.out.print(i + " ");
        System.out.println();
        for (int i = 0; i < SIZE; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < SIZE; j++) {
                char c = switch (grid[i][j]) {
                    case EMPTY -> '.';
                    case SHIP -> 'S';
                    case HIT -> 'X';
                    case MISS -> 'O';
                    case SUNK -> '#'; // Batmış gemi için
                };
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }
    public void resetBoard() {
        for (int i = 0; i < SIZE; i++) {
            Arrays.fill(grid[i], CellStatus.EMPTY); // Tüm hücreleri başlangıçta EMPTY yap
        }
    }


}