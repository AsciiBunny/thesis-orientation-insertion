package blankishproject.ui;

import blankishproject.Data;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

import static blankishproject.ui.SidePanel.titleFont;

public class SchematizationTab {

    private final Data data;
    private final SideTab tab;

    private ChangeLabel vertexCountLabel;
    private ChangeLabel edgeCountLabel;
    private ChangeLabel areaLabel;

    public SchematizationTab(Data data, SidePanel panel) {
        this.data = data;
        this.tab = panel.addTab("Schematization");

        panel.addGeometryOptionsSection(tab);
        tab.addSpace(5);
        addGeometryInformationSection();
        tab.addSpace(5);
        addRunningSection();
        tab.addSpace(5);
        addDebugDrawOptionsSection();
    }

    private void addGeometryInformationSection() {
        tab.addLabel("Geometry Information").setFont(titleFont);
        tab.addSeparator(0);

        vertexCountLabel = new ChangeLabel("Vertices", 0);
        tab.addComponent(vertexCountLabel);
        edgeCountLabel = new ChangeLabel("Edges", 0);
        tab.addComponent(edgeCountLabel);
        areaLabel = new ChangeLabel("Area", 0);
        tab.addComponent(areaLabel);
    }

    private void addRunningSection() {
        tab.addLabel("Algorithm Running").setFont(titleFont);
        tab.addSeparator(0);

        tab.addButton("Run Algorithm [r]", (e -> data.runSchematizationAlgorithm()));
        tab.addButton("Finish Algorithm [f]", (e -> data.finishSchematizationAlgorithm()));
        tab.addButton("Use As Input", (e -> data.setAsInputSimplificationAlgorithm()));
    }

    private void addDebugDrawOptionsSection() {
        tab.addLabel("Debug Draw Options").setFont(titleFont);
        tab.addSeparator(0);

        tab.addCheckbox("Draw Orientations", data.drawOrientations, (e, b) -> {
            data.drawOrientations = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Classifications", data.drawClassifications, (e, b) -> {
            data.drawClassifications = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Significance", data.drawSignificance, (e, b) -> {
            data.drawSignificance = b;
            data.draw.repaint();
        });
    }

    public void repaint() {
        if (data.schematization != null && data.original != null) {
            var original = data.original;
            var polyline = data.schematization;
            vertexCountLabel.setNewValue(original.vertexCount() + polyline.vertexCount() - data.currentIndex);
            edgeCountLabel.setNewValue(original.edgeCount() + polyline.edgeCount() - data.currentIndex);
            areaLabel.setNewValue(original.areaUnsigned());
        }
    }
}
