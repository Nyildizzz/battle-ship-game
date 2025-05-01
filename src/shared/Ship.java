package shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Amiral Battı oyunundaki bir gemiyi temsil eder.
 * Uzunluk, pozisyon, yönelim ve isabet durumunu tutar.
 */
public class Ship {

    private final int length; // Geminin uzunluğu (kaç hücre kapladığı)
    private int row;          // Geminin başlangıç satırı (-1 ise yerleştirilmemiş)
    private int col;          // Geminin başlangıç sütunu (-1 ise yerleştirilmemiş)
    private boolean horizontal; // Geminin yönelimi (true: yatay, false: dikey)
    private int hitCount;     // Geminin kaç parçasının vurulduğu
    private final List<CellCoordinate> occupiedCells; // Geminin kapladığı hücre koordinatları

    /**
     * Belirtilen uzunlukta yeni bir gemi oluşturur.
     * Başlangıçta yerleştirilmemiştir (pozisyon -1, -1).
     * @param length Geminin uzunluğu (pozitif bir tamsayı olmalı).
     */
    public Ship(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Gemi uzunluğu pozitif olmalıdır.");
        }
        this.length = length;
        this.row = -1; // Henüz yerleştirilmedi
        this.col = -1; // Henüz yerleştirilmedi
        this.horizontal = true; // Varsayılan yönelim yatay
        this.hitCount = 0;
        this.occupiedCells = new ArrayList<>(length);
    }

    // --- Getters ---

    public int getLength() {
        return length;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public int getHitCount() {
        return hitCount;
    }

    /**
     * Geminin yerleştirilip yerleştirilmediğini kontrol eder.
     * @return Pozisyonu ayarlanmışsa true, değilse false.
     */
    public boolean isPlaced() {
        return row != -1 && col != -1;
    }

    /**
     * Geminin tamamen batıp batmadığını kontrol eder.
     * @return Vurulan parça sayısı geminin uzunluğuna eşit veya büyükse true, değilse false.
     */
    public boolean isSunk() {
        return isPlaced() && hitCount >= length;
    }

    /**
     * Geminin kapladığı hücrelerin koordinat listesini döndürür.
     * @return Koordinat listesi (değiştirilemez bir kopya).
     */
    public List<CellCoordinate> getOccupiedCells() {
        // Dışarıdan listenin değiştirilmesini önlemek için kopya döndür
        return new ArrayList<>(occupiedCells);
    }


    // --- Setters ---

    /**
     * Geminin pozisyonunu (başlangıç hücresi) ayarlar.
     * @param row Başlangıç satırı.
     * @param col Başlangıç sütunu.
     */
    public void setPosition(int row, int col) {
        // TODO: Board.SIZE gibi bir sınıra göre geçerlilik kontrolü eklenebilir
        this.row = row;
        this.col = col;
        // Pozisyon değiştiğinde kaplanan hücreleri yeniden hesaplamak gerekebilir
        // veya sadece placeShip içinde addOccupiedCell kullanılabilir.
        recalculateOccupiedCells(); // Pozisyon değiştiğinde hücreleri güncelle
    }

    /**
     * Geminin yönelimini ayarlar.
     * @param horizontal True ise yatay, false ise dikey.
     */
    public void setOrientation(boolean horizontal) {
        this.horizontal = horizontal;
         recalculateOccupiedCells(); // Yönelim değiştiğinde hücreleri güncelle
    }

    // --- Actions ---

    /**
     * Gemiye bir isabet kaydedildiğini belirtir.
     * Vurulan parça sayısını artırır.
     */
    public void registerHit() {
        if (!isSunk()) { // Zaten batmışsa sayacı artırma
            this.hitCount++;
        }
    }

    /**
     * Geminin kapladığı hücre listesine bir koordinat ekler.
     * Bu genellikle Board.placeShip tarafından çağrılır.
     * @param r Eklenen hücrenin satırı.
     * @param c Eklenen hücrenin sütunu.
     */
    protected void addOccupiedCell(int r, int c) {
        this.occupiedCells.add(new CellCoordinate(r, c));
    }

     /**
      * Pozisyon veya yönelim değiştiğinde kaplanan hücre listesini temizler ve yeniden hesaplar.
      * Bu metod, setPosition veya setOrientation çağrıldığında otomatik olarak çağrılır.
      */
     private void recalculateOccupiedCells() {
         occupiedCells.clear();
         if (!isPlaced()) {
             return; // Yerleştirilmemişse hesaplanacak bir şey yok
         }

         for (int i = 0; i < length; i++) {
             if (horizontal) {
                 occupiedCells.add(new CellCoordinate(row, col + i));
             } else {
                 occupiedCells.add(new CellCoordinate(row + i, col));
             }
         }
     }


    @Override
    public String toString() {
        return "Ship{" +
               "length=" + length +
               ", row=" + row +
               ", col=" + col +
               ", horizontal=" + horizontal +
               ", hitCount=" + hitCount +
               ", sunk=" + isSunk() +
               '}';
    }

    // --- İç Sınıf: Hücre Koordinatı ---

    /**
     * Basit bir hücre koordinatını (satır, sütun) temsil eden iç sınıf.
     * Listelerde ve karşılaştırmalarda kullanılır.
     */
    public static class CellCoordinate {
        private final int row;
        private final int col;

        public CellCoordinate(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CellCoordinate that = (CellCoordinate) o;
            return row == that.row && col == that.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }

        @Override
        public String toString() {
            return "(" + row + "," + col + ")";
        }
    }
}