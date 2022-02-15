package blankishproject;

import blankishproject.simplification.SimplificationData;
import blankishproject.simplification.Simplification;
import blankishproject.ui.DrawPanel;
import blankishproject.ui.ProgressDialog;
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
import nl.tue.geometrycore.io.raster.RasterWriter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Data {

    public Polygon original;

    public Polygon staircase;
    public int stairSteps = 20;
    public double staircaseSlope = 45;
    public double staircaseSlopeVariation = 0;
    public double staircaseStepSizeVariation = 100;
    public OrientationSet staircaseOrientations = new OrientationSet();
    public boolean drawStaircase = true;

    // Schematization
    public PolyLine schematization;
    public int[] clockwiseClassifications;
    public int[] counterClockwiseClassifications;
    public int[] significance;
    public int currentIndex;

    public OrientationSet orientations = new OrientationSet();

    // Simplification
    public SimplificationData simplificationData = new SimplificationData(new Polygon(), orientations);

    // settings
    public SizeMode sizemode = SizeMode.VIEW;
    public double strokewidth = 3;
    public BaseGeometry selected = null;


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
    public boolean drawScreenshotAlignment = false;

    // keep these last
    public DrawPanel draw;
    public SidePanel side;
    public ProgressDialog dialog;

    public Data() {
        this.draw = new DrawPanel(this);
        this.side = new SidePanel(this);

        staircaseOrientations.addOrientationDegrees(0);
        staircaseOrientations.addOrientationDegrees(90);

        orientations.addOrientationDegrees(0);
        orientations.addOrientationDegrees(45);
        orientations.addOrientationDegrees(90);
        orientations.addOrientationDegrees(135);
    }

    public void select(Vector loc, double distance) {
        var polygon = simplificationData.polygon;
        if (polygon.distanceTo(loc) < distance) {
            int closest = -1;
            var closestDistance = Double.MAX_VALUE;
            for (int i = 0; i < polygon.edgeCount(); i++) {
                var edge = polygon.edge(i);
                if (edge.distanceTo(loc) < closestDistance) {
                    closestDistance = edge.distanceTo(loc);
                    closest = i;
                }
            }

            if (closest >= 0){
                simplificationData.selectedEdge = closest;
                System.out.println("Selected edge " + closest);
            }
        } else {
            simplificationData.selectedEdge = -1;
        }

        repaint();
    }

    public void pasteIPE() {
        try (IPEReader read = IPEReader.clipboardReader()) {
            List<ReadItem> items = read.read();
            for (ReadItem i : items) {
                if (i.toGeometry() instanceof Polygon) {
                    original = (Polygon) i.toGeometry();

                    initState();

                    draw.zoomToFit();
                    break;
                }
            }

            repaint();
        } catch (IOException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void pasteOriginal() {
        try (IPEReader read = IPEReader.clipboardReader()) {
            List<ReadItem> items = read.read();
            for (ReadItem i : items) {
                if (i.toGeometry() instanceof Polygon) {
                    original = (Polygon) i.toGeometry();
                    draw.zoomToFit();
                    break;
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

    int imageCount = 0;
    public void exportPNG() {
        copyMode = true;
        File file = new File("./img_out/img_" + imageCount + ".png");
        try (RasterWriter write = RasterWriter.imageWriter(draw.getWorldview(), 2000, 2000, file)) {
            write.initialize();
            draw.render(write);
        } catch (IOException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
        imageCount++;
        System.out.println("Print image " + imageCount);
        copyMode = false;
    }

    public void resetGeometry() {
        //geometries.clear();
        original = new Polygon();
        schematization = new PolyLine();
        simplificationData.init(new Polygon(), null);

        Schematization.resetDebug(this);

        Simplification.reset();
        Simplification.resetDebug(this);

        repaint();
    }

    public void runSimplificationAlgorithm() {
        Simplification.run(this);
        repaint();
    }

    public void runXCyclesSimplificationAlgorithm(int cycles) {
        if (simplificationData.runThreaded) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    dialog.setMaxProgress(cycles);
                    dialog.show();
                    Simplification.run(Data.this, cycles, dialog);
                    return null;
                }

                @Override
                protected void done() {
                    dialog.stop();
                    repaint();
                }



            }.execute();
        } else {
            Simplification.run(Data.this, cycles, dialog);
            repaint();
        }
    }

    public void runUntilKLeftSimplificationAlgorithm(int K) {
        if (simplificationData.runThreaded) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    dialog.setMaxProgress(simplificationData.polygon.vertexCount() - K);
                    dialog.show();
                    Simplification.runUntilLeft(Data.this, K, dialog);
                    return null;
                }

                @Override
                protected void done() {
                    dialog.stop();
                    repaint();
                }
            }.execute();
        } else {
            Simplification.runUntilLeft(Data.this, K, dialog);
            repaint();
        }

    }

    public void finishSimplificationAlgorithm() {
        if (simplificationData.runThreaded) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    dialog.setMaxProgress(simplificationData.polygon.vertexCount());
                    dialog.show();
                    Simplification.finish(Data.this, dialog);
                    return null;
                }

                @Override
                protected void done() {
                    dialog.stop();
                    repaint();
                }
            }.execute();
        } else {
            Simplification.finish(Data.this, dialog);
            repaint();
        }

    }

    public void setAsInputSimplificationAlgorithm() {
        original = Util.finishPolyLine(schematization);
        initState();
    }

    public void setStaircaseAsInputSimplificationAlgorithm() {
        original = staircase;
        drawStaircase = false;
        initState();
    }

    public void initState() {
        runChecks();

        if (simplificationData.runThreaded) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    dialog.setProgress(0);
                    dialog.show();
                    Simplification.initState(simplificationData, original, dialog);
                    Schematization.init(Data.this);
                    return null;
                }

                @Override
                protected void done() {
                    dialog.stop();
                    repaint();
                }
            }.execute();
        } else {
            Simplification.initState(simplificationData, original, dialog);
            Schematization.init(Data.this);
            repaint();
        }
    }

    public void recalculateState() {
        runChecks();
        if (simplificationData.runThreaded) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    dialog.setProgress(0);
                    dialog.show();
                    Simplification.initState(simplificationData, simplificationData.polygon, dialog);
                    return null;
                }

                @Override
                protected void done() {
                    dialog.stop();
                    repaint();
                }
            }.execute();
        } else {
            Simplification.initState(simplificationData, simplificationData.polygon, dialog);
            repaint();
        }

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

    public void generateStaircase() {
        DataGeneration.generate(this);
        drawStaircase = true;
        repaint();
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
