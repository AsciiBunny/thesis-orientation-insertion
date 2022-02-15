package thesis;

import nl.tue.geometrycore.gui.GUIUtil;
import thesis.ui.ProgressDialog;


public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Data data = new Data();
        var frame = GUIUtil.makeMainFrame("Polygon Simplification", data.draw, data.side);
        data.dialog = new ProgressDialog(frame);
    }

}
