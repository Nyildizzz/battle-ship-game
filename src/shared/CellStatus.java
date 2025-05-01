package shared;

/**
 * Oyun tahtasındaki bir hücrenin olası durumlarını temsil eder.
 */
public enum CellStatus {
    /**
     * Hücre boş ve henüz ateş edilmemiş.
     */
    EMPTY,

    /**
     * Hücrede bir gemi parçası var ve henüz ateş edilmemiş.
     * (Rakip tahtasında bu durum genellikle EMPTY gibi gösterilir).
     */
    SHIP,

    /**
     * Hücrede bir gemi parçası vardı ve vuruldu.
     */
    HIT,

    /**
     * Hücre boştu ve ateş edildi (ıska).
     */
    MISS,

    /**
     * Hücredeki gemi parçası vuruldu ve bu vuruşla gemi tamamen battı.
     * (Görsel olarak HIT'ten farklı gösterilebilir).
     */
    SUNK
}