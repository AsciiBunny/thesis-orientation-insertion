/**
 * This project is to serve as a simple base upon which to create GeometryCore
 * projects. It creates a simple GUI which can receive copies from IPE and
 * renders them black with a give stroke thickness. This can then be rendered
 * back to IPE as well.
 * <p>
 * Note that this demo isn't meant to display coding best-practices.
 * <p>
 * Main.java: how to easily create a default GUI with a drawpanel and a sidepanel
 * Data.java: central handler of data/settings/etc
 * DrawPanel.java: the rendering canvas
 * SidePanel.java: the sidepanel
 */
package blankishproject.ui;

import blankishproject.Algorithm;
import blankishproject.Data;
import blankishproject.TestCode;
import blankishproject.deciders.IDecider;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;
import nl.tue.geometrycore.geometry.linear.Polygon;

import java.awt.*;

/**
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SidePanel extends TabbedSidePanel {

    static final Font titleFont = new Font(null, Font.BOLD, 16);
    static final Font subTitleFont = new Font(null, Font.BOLD, 14);

    private final Data data;


    private ChangeLabel geometryCountLabel;
    private ChangeLabel vertexCountLabel;
    private ChangeLabel edgeCountLabel;
    private ChangeLabel areaLabel;

    private ChangeLabel totalRemovedVertexCountLabel;
    private ChangeLabel lastCycleRemovedVertexCountLabel;

    private ChangeLabel totalEffectedAreaLabel;
    private ChangeLabel lastCycleEffectedAreaLabel;

    private ChangeLabel totalMovesMadeLabel;
    private ChangeLabel lastCycleMovesMadeLabel;

    private ChangeLabel totalTimeTakenLabel;
    private ChangeLabel lastCycleTimeTakenLabel;

    public SidePanel(Data data) {
        super();
        this.data = data;

        addSchematizationTab();
        addSimplificationTab();
    }

    private void addSchematizationTab() {
        SideTab tab = addTab("Schematization");

        addGeometryOptionsSection(tab);
        tab.addSpace(5);
        addGeometryInformationSection(tab);
    }

    private void addSimplificationTab() {
        SideTab tab = addTab("Simplification");

        addGeometryOptionsSection(tab);
        tab.addSpace(5);
        addGeometryInformationSection(tab);
        tab.addSpace(5);
        addAlgorithmRunningSection(tab);
        tab.addSpace(5);
        addAlgorithmOptionsSection(tab);
        tab.addSpace(5);
        addAlgorithmInformationSection(tab);
        tab.addSpace(5);
        addDebugDrawOptionsSection(tab);
    }

    private void addGeometryOptionsSection(SideTab tab) {
        tab.addLabel("Geometry Options").setFont(titleFont);
        tab.addSeparator(0);

        tab.addButton("Paste IPE [v]", (e) -> data.pasteIPE());
        tab.addButton("Copy IPE [c]", (e) -> data.copyIPE());
        tab.addButton("Reset Geometry [r]", (e) -> data.resetGeometry());

        tab.addComboBox(SizeMode.values(), data.sizemode, (e, v) -> {
            data.sizemode = v;
            data.draw.repaint();
        });
        tab.addDoubleSpinner(data.strokewidth, 0, Double.POSITIVE_INFINITY, 0.1, (e, v) -> {
            data.strokewidth = v;
            data.draw.repaint();
        });
    }

    private void addGeometryInformationSection(SideTab tab) {
        tab.addLabel("Geometry Information").setFont(titleFont);
        tab.addSeparator(0);


        geometryCountLabel = new ChangeLabel("Geometries", 0);
        tab.addComponent(geometryCountLabel);
        vertexCountLabel = new ChangeLabel("Vertices", 0);
        tab.addComponent(vertexCountLabel);
        edgeCountLabel = new ChangeLabel("Edges", 0);
        tab.addComponent(edgeCountLabel);
        areaLabel = new ChangeLabel("Area", 0);
        tab.addComponent(areaLabel);
    }

    private void addAlgorithmRunningSection(SideTab tab) {
        tab.addLabel("Algorithm Running").setFont(titleFont);
        tab.addSeparator(0);

        tab.addButton("Run Algorithm", (e -> {
            Algorithm.run(data);
            data.draw.repaint();
            this.repaint();
        }));

        tab.addButton("Finish Algorithm", (e -> {
            Algorithm.finish(data);
            data.draw.repaint();
            this.repaint();
        }));

        tab.addSpace(3);

        tab.addButton("Run Test Code", (e -> {
            TestCode.run(data);
            data.draw.repaint();
            this.repaint();
        }));

        tab.addButton("Reset State", (e -> {
            // TODO: this
            Algorithm.reset();
            data.draw.repaint();
            this.repaint();
        }));

        tab.addButton("Debug draw", (e -> {
            data.draw.repaint();
        }));
    }

    private void addAlgorithmOptionsSection(SideTab tab) {
        tab.addLabel("Algorithm Options").setFont(titleFont);
        tab.addSeparator(0);

        var keySet = IDecider.deciders.keySet().stream().sorted().toArray(String[]::new);
        tab.addComboBox(keySet, data.deciderType, (e, v) -> {
            data.deciderType = v;
        });
    }


    private void addAlgorithmInformationSection(SideTab tab) {
        tab.addLabel("Algorithm Information").setFont(titleFont);
        tab.addSeparator(0);

        tab.addLabel("Total").setFont(subTitleFont);

        totalRemovedVertexCountLabel = new ChangeLabel("Removed Vertices", 0);
        tab.addComponent(totalRemovedVertexCountLabel);
        totalEffectedAreaLabel = new ChangeLabel("Effected Area", 0);
        tab.addComponent(totalEffectedAreaLabel);
        totalMovesMadeLabel = new ChangeLabel("Moves Made", 0);
        tab.addComponent(totalMovesMadeLabel);
        totalTimeTakenLabel = new ChangeLabel("Time Taken", 0);
        tab.addComponent(totalTimeTakenLabel);

        tab.addSpace(3);

        tab.addLabel("Last Cycle").setFont(subTitleFont);
        lastCycleRemovedVertexCountLabel = new ChangeLabel("Removed Vertices", 0);
        tab.addComponent(lastCycleRemovedVertexCountLabel);
        lastCycleEffectedAreaLabel = new ChangeLabel("Effected Area", 0);
        tab.addComponent(lastCycleEffectedAreaLabel);
        lastCycleMovesMadeLabel = new ChangeLabel("Moves Made", 0);
        tab.addComponent(lastCycleMovesMadeLabel);
        lastCycleTimeTakenLabel = new ChangeLabel("Time Taken", 0);
        tab.addComponent(lastCycleTimeTakenLabel);
    }

    private void addDebugDrawOptionsSection(SideTab tab) {
        tab.addLabel("Debug Draw Options").setFont(titleFont);
        tab.addSeparator(0);

        tab.addCheckbox("Draw Convexity Arcs", data.drawConvexityArcs, (e, b) -> {
            data.drawConvexityArcs = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Convexity Edges", data.drawConvexityEdges, (e, b) -> {
            data.drawConvexityEdges = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Positive Contractions", data.drawPositiveContractions, (e, b) -> {
            data.drawPositiveContractions = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Negative Contractions", data.drawNegativeContractions, (e, b) -> {
            data.drawNegativeContractions = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Blocking Points", data.drawBlockingPoints, (e, b) -> {
            data.drawBlockingPoints = b;
            data.draw.repaint();
        });
    }

    @Override
    public void repaint() {
        super.repaint();
        if (this.data == null)
            return;
        repaintGeometryInformation();
        repaintAlgorithmInformation();
    }

    public void repaintGeometryInformation() {
        var polygon = data.geometries.size() > 0 ? ((Polygon) data.geometries.get(0)) : null;

        geometryCountLabel.setNewValue(data.geometries.size());
        vertexCountLabel.setNewValue(polygon != null ? polygon.vertexCount() : 0);
        edgeCountLabel.setNewValue(polygon != null ? polygon.edgeCount() : 0);
        areaLabel.setNewValue(polygon != null ? polygon.areaUnsigned() : 0);
    }

    public void repaintAlgorithmInformation() {
        totalRemovedVertexCountLabel.setNewValue(Algorithm.totalVerticesRemoved);
        lastCycleRemovedVertexCountLabel.setNewValue(Algorithm.lastCycleVerticesRemoved);

        totalEffectedAreaLabel.setNewValue(Algorithm.totalAreaEffected);
        lastCycleEffectedAreaLabel.setNewValue(Algorithm.lastCycleAreaEffected);

        totalMovesMadeLabel.setNewValue(Algorithm.totalMovesMade);
        lastCycleMovesMadeLabel.setNewValue(Algorithm.lastCycleMovesMade);

        totalTimeTakenLabel.setNewValue(Algorithm.totalTimeTaken);
        lastCycleTimeTakenLabel.setNewValue(Algorithm.lastCycleTimeTaken);
    }
}
