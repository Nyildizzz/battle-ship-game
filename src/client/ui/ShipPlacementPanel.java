package client.ui;

import client.GameClient;
import shared.Board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ShipPlacementPanel extends JPanel {
    private GameClient gameClient;
    private JComboBox<String> shipSelector;
    private JComboBox<String> orientationSelector;
    private JButton placeButton;
    private JLabel instructionLabel;
    private BoardPanel previewBoard;
    private JButton randomPlaceButton;
    private JButton resetButton;

    private final String[] SHIP_NAMES = {"Carrier (5)", "Battleship (4)", "Cruiser (3)", "Submarine (3)", "Destroyer (2)"};
    private final int[] SHIP_SIZES = {5, 4, 3, 3, 2};
    private List<Integer> placedShips = new ArrayList<>();

    public ShipPlacementPanel(GameClient gameClient) {
        this.gameClient = gameClient;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(250, 600));

        initComponents();
    }

    private void initComponents() {
        // Top control panel
        JPanel controlPanel = new JPanel(new GridLayout(6, 1, 5, 5));

        instructionLabel = new JLabel("Place your ships:", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 14));

        shipSelector = new JComboBox<>(SHIP_NAMES);
        orientationSelector = new JComboBox<>(new String[]{"Horizontal", "Vertical"});

        placeButton = new JButton("Place Selected Ship");
        placeButton.addActionListener(this::placeShip);

        randomPlaceButton = new JButton("Random Placement");
        randomPlaceButton.addActionListener(e -> randomPlacement());

        resetButton = new JButton("Reset Placement");
        resetButton.addActionListener(e -> resetPlacement());

        controlPanel.add(instructionLabel);
        controlPanel.add(shipSelector);
        controlPanel.add(orientationSelector);
        controlPanel.add(placeButton);
        controlPanel.add(randomPlaceButton);
        controlPanel.add(resetButton);

        // Preview board
        previewBoard = new BoardPanel(gameClient.getPlayerBoard(), true);
        previewBoard.setCellClickListener((x, y) -> {
            if (shipSelector.getSelectedIndex() >= 0) {
                previewShipPlacement(x, y);
            }
        });

        add(controlPanel, BorderLayout.NORTH);
        add(previewBoard, BorderLayout.CENTER);

        // Bottom confirmation
        JButton doneButton = new JButton("Done Placing Ships");
        doneButton.addActionListener(e -> finishPlacement());
        add(doneButton, BorderLayout.SOUTH);
    }

    private void placeShip(ActionEvent e) {
        int shipIndex = shipSelector.getSelectedIndex();
        if (shipIndex < 0 || placedShips.contains(shipIndex)) {
            return;
        }

        int shipSize = SHIP_SIZES[shipIndex];
        boolean horizontal = orientationSelector.getSelectedIndex() == 0;

        // Get coordinates from gameClient's selected cell or use default
        int[] selectedCell = gameClient.getSelectedCell();

        if (selectedCell != null &&
                gameClient.placeShip(selectedCell[0], selectedCell[1], shipSize, horizontal)) {
            placedShips.add(shipIndex);
            updateShipSelector();
            previewBoard.repaint();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Cannot place ship at selected position. Try another location.",
                    "Invalid Placement", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void previewShipPlacement(int x, int y) {
        gameClient.setSelectedCell(x, y);
        // In a real implementation, you might show a preview overlay
    }

    private void randomPlacement() {
        if (gameClient.randomShipPlacement()) {
            placedShips.clear();
            for (int i = 0; i < SHIP_NAMES.length; i++) {
                placedShips.add(i);
            }
            updateShipSelector();
            previewBoard.repaint();
        }
    }

    private void resetPlacement() {
        gameClient.resetBoard();
        placedShips.clear();
        updateShipSelector();
        previewBoard.repaint();
    }

    private void updateShipSelector() {
        if (placedShips.size() == SHIP_NAMES.length) {
            instructionLabel.setText("All ships placed!");
            shipSelector.setEnabled(false);
            orientationSelector.setEnabled(false);
            placeButton.setEnabled(false);
        } else {
            for (int i = 0; i < SHIP_NAMES.length; i++) {
                if (!placedShips.contains(i)) {
                    shipSelector.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void finishPlacement() {
        if (placedShips.size() == SHIP_NAMES.length) {
            gameClient.sendShipPlacement();
            setVisible(false);
        } else {
            JOptionPane.showMessageDialog(this,
                    "You must place all ships before starting the game.",
                    "Ships Required", JOptionPane.WARNING_MESSAGE);
        }
    }
}