import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class LifeSim extends JFrame {

    private LifePanel lifePanel;
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JButton startStopButton;
    private JButton oneStepButton;
    private JButton clearFieldButton;
    private JSlider animationSpeedSlider;

    public LifeSim(String title) {
        super(title);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        lifePanel = new LifePanel();
        lifePanel.initialize(70, 70);
        add(lifePanel, BorderLayout.CENTER);

        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        add(toolBar, BorderLayout.SOUTH);

        Action openConfiguration = new AbstractAction("Open") {
            public void actionPerformed(ActionEvent event) {
                try (FileInputStream fos = new FileInputStream(getFileForOpen());
                     ObjectInputStream oos = new ObjectInputStream(fos)) {
                    lifePanel.setLife((LifeModel) oos.readObject());
                    lifePanel.repaint();
                    Insets insets = getInsets();
                    setSize(lifePanel.getPreferredSize().width + insets.left + insets.right,
                            lifePanel.getPreferredSize().height + insets.bottom + insets.top +
                                    menuBar.getHeight() + toolBar.getHeight());
                    repaint();
                } catch (IOException | ClassNotFoundException e) {
                    JOptionPane.showMessageDialog(LifeSim.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        fileMenu.add(openConfiguration);

        Action saveConfiguration = new AbstractAction("Save") {
            public void actionPerformed(ActionEvent event) {
                if (lifePanel.isSimulating()) {
                    JOptionPane.showMessageDialog(LifeSim.this, "Please, stop simulation!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try (FileOutputStream fos = new FileOutputStream(getFileForSave());
                     ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(lifePanel.getLifeModel());
                    oos.flush();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(LifeSim.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        fileMenu.add(saveConfiguration);

        startStopButton = new JButton("Start");
        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lifePanel.isSimulating()) {
                    lifePanel.stopSimulation();
                    oneStepButton.setEnabled(true);
                    startStopButton.setText("Start");
                } else {
                    lifePanel.startSimulation();
                    oneStepButton.setEnabled(false);
                    startStopButton.setText("Stop");
                }
            }
        });
        toolBar.add(startStopButton);

        oneStepButton = new JButton("One step");
        oneStepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lifePanel.getLifeModel().simulate();
                lifePanel.repaint();
            }
        });
        toolBar.add(oneStepButton);

        toolBar.addSeparator();

        clearFieldButton = new JButton("Clear field");
        clearFieldButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (lifePanel.getLifeModel()) {
                    lifePanel.getLifeModel().clear();
                    lifePanel.repaint();
                }
            }
        });
        toolBar.add(clearFieldButton);

        // бегунок, регулирующий скорость симул€ции (задержка в мс между шагами симул€ции)
        animationSpeedSlider = new JSlider(1, 1000);
        animationSpeedSlider.setValue(500);
        lifePanel.setUpdateDelay(animationSpeedSlider.getValue());
        animationSpeedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                lifePanel.setUpdateDelay(animationSpeedSlider.getValue());
            }
        });

        toolBar.addSeparator();
        toolBar.add(new JLabel("Fast"));
        toolBar.add(animationSpeedSlider);
        toolBar.add(new JLabel("Slow"));

        startStopButton.setMaximumSize(new Dimension(100, 50));
        oneStepButton.setMaximumSize(new Dimension(100, 50));
        clearFieldButton.setMaximumSize(new Dimension(100, 50));
        animationSpeedSlider.setMaximumSize(new Dimension(500, 50));
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LifeSim("LifeSim");
            }
        });
    }

    private String getFileForSave() throws FileNotFoundException {
        String result;
        JFileChooser fileSave = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Game of Life Config", "glconf");
        fileSave.setFileFilter(filter);
        if (fileSave.showSaveDialog(LifeSim.this) == JFileChooser.APPROVE_OPTION) {
            String fileName = fileSave.getSelectedFile().getName();
            result = fileSave.getSelectedFile().getAbsolutePath();
            if (!fileName.contains(".glconf"))
                result += ".glconf";
        } else {
            throw new FileNotFoundException("The file is not saved.");
        }
        return result;
    }

    private String getFileForOpen() throws FileNotFoundException {
        String result = null;
        JFileChooser fileSave = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Game of Life Config", "glconf");
        fileSave.setFileFilter(filter);
        if (fileSave.showOpenDialog(LifeSim.this) == JFileChooser.APPROVE_OPTION) {
            String fileName = fileSave.getSelectedFile().getName();
            result = fileSave.getSelectedFile().getAbsolutePath();
            if (!fileName.contains(".glconf"))
                throw new FileNotFoundException("The file you selected is not a configuration file of the game \"Life\".");
        }
        return result;
    }
}