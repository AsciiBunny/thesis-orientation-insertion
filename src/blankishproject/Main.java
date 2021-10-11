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

import blankishproject.ui.ProgressDialog;
import nl.tue.geometrycore.gui.GUIUtil;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Data data = new Data();
        var frame = GUIUtil.makeMainFrame("Polygon Simplification", data.draw, data.side);
        data.dialog = new ProgressDialog(frame, data);
    }

}
