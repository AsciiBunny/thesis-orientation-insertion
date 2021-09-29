package blankishproject;

import blankishproject.edgelist.ConfigurationList;
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

    // Schematization
    public PolyLine schematization;
    public int[] clockwiseClassifications;
    public int[] counterClockwiseClassifications;
    public int[] significance;
    public int currentIndex;

    // Simplification
    public Polygon simplification;
    public ConfigurationList configurations;

    // settings
    public SizeMode sizemode = SizeMode.VIEW;
    public double strokewidth = 3;
    public BaseGeometry selected = null;

    public String deciderType = "4. Minimal Complementary Pair";

    public OrientationSet orientations = new OrientationSet();



    // Debug geometry
    public Map<Color, GeometryList<LineSegment>> debugLines = new HashMap<>();
    public Map<Color, GeometryList<LineSegment>> debugArrows = new HashMap<>();
    public List<Polygon> innerDifference;
    public List<Polygon> outerDifference;

    public boolean copyMode = false;

    // Debug Draw Options
    public boolean drawOrientations = false;
    public boolean drawClassifications = false;
    public boolean drawSignificance = false;

    public boolean drawConvexityArcs = false;
    public boolean drawConvexityEdges = false;
    public boolean drawPositiveContractions = false;
    public boolean drawNegativeContractions = false;
    public boolean drawBlockingPoints = false;

    public boolean drawInnerDifference = false;
    public boolean drawOuterDifference = false;

    // keep these last
    public DrawPanel draw;
    public SidePanel side;

    public Data() {
        this.draw = new DrawPanel(this);
        this.side = new SidePanel(this);

        orientations.addOrientationDegrees(0);
        orientations.addOrientationDegrees(0 + 45);
        orientations.addOrientationDegrees(90);
        orientations.addOrientationDegrees(90 + 45);
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

                    // TODO: Run checks
                    runChecks();

                    Schematization.init(this);
                    Simplification.init(this);
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

    public void setAsInputSimplificationAlgorithm() {
        original = Util.finishPolyLine(schematization);

        // TODO: Run checks
        runChecks();

        Simplification.init(this);
        Schematization.init(this);
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

    public void runChecks() {
        Checks.correctOrder(this);
        Checks.uniqueVertices(this);
        Checks.noUnnecessaryVertices(this);
        Checks.no360DegreeTurns(this);
    }

    public void runTestCode() {
        TestCode.run(this);
        repaint();
    }

    public void repaint() {
        draw.repaint();
        side.repaint();
    }
}
