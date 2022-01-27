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
        addTestSection();
        tab.addSpace(5);
        addDebugDrawOptionsSection();
    }

    private void addGenerationConfigurationSection() {
        tab.addLabel("Generation Options").setFont(titleFont);
        tab.addSeparator(0);

        var countLabel = tab.addLabel("Step Count: 10");
        tab.addIntegerSlider(data.stairSteps, 1,  100, (changeEvent, integer) -> {
            data.stairSteps = integer;
            countLabel.setText("Step Count: " + integer);
            data.generateStaircase();
        });

        tab.addSpace(3);
        var slopeLabel = tab.addLabel("Staircase Slope: 45 degrees");
        tab.addIntegerSlider((int) data.staircaseSlope, 1,  89, (changeEvent, integer) -> {
            data.staircaseSlope = integer;
            slopeLabel.setText("Staircase Slope: " + integer);
            data.generateStaircase();
        });

        tab.addSpace(3);
        var slopeVariationLabel = tab.addLabel("Slope Variation: 0 degrees");
        tab.addIntegerSlider((int) data.staircaseSlopeVariation, 0,  89, (changeEvent, integer) -> {
            data.staircaseSlopeVariation = integer;
            slopeVariationLabel.setText("Slope Variation : " + integer + " degrees");
            data.generateStaircase();
        });


        tab.addSpace(3);
        var stepSizeVariationLabel = tab.addLabel("Step Size Variation: 0 times");
        tab.addIntegerSlider((int) data.staircaseStepSizeVariation, 100,  5000, (changeEvent, integer) -> {
            data.staircaseStepSizeVariation = integer;
            stepSizeVariationLabel.setText("Step Size Variation : " + data.staircaseStepSizeVariation / 100.0 + " times");
            data.generateStaircase();
        });

    }


    private void addRunningSection() {
        tab.addLabel("Generate Staircase").setFont(titleFont);
        tab.addSeparator(0);

        tab.addButton("Generate", (e -> data.generateStaircase()));
        tab.addButton("Use As Input", (e -> data.setStaircaseAsInputSimplificationAlgorithm()));
    }

    private void addTestSection() {
        tab.addButton("Run Test Code [t]", (e -> data.runTestCode()));
        tab.addButton("Debug draw", (e -> data.draw.repaint()));
    }

    private void addDebugDrawOptionsSection() {
        tab.addLabel("Debug Draw Options").setFont(titleFont);
        tab.addSeparator(0);

        drawCheckbox = tab.addCheckbox("Draw Staircase", data.drawOrientations, (e, b) -> {
            data.drawStaircase = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Alignment Rectangle", data.drawScreenshotAlignment, (e, b) -> {
            data.drawScreenshotAlignment = b;
            data.draw.repaint();
        });
    }

    public void repaint() {
        drawCheckbox.setSelected(data.drawStaircase);
    }
}
