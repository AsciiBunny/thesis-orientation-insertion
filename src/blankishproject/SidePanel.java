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
package blankishproject;

import blankishproject.deciders.IDecider;
import blankishproject.ui.ChangeLabel;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;
import nl.tue.geometrycore.geometry.linear.Polygon;

import javax.swing.*;
import java.awt.*;

/**
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SidePanel extends TabbedSidePanel {

    static final Font titleFont = new Font(null, Font.BOLD, 16);

    private final Data data;


    private ChangeLabel geometryCountLabel;
    private ChangeLabel vertexCountLabel;
    private ChangeLabel edgeCountLabel;
    private ChangeLabel areaLabel;

    public SidePanel(Data data) {
        this.data = data;
        addMyTab();
    }

    private void addMyTab() {
        SideTab tab = addTab("Tab");

        tab.addLabel("Algorithm Options").setFont(titleFont);
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

        tab.addSpace(5);

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

        tab.addButton("Debug draw", (e -> {
            data.draw.repaint();
        }));

        tab.addSpace(5);

        tab.addLabel("Algorithm Options").setFont(titleFont);
        tab.addSeparator(0);

        var keySet = IDecider.deciders.keySet().stream().sorted().toArray(String[]::new);
        tab.addComboBox(keySet, data.deciderType, (e, v) -> {
            data.deciderType = v;
        });

        tab.addSpace(5);

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

        tab.addSpace(5);

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

    @Override
    public void repaint() {
        super.repaint();
        if (this.data == null)
            return;
        geometryCountLabel.setNewValue(data.geometries.size());
        vertexCountLabel.setNewValue(data.geometries.size() > 0 ? ((Polygon) data.geometries.get(0)).vertexCount() : 0);
        edgeCountLabel.setNewValue(data.geometries.size() > 0 ? ((Polygon) data.geometries.get(0)).edgeCount() : 0);
        areaLabel.setNewValue(data.geometries.size() > 0 ? ((Polygon) data.geometries.get(0)).areaUnsigned() : 0);
    }
}
