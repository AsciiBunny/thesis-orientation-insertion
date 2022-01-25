package blankishproject;

import blankishproject.ui.DrawPanel;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DataGeneration {

    private static LineSegment[] alignment = new LineSegment[]{
            new LineSegment(new Vector(0, 0), new Vector(0, 400)),
            new LineSegment(new Vector(0, 400), new Vector(400, 400)),
            new LineSegment(new Vector(400, 400), new Vector(400, 0)),
            new LineSegment(new Vector(400, 0), new Vector(0, 0)),
    };

    public static void generate(Data data) {
        var sideLength = 200.0;
        Vector corner = new Vector(100,100);
        Vector start = new Vector(corner.getX(),corner.getY() + sideLength);

        var bottomLength = sideLength / Math.tan(data.staircaseSlope / 360 * (Math.PI * 2));

        Vector end = new Vector(corner.getX() + bottomLength  ,100);

        Vector assigned = data.staircaseOrientations.get(0).getDirection();
        Vector associated = data.staircaseOrientations.get(1).getDirection();


        var points = new ArrayList<Vector>();

        var direction = Vector.subtract(end, start);

        var stepSize = direction.length() / (data.stairSteps * 2);
        var step = direction.clone();
        step.normalize();


        var ratios = Vector.solveVectorAddition(assigned, associated, step);
        var assignedStep = Vector.multiply(stepSize * ratios[0], assigned);
        var associatedStep = Vector.multiply(stepSize * ratios[1], associated);


        points.add(start.clone());
        var now = start.clone();
        for (int i = 0; i < data.stairSteps; i++) {
            buildStep(points, now, assignedStep, associatedStep);
            buildStep(points, now, associatedStep, assignedStep);
        }
        points.add(now);

        points.add(new Vector(now.getX(),corner.getY() - 10 * sideLength));
        points.add(new Vector(corner.getX() - 10 * sideLength,corner.getY() - 10 * sideLength));
        points.add(new Vector(corner.getX() - 10 * sideLength,corner.getY() + sideLength));

        data.staircase = new Polygon(points);
    }

    public static void buildStep(List<Vector> points, Vector now, Vector a, Vector b) {
        now.translate(a);
        points.add(now.clone());
        now.translate(b);
    }

    public static void drawDebug(Data data, DrawPanel panel) {
        if (data.drawScreenshotAlignment) {
            panel.setStroke(Color.black, 3, Dashing.SOLID);
            panel.draw(alignment);
        }

    }

}
