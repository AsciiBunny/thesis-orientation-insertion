package blankishproject.ui;

import blankishproject.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProgressDialog {

    private JDialog dialog;
    private JProgressBar progressBar;
    private JLabel label;

    private int maxProgress = 500;
    private int progress = 0;
    public boolean canceled;

    public ProgressDialog(JFrame frame) {
        setupJDialog(frame);
    }

    private void setupJDialog(JFrame frame) {
        dialog = new JDialog(frame, "Simplification Progress", true);

        progressBar = new JProgressBar(0, 500);
        progressBar.setStringPainted(true);

        label = new JLabel("Progress: ");

        dialog.add(BorderLayout.CENTER, progressBar);
        dialog.add(BorderLayout.NORTH, label);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 75);
        dialog.setLocationRelativeTo(frame);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                canceled = true;
            };
        });
    }

    public void show() {
        progressBar.setMaximum(maxProgress);
        progressBar.setValue(0);
        Thread dialogThread = new Thread(() -> {
            dialog.setVisible(true);
        });
        dialogThread.start();
    }

    public void stop() {
        dialog.setVisible(false);
    }

    public void update() {
        progressBar.setValue(progress);
        label.setText("Progress: " + progress + "/" + maxProgress);
        progressBar.repaint();
    }

    public void setProgress(int progress) {
        this.progress = progress;
        update();
    }

    public void increaseProgress(int increase) {
        setProgress(progress + increase);
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
        progressBar.setMaximum(maxProgress);
    }
}
