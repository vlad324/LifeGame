import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LifePanel extends JPanel implements Runnable {

    private static final Color COLOR_DEAD = new Color(0x696969);
    private static final Color COLOR_ALIVE = new Color(0x00FF2E);
    private Thread simThread;
    private LifeModel life;
    private int updateDelay;
    private int cellSize = 8;
    private int cellGap = 1;

    public LifePanel() {
        setBackground(Color.BLACK);

        MouseAdapter ma = new MouseAdapter() {
            private boolean pressedLeft = false;
            private boolean pressedRight = false;

            @Override
            public void mouseDragged(MouseEvent e) {
                setCell(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    pressedLeft = true;
                    pressedRight = false;
                    setCell(e);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    pressedLeft = false;
                    pressedRight = true;
                    setCell(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    pressedLeft = false;
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    pressedRight = false;
                }
            }

            private void setCell(MouseEvent e) {
                if (life != null) {
                    synchronized (life) {
                        int x = e.getX() / (cellSize + cellGap);
                        int y = e.getY() / (cellSize + cellGap);
                        if (x >= 0 && y >= 0 && x < life.getWidth() && y < life.getHeight()) {
                            if (pressedLeft) {
                                life.setCell(x, y, (byte) 1);
                                repaint();
                            }
                            if (pressedRight) {
                                life.setCell(x, y, (byte) 0);
                                repaint();
                            }
                        }
                    }
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public LifeModel getLifeModel() {
        return life;
    }

    public void setLife(LifeModel life) {
        this.life = life;
    }

    public void initialize(int width, int height) {
        life = new LifeModel(width, height);
    }

    public void setUpdateDelay(int updateDelay) {
        this.updateDelay = updateDelay;
    }

    public void startSimulation() {
        if (simThread == null) {
            simThread = new Thread(this);
            simThread.start();
        }
    }

    public void stopSimulation() {
        simThread = null;
    }

    public boolean isSimulating() {
        return simThread != null;
    }

    @Override
    public void run() {
        repaint();
        while (simThread != null) {
            try {
                Thread.sleep(updateDelay);
            } catch (InterruptedException ignored) {
            }
            synchronized (life) {
                life.simulate();
            }
            repaint();
        }
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (life != null) {
            Insets b = getInsets();
            return new Dimension((cellSize + cellGap) * life.getWidth() + cellGap + b.left + b.right,
                    (cellSize + cellGap) * life.getHeight() + cellGap + b.top + b.bottom);
        } else
            return new Dimension(100, 100);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (life != null) {
            synchronized (life) {
                super.paintComponent(g);
                Insets b = getInsets();
                for (int y = 0; y < life.getHeight(); y++) {
                    for (int x = 0; x < life.getWidth(); x++) {
                        byte c = life.getCell(x, y);
                        g.setColor(c == 1 ? COLOR_ALIVE : COLOR_DEAD);
                        g.fillRect(b.left + cellGap + x * (cellSize + cellGap), b.top + cellGap + y
                                * (cellSize + cellGap), cellSize, cellSize);
                    }
                }
            }
        }
    }

}