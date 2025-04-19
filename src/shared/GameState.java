package shared;

import java.util.HashMap;
import java.util.Map;

public class GameState {
    private Map<Integer, Board> playerBoards;
    private int currentPlayer;
    private boolean gameOver;
    private int winner;

    public GameState() {
        playerBoards = new HashMap<>();
        currentPlayer = 1; // Player 1 starts
        gameOver = false;
        winner = -1;
    }

    public void addPlayer(int playerId, Board board) {
        playerBoards.put(playerId, board);
    }

    public Board getBoard(int playerId) {
        return playerBoards.get(playerId);
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void switchTurn() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setWinner(int playerId) {
        gameOver = true;
        winner = playerId;
    }

    public int getWinner() {
        return winner;
    }

    /**
     * Sets the game over status
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}