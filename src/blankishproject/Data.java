package blankishproject;

import blankishproject.ui.DrawPanel;
import blankishproject.ui.SidePanel;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.io.ipe.IPEReader;
import nl.tue.geometrycore.io.ipe.IPEWriter;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Data {
    // geometries to draw
    //public List<BaseGeometry> geometries = new ArrayList<>();

    public Polygon original;
    public PolyLine schematization;
    public Polygon simplification;

    // settings
    public SizeMode sizemode = SizeMode.VIEW;
    public double strokewidth = 3;
    public BaseGeometry selected = null;

    public String deciderType = "4. Minimal Complementary Pair";

    public OrientationSet orientations = new OrientationSet();

    public int[] clockwiseClassifications;
    public int[] counterClockwiseClassifications;
    public int currentIndex;

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

        orientations.addOrientationDegrees(0);
//        orientations.addOrientationDegrees(0 + 45);
        orientations.addOrientationDegrees(90);
//        orientations.addOrientationDegrees(90 + 45);
    }

    public void select(Vector loc, double distance) {
//        selected = null;
//        for (BaseGeometry g : geometries) {
//            if (g.distanceTo(loc) < distance) {
//                selected = g;
//            }
//        }
//        repaint();
    }

    public void pasteIPE() {
        try (IPEReader read = IPEReader.clipboardReader()) {
            List<ReadItem> items = read.read();
            for (ReadItem i : items) {
                //geometries.add(i.toGeometry());
                if (i.toGeometry() instanceof Polygon) {
                    original = (Polygon) i.toGeometry();
                    simplification = original.clone();

                    Schematization.init(this);
                }
            }

            repaint();
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
        //geometries.clear();
        original = new Polygon();
        schematization = new PolyLine();
        simplification = new Polygon();

        Schematization.resetDebug(this);

        Simplification.reset();
        Simplification.resetDebug(this);

        repaint();
    }

    public void runSimplificationAlgorithm() {
        Simplification.run(this);
        repaint();
    }

    public void finishSimplificationAlgorithm() {
        Simplification.finish(this);
        repaint();
    }

    public void runSchematizationAlgorithm() {
        Schematization.run(this);
        repaint();
    }

    public void finishSchematizationAlgorithm() {
        Schematization.finish(this);
        repaint();
    }

    public void runTestCode() {
        repaint();
    }

    public void repaint() {
        TestCode.run(this);
        draw.repaint();
        side.repaint();
    }
}
