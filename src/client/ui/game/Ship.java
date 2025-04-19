package client.ui.game;

public class Ship {
    private int length;
    private String name;
    private boolean isHorizontal;
    private int row;
    private int col;
    private boolean[] hits;

    public Ship(String name, int length) {
        this.name = name;
        this.length = length;
        this.hits = new boolean[length];
    }

    public boolean isSunk() {
        for (boolean hit : hits) {
            if (!hit) return false;
        }
        return true;
    }

    public void hit(int index) {
        if (index >= 0 && index < length) {
            hits[index] = true;
        }
    }
    public int getLength() {
        return length;
    }
    public String getName() {
        return name;
    }
    public boolean isHorizontal() {
        return isHorizontal;
    }
    public void setHorizontal(boolean horizontal) {
        isHorizontal = horizontal;
    }
}