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
package blankishproject;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import blankishproject.ui.DrawPanel;
import blankishproject.ui.SidePanel;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.io.ipe.IPEReader;
import nl.tue.geometrycore.io.ipe.IPEWriter;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Data {
    // geometries to draw
    public List<BaseGeometry> geometries = new ArrayList<>();
    
    // settings
    public SizeMode sizemode = SizeMode.VIEW;
    public double strokewidth = 3;
    public BaseGeometry selected = null;

    public String deciderType = "4. Minimal Complementary Pair";

    // Debug geometry
    public Map<Color, GeometryList<LineSegment>> debugLines = new HashMap<>();
    public Map<Color, GeometryList<LineSegment>> debugArrows = new HashMap<>();

    public boolean copyMode = false;

    // Debug Draw Options
    public boolean drawConvexityArcs = false;
    public boolean drawConvexityEdges = false;
    public boolean drawPositiveContractions = false;
    public boolean drawNegativeContractions = false;
    public boolean drawBlockingPoints = false;

    // keep these last
    public DrawPanel draw;
    public SidePanel side;

    public Data() {
        this.draw = new DrawPanel(this);
        this.side = new SidePanel(this);
    }

    public void select(Vector loc, double distance) {
        selected =null;
        for (BaseGeometry g : geometries) {
            if (g.distanceTo(loc) < distance) {
                selected = g;
            }
        }
        draw.repaint();
    }

    public void pasteIPE() {
        try (IPEReader read = IPEReader.clipboardReader()) {
            List<ReadItem> items = read.read();
            for (ReadItem i : items) {
                geometries.add(i.toGeometry());
            }
            draw.repaint();
            side.repaint();
        } catch (IOException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void copyIPE() {
        copyMode = true;
        try (IPEWriter write = IPEWriter.clipboardWriter()) {
            write.initialize();            
            draw.render(write);
            
        } catch (IOException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
        copyMode = false;
    }

    public void resetGeometry() {
        geometries.clear();
        Algorithm.reset();
        Algorithm.resetDebug(this);
        draw.repaint();
    }

    public void runAlgorithm() {
        Algorithm.run(this);
        draw.repaint();
        side.repaint();
    }

    public void finishAlgorithm() {
        Algorithm.finish(this);
        draw.repaint();
        side.repaint();
    }

    public void runTestCode() {
        TestCode.run(this);
        draw.repaint();
        side.repaint();
    }
}
