package shared;

/**
 * Oyun tahtasındaki bir hücrenin olası durumlarını temsil eder.
 */
public enum CellStatus {
    EMPTY,
    SHIP,
    HIT,
    MISS,
    SUNK
}