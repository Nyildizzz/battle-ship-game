package client.ui.game;

public class GameBoard {
    private static final int BOARD_SIZE = 10;
    private Cell[][] board;

    public enum Cell {
        EMPTY('~'),
        SHIP('S'),
        HIT('X'),
        MISS('O');

        private char symbol;

        Cell(char symbol) {
            this.symbol = symbol;
        }
    }

    public GameBoard() {
        board = new Cell[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = Cell.EMPTY;
            }
        }
    }

    public boolean placeShip(Ship ship, int row, int col, boolean isHorizontal) {
        if (!isValidPlacement(ship, row, col, isHorizontal)) {
            return false;
        }

        if (isHorizontal) {
            for (int i = 0; i < ship.getLength(); i++) {
                board[row][col + i] = Cell.SHIP;
            }
        } else {
            for (int i = 0; i < ship.getLength(); i++) {
                board[row + i][col] = Cell.SHIP;
            }
        }
        return true;
    }

    public boolean processShot(int row, int col) {
        if (board[row][col] == Cell.SHIP) {
            board[row][col] = Cell.HIT;
            return true;
        } else {
            board[row][col] = Cell.MISS;
            return false;
        }
    }
    public boolean isValidPlacement(Ship ship, int row, int col, boolean isHorizontal) {
        if (isHorizontal) {
            if (col + ship.getLength() > BOARD_SIZE) {
                return false;
            }
            for (int i = 0; i < ship.getLength(); i++) {
                if (board[row][col + i] != Cell.EMPTY) {
                    return false;
                }
            }
        } else {
            if (row + ship.getLength() > BOARD_SIZE) {
                return false;
            }
            for (int i = 0; i < ship.getLength(); i++) {
                if (board[row + i][col] != Cell.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
}