package thesis.ui;

import thesis.Data;
import thesis.OrientationSet;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;

import java.awt.*;


public class SidePanel extends TabbedSidePanel {

    static final Font titleFont = new Font(null, Font.BOLD, 16);
    static final Font subTitleFont = new Font(null, Font.BOLD, 14);

    private final Data data;
    private final DataTab dataTab;
    private final SchematizationTab schematizationTab;
    private final SimplificationTab simplificationTab;


    public SidePanel(Data data) {
        super();
        this.data = data;

        Dimension D = new Dimension(300, 100);
        this.setMinimumSize(D);
        this.setPreferredSize(D);
        this.setSize(D);

        dataTab = new DataTab(data, this);
        schematizationTab = new SchematizationTab(data, this);
        simplificationTab = new SimplificationTab(data, this);
    }

    void addGeometryOptionsSection(SideTab tab) {
        tab.addLabel("Geometry Options").setFont(titleFont);
        tab.addSeparator(0);

        tab.addButton("Paste IPE [v]", (e) -> data.pasteIPE());
        tab.addButton("Paste IPE Original", (e) -> data.pasteOriginal());
        tab.addButton("Copy IPE [c]", (e) -> data.copyIPE());
        tab.addButton("Reset Geometry [x]", (e) -> data.resetGeometry());

        tab.addButton("Export PNG", (e -> {
            data.exportPNG();
        }));

        tab.addComboBox(SizeMode.values(), data.sizemode, (e, v) -> {
            data.sizemode = v;
            data.draw.repaint();
        });
        tab.addDoubleSpinner(data.strokewidth, 0, Double.POSITIVE_INFINITY, 0.1, (e, v) -> {
            data.strokewidth = v;
            data.draw.repaint();
        });
    }

    void addOrientationsSection(SideTab tab, OrientationSet orientations, String startText) {
        tab.addLabel("Geometry Options").setFont(titleFont);
        tab.addSeparator(0);

        var tf = tab.addTextField(startText);
        tab.addButton("Set Orientations", (e) -> {
            orientations.setFrom(tf.getText());
            data.repaint();
        });

    }

    @Override
    public void repaint() {
        super.repaint();
        if (this.data == null)
            return;


        dataTab.repaint();
        schematizationTab.repaint();
        simplificationTab.repaint();
    }


}
