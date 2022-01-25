package blankishproject.ui;

import blankishproject.Data;
import blankishproject.simplification.Simplification;
import blankishproject.simplification.deciders.IDecider;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

import static blankishproject.ui.SidePanel.subTitleFont;
import static blankishproject.ui.SidePanel.titleFont;

public class SimplificationTab {

    private final Data data;
    private final SideTab tab;

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

    private int x = 50;
    private int k = 100;

    public SimplificationTab(Data data, SidePanel panel) {
        this.data = data;
        this.tab = panel.addTab("Simplification");


        panel.addGeometryOptionsSection(tab);
        tab.addSpace(5);
        panel.addOrientationsSection(tab, data.orientations, "0, 45, 90, 135");
        tab.addSpace(5);
        addGeometryInformationSection(tab);
        tab.addSpace(5);
        addSimplificationRunningSection(tab);
        tab.addSpace(5);
        addSimplificationOptionsSection(tab);
        tab.addSpace(5);
        addSimplificationInformationSection(tab);
        tab.addSpace(5);
        addSimplificationDebugDrawOptionsSection(tab);
    }

    private void addGeometryInformationSection(SideTab tab) {
        tab.addLabel("Geometry Information").setFont(titleFont);
        tab.addSeparator(0);

        vertexCountLabel = new ChangeLabel("Vertices", 0);
        tab.addComponent(vertexCountLabel);
        edgeCountLabel = new ChangeLabel("Edges", 0);
        tab.addComponent(edgeCountLabel);
        areaLabel = new ChangeLabel("Area", 0);
        tab.addComponent(areaLabel);
    }

    private void addSimplificationRunningSection(SideTab tab) {
        tab.addLabel("Algorithm Running").setFont(titleFont);
        tab.addSeparator(0);

        tab.addButton("Run timed cycle [r]", (e -> data.runSimplificationAlgorithm()));
        tab.addButton("Finish completely [f]", (e -> data.finishSimplificationAlgorithm()));

        tab.addSpace(3);

        var xButton = tab.addButton("Run " + x + " cycles", (e -> data.runXCyclesSimplificationAlgorithm(x)));
        tab.addIntegerSlider(x, 1, 1000, (changeEvent, integer) -> {
            x = integer;
            xButton.setText("Run " + x + " cycles");
        });

        tab.addSpace(3);

        var kButton = tab.addButton("Run until " + k + " left", (e -> data.runUntilKLeftSimplificationAlgorithm(k)));
        tab.addIntegerSlider(k, 1, 1000, (changeEvent, integer) -> {
            k = integer;
            kButton.setText("Run until " + k + " left");
        });

        tab.addButton("Reset State", (e -> {
            data.recalculateState();
            data.draw.repaint();
            this.repaint();
        }));

        tab.addCheckbox("Run Threaded", data.simplificationData.runThreaded, (e, b) -> {
            data.simplificationData.runThreaded = b;
            data.draw.repaint();
        });
    }

    private void addSimplificationOptionsSection(SideTab tab) {
        tab.addLabel("Algorithm Options").setFont(titleFont);
        tab.addSeparator(0);

        var keySet = IDecider.deciders.keySet().stream().sorted().toArray(String[]::new);
        tab.addComboBox(keySet, data.simplificationData.deciderType, (e, v) -> data.simplificationData.deciderType = v);

        tab.addSpace(3);

        var sData = data.simplificationData;

        tab.addCheckbox("Calculate Start Rotations", sData.calculateStartRotationMoves, (e, b) -> {
            sData.calculateStartRotationMoves = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Calculate End Rotations", sData.calculateEndRotationMoves, (e, b) -> {
            sData.calculateEndRotationMoves = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Calculate Middle Rotations", sData.calculateMiddleRotationMoves, (e, b) -> {
            sData.calculateMiddleRotationMoves = b;
            data.draw.repaint();
        });
        var label = tab.addLabel("Minimal staircase size: " + data.simplificationData.minStaircaseSize );
        tab.addIntegerSlider(data.simplificationData.minStaircaseSize, 1, 50, (changeEvent, integer) -> {
            data.simplificationData.minStaircaseSize = integer;
            data.simplificationData.recalculateStaircases();
            label.setText("Minimal staircase size: " + data.simplificationData.minStaircaseSize );
            data.draw.repaint();
        });

    }


