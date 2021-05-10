package blankishproject.ui;

import nl.tue.geometrycore.util.DoubleUtil;

import javax.swing.*;

public class ChangeLabel extends JLabel {

    private final String name;
    private double value;
    private double change = 0;

    public ChangeLabel(String name, double value) {
        this.name = name;
        this.value = value;
        updateText();
    }

    public void setNewValue(double newValue) {
        this.change = newValue - value;
        this.value = newValue;
        updateText();
    }

    public void updateText() {
        this.setText(name + ": " + String.format("%.2f", value) + " (" + (change > 0 ? "+" : "") + String.format("%.2f", change) + ")");
    }
}
