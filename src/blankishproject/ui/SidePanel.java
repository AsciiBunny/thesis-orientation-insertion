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

import blankishproject.Simplification;
import blankishproject.Data;
import blankishproject.deciders.IDecider;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;

import java.awt.*;

/**
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SidePanel extends TabbedSidePanel {

    static final Font titleFont = new Font(null, Font.BOLD, 16);
    static final Font subTitleFont = new Font(null, Font.BOLD, 14);

    private final Data data;
    private final SchematizationTab schematizationTab;
    private final SimplificationTab simplificationTab;


    public SidePanel(Data data) {
        super();
        this.data = data;

        schematizationTab = new SchematizationTab(data, this);
        simplificationTab = new SimplificationTab(data, this);
    }

    //region Geometry

    void addGeometryOptionsSection(SideTab tab) {
        tab.addLabel("Geometry Options").setFont(titleFont);
        tab.addSeparator(0);

        tab.addButton("Paste IPE [v]", (e) -> data.pasteIPE());
        tab.addButton("Copy IPE [c]", (e) -> data.copyIPE());
        tab.addButton("Reset Geometry [x]", (e) -> data.resetGeometry());

        tab.addComboBox(SizeMode.values(), data.sizemode, (e, v) -> {
            data.sizemode = v;
            data.draw.repaint();
        });
        tab.addDoubleSpinner(data.strokewidth, 0, Double.POSITIVE_INFINITY, 0.1, (e, v) -> {
            data.strokewidth = v;
            data.draw.repaint();
        });
    }



    //endregion Geometry

    @Override
    public void repaint() {
        super.repaint();
        if (this.data == null)
            return;


        schematizationTab.repaint();
        simplificationTab.repaint();
    }


}
