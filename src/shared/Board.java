package shared;

public class Board {
    private static final int SIZE = 10;
    private Cell[][] grid;

    public enum Cell {
        EMPTY,
        SHIP,
        HIT,
        MISS
    }


    public boolean placeShip(int x, int y, int length, boolean horizontal) {
        if (horizontal) {
            if (x + length > SIZE) return false;
            for (int i = 0; i < length; i++) {
                if (grid[y][x + i] != Cell.EMPTY) return false;
            }
            for (int i = 0; i < length; i++) {
                grid[y][x + i] = Cell.SHIP;
            }
        } else {
            if (y + length > SIZE) return false;
            for (int i = 0; i < length; i++) {
                if (grid[y + i][x] != Cell.EMPTY) return false;
            }
            for (int i = 0; i < length; i++) {
                grid[y + i][x] = Cell.SHIP;
            }
        }
        return true;
    }

    public String attack(int x, int y) {
        if (grid[y][x] == Cell.SHIP) {
            grid[y][x] = Cell.HIT;
            return "HIT";
        } else if (grid[y][x] == Cell.EMPTY) {
            grid[y][x] = Cell.MISS;
            return "MISS";
        }
        return "ALREADY_ATTACKED";
    }

    public boolean allShipsSunk() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == Cell.SHIP) return false;
            }
        }
        return true;
    }

    public void markHit(int x, int y) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
            grid[y][x] = Cell.HIT;
        }
    }

    public void markMiss(int x, int y) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
            grid[y][x] = Cell.MISS;
        }
    }

    public Cell getCell(int x, int y) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
            return grid[y][x];
        }
        return Cell.EMPTY;
    }
}