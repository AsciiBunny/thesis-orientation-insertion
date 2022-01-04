package blankishproject.ui;

import blankishproject.Data;
import blankishproject.DataGeneration;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

import javax.swing.*;

import static blankishproject.ui.SidePanel.titleFont;

public class DataTab {

    private final Data data;
    private final SideTab tab;
    private JCheckBox drawCheckbox;

    public DataTab(Data data, SidePanel panel) {
        this.data = data;
        this.tab = panel.addTab("Data");

        panel.addOrientationsSection(tab, data.staircaseOrientations, "0, 90");
        tab.addSpace(5);
        addGenerationConfigurationSection();
        tab.addSpace(5);
        addRunningSection();
        tab.addSpace(5);
        addDebugDrawOptionsSection();
    }

    private void addGenerationConfigurationSection() {
        tab.addLabel("Generation Options").setFont(titleFont);
        tab.addSeparator(0);

        var countLabel = tab.addLabel("Step Count: 10");
        tab.addIntegerSlider(data.stairSteps, 1,  1000, (changeEvent, integer) -> {
            data.stairSteps = integer;
            countLabel.setText("Step Count: " + integer);
            data.generateStaircase();
        });

        tab.addSpace(3);
        var slopeLabel = tab.addLabel("Staircase Slope: 45");
        tab.addIntegerSlider((int) data.staircaseSlope, 1,  89, (changeEvent, integer) -> {
            data.staircaseSlope = integer;
            slopeLabel.setText("Staircase Slope: " + integer);
            data.generateStaircase();
        });
    }


    private void addRunningSection() {
        tab.addLabel("Generate Staircase").setFont(titleFont);
        tab.addSeparator(0);

        tab.addButton("Generate", (e -> data.generateStaircase()));
        tab.addButton("Use As Input", (e -> data.setStaircaseAsInputSimplificationAlgorithm()));
    }

    private void addDebugDrawOptionsSection() {
        tab.addLabel("Debug Draw Options").setFont(titleFont);
        tab.addSeparator(0);
//
        drawCheckbox = tab.addCheckbox("Draw Staircase", data.drawOrientations, (e, b) -> {
            data.drawStaircase = b;
            data.draw.repaint();
        });
//
//        tab.addCheckbox("Draw Classifications", data.drawClassifications, (e, b) -> {
//            data.drawClassifications = b;
//            data.draw.repaint();
//        });
//
//        tab.addCheckbox("Draw Significance", data.drawSignificance, (e, b) -> {
//            data.drawSignificance = b;
//            data.draw.repaint();
//        });
    }

    public void repaint() {
        drawCheckbox.setSelected(data.drawStaircase);
    }
}
