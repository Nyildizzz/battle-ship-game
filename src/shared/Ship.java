package shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Amiral Battı oyunundaki bir gemiyi temsil eder.
 * Uzunluk, pozisyon, yönelim ve isabet durumunu tutar.
 */
public class Ship {

    private final int size; // Geminin uzunluğu (kaç hücre kapladığı)
    private int startRow;          // Geminin başlangıç satırı (-1 ise yerleştirilmemiş)
    private int startCol;          // Geminin başlangıç sütunu (-1 ise yerleştirilmemiş)
    private boolean horizontal; // Geminin yönelimi (true: yatay, false: dikey)
    private int hitCount;     // Geminin kaç parçasının vurulduğu
    private ShipType shipType; // Geminin türü (örneğin: destroyer, battleship)
    private final List<CellCoordinate> occupiedCells; // Geminin kapladığı hücre koordinatları


    public Ship(int row, int col, int length, boolean isHorizontal, ShipType type) {
        if (length <= 0) {
            throw new IllegalArgumentException("Gemi uzunluğu pozitif olmalıdır.");
        }
        this.size = length;
        this.startRow = row;
        this.startCol = col;
        this.horizontal = isHorizontal;
        this.hitCount = 0;
        this.occupiedCells = new ArrayList<>();
        this.shipType = type;

    }

    // --- Getters ---

    public int getLength() {
        return size;
    }

    public int getRow() {
        return startRow;
    }

    public int getCol() {
        return startCol;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public ShipType getType() {
        return shipType;
    }
    public boolean isHitAt(int row, int col) {
        return occupiedCells.contains(new CellCoordinate(row, col));
    }

    /**
     * Geminin yerleştirilip yerleştirilmediğini kontrol eder.
     * @return Pozisyonu ayarlanmışsa true, değilse false.
     */
    public boolean isPlaced() {
        return startRow >= 0 && startCol >= 0;
    }

    /**
     * Geminin tamamen batıp batmadığını kontrol eder.
     * @return Vurulan parça sayısı geminin uzunluğuna eşit veya büyükse true, değilse false.
     */
    public boolean isSunk() {
        return hitCount >= size;
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
        this.startRow = row;
        this.startCol = col;
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
        occupiedCells.clear(); // Önceki hücreleri temizle
        for (int i = 0; i < size; i++) {
            int r = horizontal ? startRow : startRow + i;
            int c = horizontal ? startCol + i : startCol;
            occupiedCells.add(new CellCoordinate(r, c));
        }
    }

    /**
     * Belirtilen koordinattaki gemi hücresine isabet kaydeder.
     * @param row İsabet alan hücrenin satırı
     * @param col İsabet alan hücrenin sütunu
     * @return Eğer isabet kaydedildiyse true, değilse false (hücre gemiye ait değilse)
     */
    public boolean registerHit(int row, int col) {
        if (isOccupying(row, col)) {
            registerHit(); // Parametresiz metodu çağır
            return true;
        }
        return false;
    }

    /**
     * Belirtilen koordinatın gemi tarafından kaplanıp kaplanmadığını kontrol eder.
     * @param row Kontrol edilecek satır
     * @param col Kontrol edilecek sütun
     * @return Eğer koordinat gemi tarafından kaplanıyorsa true, değilse false
     */
    public boolean isOccupying(int row, int col) {
        for (CellCoordinate cell : occupiedCells) {
            if (cell.getRow() == row && cell.getCol() == col) {
                return true;
            }
        }
        return false;
    }

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