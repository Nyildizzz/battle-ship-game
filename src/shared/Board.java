package shared;

import java.util.Arrays;

/**
 * Amiral Battı oyun tahtasını temsil eder.
 * Hücre durumlarını ve gemi yerleştirme/saldırı mantığını yönetir.
 */
public class Board {
    private static final int SIZE = 10; // Tahta boyutu (sabit)
    private final CellStatus[][] grid; // Hücre durumlarını tutan 2D dizi
    private Ship [] ships;

    /**
     * Yeni bir Board nesnesi oluşturur ve tüm hücreleri EMPTY olarak başlatır.
     */
    public Board() {
        grid = new CellStatus[SIZE][SIZE];
        ships = new Ship[5];
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
    // Gemi yerleştirme sırasında ships dizisine kaydet
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
        // Gemiyi kaydet
        for (int i = 0; i < ships.length; i++) {
            if (ships[i] == null) {
                ships[i] = ship;
                break;
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


    public CellStatus attack(int row, int col) {
        if (!isValidCoordinate(row, col)) {
            return CellStatus.EMPTY; // Veya exception fırlat
        }

        CellStatus currentStatus = grid[row][col];
        System.out.println("Attack at (" + row + ", " + col + "): " + currentStatus);
        if (currentStatus == CellStatus.SHIP) {
            grid[row][col] = CellStatus.HIT;
            
            // Hangi gemiye isabet edildiğini bul ve güncelle
            updateShipHitStatus(row, col);
            
            return CellStatus.HIT;
        } else if (currentStatus == CellStatus.EMPTY) {
            grid[row][col] = CellStatus.MISS;
            return CellStatus.MISS;
        } else {
            // Zaten vurulmuş bir hücre (HIT, MISS veya SUNK)
            return currentStatus; // Tekrar saldırmak bir şeyi değiştirmez, mevcut durumu döndür
        }
    }
    
    private void updateShipHitStatus(int row, int col) {
        for (Ship ship : ships) {
            if (ship != null) {
                // isOccupying yerine getOccupiedCells kullanarak kontrolü yap
                boolean isOccupying = false;
                for (Ship.CellCoordinate cell : ship.getOccupiedCells()) {
                    if (cell.getRow() == row && cell.getCol() == col) {
                        isOccupying = true;
                        break;
                    }
                }
                
                if (isOccupying) {
                    // registerHit metodunu parametresiz çağır
                    ship.registerHit();
                    
                    // Eğer gemi battıysa, tüm hücreleri güncelle
                    if (ship.isSunk()) {
                        for (Ship.CellCoordinate cell : ship.getOccupiedCells()) {
                            grid[cell.getRow()][cell.getCol()] = CellStatus.SUNK;
                        }
                    }
                    break;
                }
            }
        }
    }


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


    public void updateCellStatus(int row, int col, CellStatus status) {
        if (isValidCoordinate(row, col)) {
            grid[row][col] = status;
        }
    }


    private boolean isValidCoordinate(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }


    public void resetBoard() {
        for (int i = 0; i < SIZE; i++) {
            Arrays.fill(grid[i], CellStatus.EMPTY); // Tüm hücreleri başlangıçta EMPTY yap
        }
    }
    public boolean isAlreadyShot(int row, int col) {
        return grid[row][col] == CellStatus.HIT || grid[row][col] == CellStatus.MISS;
    }
    public boolean processShot(int row, int col) {
        CellStatus result = attack(row, col);
        System.out.println("Shot result: " + result);
        if (result == CellStatus.HIT) {
            return true; // Vuruş başarılı
        } else if (result == CellStatus.MISS) {
            return false; // Iska
        }
        return false; // Geçersiz durum
    }
    public String getShipTypeAt(int row, int col) {
        for (Ship ship : ships) {
            if (ship.isHitAt(row, col)) {
                return ship.getType().name(); // Gemi tipini döndür
            }
        }
        return null;
    }

    public boolean isShipSunk(int row, int col) {
        for (Ship ship : ships) {
            if (ship.isHitAt(row, col)) {
                return ship.isSunk(); // Geminin batıp batmadığını kontrol et
            }
        }
        return false;
    }
    public boolean areAllShipsSunk() {
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false; // Hala batmamış gemi var
            }
        }
        return true; // Tüm gemiler batmış
    }

   public void markCellAsHit(int row, int col) {
       if (isValidCoordinate(row, col)) {
           // Eğer orijinal hücre durumu SHIP ise, gemi güncelleme işlemi yap
           if (grid[row][col] == CellStatus.SHIP) {
               updateShipHitStatus(row, col);
           }
           grid[row][col] = CellStatus.HIT;
       }
   }

    public void markCellAsMiss(int row, int col) {
        if (isValidCoordinate(row, col)) {
            grid[row][col] = CellStatus.MISS;
        }
    }





}