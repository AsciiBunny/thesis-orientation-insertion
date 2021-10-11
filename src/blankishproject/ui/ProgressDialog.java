package blankishproject.ui;

import blankishproject.Data;

import javax.swing.*;
import java.awt.*;

public class ProgressDialog {

    private final Data data;

    private JDialog dialog;
    private JProgressBar progressBar;
    private Thread dialogThread;

    public ProgressDialog(JFrame frame, Data data) {
        this.data = data;

        setupJDialog(frame);
    }

    private void setupJDialog(JFrame frame) {
        dialog = new JDialog(frame, "Simplification Progress", true);
        progressBar = new JProgressBar(0, 500);
        progressBar.setStringPainted(true);

        dialog.add(BorderLayout.CENTER, progressBar);
        dialog.add(BorderLayout.NORTH, new JLabel("Progress..."));
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 75);
        dialog.setLocationRelativeTo(frame);
    }

    public void show() {
        progressBar.setMaximum(data.maxProgress);
        progressBar.setValue(0);
        dialogThread = new Thread(() -> {
            dialog.setVisible(true);
        });
        dialogThread.start();
    }

    public void stop() {
        dialog.setVisible(false);
    }

    public void update() {
        progressBar.setValue(data.progress);
        progressBar.repaint();
    }
}
