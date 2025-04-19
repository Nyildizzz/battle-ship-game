package shared;

public class Board {
    private static final int SIZE = 10;
    private char[][] grid; // 'S' for ship, 'H' for hit, 'M' for miss

    public Board() {
        grid = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = '-'; // Empty cell
            }
        }
    }

    public boolean placeShip(int x, int y, int length, boolean horizontal) {
        if (horizontal) {
            if (x + length > SIZE) return false;
            for (int i = 0; i < length; i++) {
                if (grid[y][x + i] != '-') return false;
            }
            for (int i = 0; i < length; i++) {
                grid[y][x + i] = 'S';
            }
        } else {
            if (y + length > SIZE) return false;
            for (int i = 0; i < length; i++) {
                if (grid[y + i][x] != '-') return false;
            }
            for (int i = 0; i < length; i++) {
                grid[y + i][x] = 'S';
            }
        }
        return true;
    }

    public String attack(int x, int y) {
        if (grid[y][x] == 'S') {
            grid[y][x] = 'H';
            return "HIT";
        } else if (grid[y][x] == '-') {
            grid[y][x] = 'M';
            return "MISS";
        }
        return "ALREADY_ATTACKED";
    }

    public boolean allShipsSunk() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == 'S') return false;
            }
        }
        return true;
    }
    // Add these methods to shared.Board class

    /**
     * Marks a cell as hit
     */
    public void markHit(int x, int y) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
            grid[y][x] = 'H';
        }
    }

    /**
     * Marks a cell as miss
     */
    public void markMiss(int x, int y) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
            grid[y][x] = 'M';
        }
    }
}