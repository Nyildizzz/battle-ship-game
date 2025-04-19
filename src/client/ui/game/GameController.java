package client.ui.game;

public class GameController {
    private GameBoard playerBoard;
    private GameBoard opponentBoard;
    private Ship[] ships;

    public GameController() {
        playerBoard = new GameBoard();
        opponentBoard = new GameBoard();
        initializeShips();
    }

    private void initializeShips() {
        ships = new Ship[] {
                new Ship("Carrier", 5),
                new Ship("Battleship", 4),
                new Ship("Cruiser", 3),
                new Ship("Submarine", 3),
                new Ship("Destroyer", 2)
        };
    }

    public boolean placeShip(int shipIndex, int row, int col, boolean isHorizontal) {
        return playerBoard.placeShip(ships[shipIndex], row, col, isHorizontal);
    }

    public boolean processPlayerShot(int row, int col) {
        return opponentBoard.processShot(row, col);
    }
}