    private void addSimplificationInformationSection(SideTab tab) {
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

    private void addSimplificationDebugDrawOptionsSection(SideTab tab) {
        tab.addLabel("Debug Draw Options").setFont(titleFont);
        tab.addSeparator(0);

        var sData = data.simplificationData;

        tab.addCheckbox("Draw For All Edges", sData.drawForAll, (e, b) -> {
            sData.drawForAll = b;
            data.draw.repaint();
        });

        tab.addSpace(3);

        tab.addCheckbox("Draw Convexity Arcs", sData.drawConvexityArcs, (e, b) -> {
            sData.drawConvexityArcs = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Convexity Edges", sData.drawConvexityEdges, (e, b) -> {
            sData.drawConvexityEdges = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Positive Contractions", sData.drawPositiveContractions, (e, b) -> {
            sData.drawPositiveContractions = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Negative Contractions", sData.drawNegativeContractions, (e, b) -> {
            sData.drawNegativeContractions = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Positive Pairs", sData.drawPositivePairs, (e, b) -> {
            sData.drawPositivePairs = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Negative Pairs", sData.drawNegativePairs, (e, b) -> {
            sData.drawNegativePairs = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Blocking Points", sData.drawBlockingPoints, (e, b) -> {
            sData.drawBlockingPoints = b;
            data.draw.repaint();
        });

        tab.addSpace(3);

        tab.addCheckbox("Draw Start Rotations", sData.drawStartRotations, (e, b) -> {
            sData.drawStartRotations = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw End Rotations", sData.drawEndRotations, (e, b) -> {
            sData.drawEndRotations = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Middle Rotations", sData.drawMiddleRotations, (e, b) -> {
            sData.drawMiddleRotations = b;
            data.draw.repaint();
        });

        tab.addSpace(3);

        tab.addCheckbox("Draw Inner Difference", sData.drawInnerDifference, (e, b) -> {
            sData.drawInnerDifference = b;
            data.draw.repaint();
        });

        tab.addCheckbox("Draw Outer Difference", sData.drawOuterDifference, (e, b) -> {
            sData.drawOuterDifference = b;
            data.draw.repaint();
        });
    }

    public void repaint() {
        var polygon = data.simplificationData.polygon;

        vertexCountLabel.setNewValue(polygon != null ? polygon.vertexCount() : 0);
        edgeCountLabel.setNewValue(polygon != null ? polygon.edgeCount() : 0);
        areaLabel.setNewValue(polygon != null && polygon.vertexCount() > 2 ? polygon.areaUnsigned() : 0);

        repaintAlgorithmInformation();
    }

    public void repaintAlgorithmInformation() {
        totalRemovedVertexCountLabel.setNewValue(Simplification.totalVerticesRemoved);
        lastCycleRemovedVertexCountLabel.setNewValue(Simplification.lastCycleVerticesRemoved);

        totalEffectedAreaLabel.setNewValue(Simplification.totalAreaEffected);
        lastCycleEffectedAreaLabel.setNewValue(Simplification.lastCycleAreaEffected);

        totalMovesMadeLabel.setNewValue(Simplification.totalMovesMade);
        lastCycleMovesMadeLabel.setNewValue(Simplification.lastCycleMovesMade);

        totalTimeTakenLabel.setNewValue(Simplification.totalTimeTaken);
        lastCycleTimeTakenLabel.setNewValue(Simplification.lastCycleTimeTaken);
    }
}
