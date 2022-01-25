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

import blankishproject.*;
import blankishproject.simplification.Simplification;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.GeometryPanel;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;

/**
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DrawPanel extends GeometryPanel {

    private final Data data;

    public DrawPanel(Data data) {
        this.data = data;
    }

    @Override
    protected void drawScene() {
        setSizeMode(data.sizemode);

        setStroke(Color.lightGray, data.strokewidth, Dashing.SOLID);
        if (data.original != null && data.original.vertexCount() > 0)
            draw(data.original);

        setStroke(Color.blue, data.strokewidth, Dashing.SOLID);
        if (data.staircase != null && data.staircase.vertexCount() > 0 && data.drawStaircase)
            draw(data.staircase);

        setStroke(Color.black, data.strokewidth, Dashing.SOLID);
        if (data.schematization != null && data.schematization.vertexCount() > 0)
            draw(data.schematization);

        if (data.simplificationData.polygon != null && data.original != null && (data.simplificationData.polygon.vertexCount() < data.original.vertexCount() || Simplification.totalMovesMade > 0))
            draw(data.simplificationData.polygon);

        if (data.copyMode)
            return;

        setStroke(Color.red, 1.5 * data.strokewidth, Dashing.SOLID);
        draw(data.selected);

        for (var color : data.debugLines.keySet()) {
            setStroke(color, data.strokewidth, Dashing.SOLID);
            draw(data.debugLines.get(color));
        }

        for (var color : data.debugArrows.keySet()) {
            setStroke(color, data.strokewidth, Dashing.dashed(data.strokewidth));
            //setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 100);
            draw(data.debugArrows.get(color));
        }

        if (data.simplificationData.drawInnerDifference && data.innerDifference != null) {
            setStroke(Color.pink, data.strokewidth, Dashing.SOLID);
            draw(data.innerDifference);
        }

        if (data.simplificationData.drawOuterDifference && data.outerDifference != null) {
            setStroke(Color.pink, data.strokewidth, Dashing.SOLID);
            draw(data.outerDifference);
        }


        Schematization.drawDebug(data, this);
        Simplification.drawDebug(data.simplificationData, this);
        DataGeneration.drawDebug(data, this);
        Compass.draw(data, this);
        TestCode.draw(data, this);
    }

    @Override
    public Rectangle getBoundingRectangle() {
        //return Rectangle.byBoundingBox(data.geometries);
        return Rectangle.byBoundingBox(Arrays.asList(data.original, data.simplificationData.polygon, data.schematization));
    }

    @Override
    protected void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        if (button == MouseEvent.BUTTON1) {
            data.select(loc, convertViewToWorld(5));
        }
    }

    @Override
    public void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {
        switch (keycode) {
            case KeyEvent.VK_V -> data.pasteIPE();
            case KeyEvent.VK_C -> data.copyIPE();
            case KeyEvent.VK_X -> data.resetGeometry();

            case KeyEvent.VK_R -> data.runSimplificationAlgorithm();
            case KeyEvent.VK_F -> data.finishSimplificationAlgorithm();
            case KeyEvent.VK_T -> data.runTestCode();
        }
    }
}
