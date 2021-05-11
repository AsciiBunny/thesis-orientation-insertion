/**
 * This project is to serve as a simple base upon which to create GeometryCore 
 * projects. It creates a simple GUI which can receive copies from IPE and 
 * renders them black with a give stroke thickness. This can then be rendered 
 * back to IPE as well.
 * 
 * Note that this demo isn't meant to display coding best-practices.
 * 
 * Main.java: how to easily create a default GUI with a drawpanel and a sidepanel
 * Data.java: central handler of data/settings/etc
 * DrawPanel.java: the rendering canvas
 * SidePanel.java: the sidepanel
 */
package blankishproject.ui;

import blankishproject.Algorithm;
import blankishproject.Data;
import blankishproject.TestCode;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.GeometryPanel;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 *
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
        setStroke(Color.black, data.strokewidth, Dashing.SOLID);
        draw(data.geometries);

        if (data.copyMode)
            return;

        setStroke(Color.red, 1.5 * data.strokewidth, Dashing.SOLID);
        draw(data.selected);

        for(var color : data.debugLines.keySet()) {
            setStroke(color, data.strokewidth, Dashing.SOLID);
            draw(data.debugLines.get(color));
        }

        for(var color : data.debugArrows.keySet()) {
            setStroke(color, data.strokewidth, Dashing.dashed(data.strokewidth));
            //setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 100);
            draw(data.debugArrows.get(color));
        }

        Algorithm.drawDebug(data, this);
        TestCode.draw(data, this);
    }

    @Override
    public Rectangle getBoundingRectangle() {
        return Rectangle.byBoundingBox(data.geometries);
    }

    @Override
    protected void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {
        if (button == MouseEvent.BUTTON1) {
            data.select(loc, convertViewToWorld(5));
        }
    }

    @Override
    protected void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {
        switch (keycode) {
            case KeyEvent.VK_V -> data.pasteIPE();
            case KeyEvent.VK_C -> data.copyIPE();
            case KeyEvent.VK_R -> data.resetGeometry();
        }
    }

}
