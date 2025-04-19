package client.ui;

import shared.Board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardPanel extends JPanel {
    private static final int CELL_SIZE = 30;
    private static final int GRID_SIZE = 10;

    private Board board;
    private boolean isPlayerBoard;
    private CellClickListener clickListener;

    public BoardPanel(Board board, boolean isPlayerBoard) {
        this.board = board;
        this.isPlayerBoard = isPlayerBoard;
        setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (clickListener != null && !isPlayerBoard) {
                    int x = e.getX() / CELL_SIZE;
                    int y = e.getY() / CELL_SIZE;

                    if (x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE) {
                        clickListener.onCellClick(x, y);
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw grid
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= GRID_SIZE; i++) {
            g2d.drawLine(0, i * CELL_SIZE, GRID_SIZE * CELL_SIZE, i * CELL_SIZE);
            g2d.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, GRID_SIZE * CELL_SIZE);
        }

        // Draw board state (ships, hits, misses)
        // This would depend on how your Board class is implemented
        // Example: draw ships on player's board, and hits/misses on both boards

        // Add code to visualize the board state
    }

    public void setCellClickListener(CellClickListener listener) {
        this.clickListener = listener;
    }

    public interface CellClickListener {
        void onCellClick(int x, int y);
    }
}