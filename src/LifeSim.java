import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
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
        JMenu settingsMenu = new JMenu("Settings");
        menuBar.add(fileMenu);
        menuBar.add(settingsMenu);

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        add(toolBar, BorderLayout.SOUTH);

        Action openConfiguration = new AbstractAction("Open") {
            public void actionPerformed(ActionEvent event) {
                if (lifePanel.isSimulating()) {
                    JOptionPane.showMessageDialog(LifeSim.this, "Please, stop simulation!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try (FileInputStream fos = new FileInputStream(getFileForOpen());
                     ObjectInputStream oos = new ObjectInputStream(fos)) {
                    lifePanel.setLife((LifeModel) oos.readObject());
                    pack();
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

        Action changeSizeOfArea = new AbstractAction("Change size") {
            public void actionPerformed(ActionEvent event) {
                if (lifePanel.isSimulating()) {
                    JOptionPane.showMessageDialog(LifeSim.this, "Please, stop simulation!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Box mainBox = Box.createVerticalBox();
                Box inputBox = Box.createHorizontalBox();
                JFrame changeSize = new JFrame("Change size");
                JLabel widthLabel = new JLabel("Width:");
                JTextField widthText = new JTextField(10);
                widthText.setMaximumSize(widthText.getPreferredSize());
                JLabel heightLabel = new JLabel("Height:");
                JTextField heightText = new JTextField(10);
                heightText.setMaximumSize(heightText.getPreferredSize());
                JButton okButton = new JButton("Ok!");
                okButton.addActionListener(e -> {
                    try {
                        int width = Integer.parseInt(widthText.getText());
                        int height = Integer.parseInt(heightText.getText());
                        lifePanel.initialize(width, height);
                        pack();
                        repaint();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(LifeSim.this, "Invalid arguments!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    changeSize.dispose();
                });
                inputBox.add(Box.createHorizontalStrut(10));
                inputBox.add(widthLabel);
                inputBox.add(Box.createHorizontalStrut(10));
                inputBox.add(widthText);
                inputBox.add(Box.createHorizontalStrut(10));
                inputBox.add(heightLabel);
                inputBox.add(Box.createHorizontalStrut(10));
                inputBox.add(heightText);
                inputBox.add(Box.createHorizontalStrut(10));
                mainBox.add(Box.createVerticalStrut(10));
                mainBox.add(inputBox);
                mainBox.add(Box.createVerticalStrut(10));
                mainBox.add(okButton);
                mainBox.add(Box.createVerticalStrut(10));
                changeSize.add(mainBox);
                changeSize.pack();
                changeSize.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                changeSize.setLocationRelativeTo(LifeSim.this);
                changeSize.setVisible(true);
            }
        };
        settingsMenu.add(changeSizeOfArea);

        startStopButton = new JButton("Start");
        startStopButton.addActionListener(e -> {
            if (lifePanel.isSimulating()) {
                lifePanel.stopSimulation();
                oneStepButton.setEnabled(true);
                startStopButton.setText("Start");
            } else {
                lifePanel.startSimulation();
                oneStepButton.setEnabled(false);
                startStopButton.setText("Stop");
            }
        });
        toolBar.add(startStopButton);

        oneStepButton = new JButton("One step");
        oneStepButton.addActionListener(e -> {
            lifePanel.getLifeModel().simulate();
            lifePanel.repaint();
        });
        toolBar.add(oneStepButton);

        toolBar.addSeparator();

        clearFieldButton = new JButton("Clear field");
        clearFieldButton.addActionListener(e -> {
            synchronized (lifePanel.getLifeModel()) {
                lifePanel.getLifeModel().clear();
                lifePanel.repaint();
            }
        });
        toolBar.add(clearFieldButton);

        animationSpeedSlider = new JSlider(1, 1000);
        animationSpeedSlider.setValue(500);
        lifePanel.setUpdateDelay(animationSpeedSlider.getValue());
        animationSpeedSlider.addChangeListener(e -> lifePanel.setUpdateDelay(animationSpeedSlider.getValue()));

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

        SwingUtilities.invokeLater(() -> new LifeSim("LifeSim"));
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