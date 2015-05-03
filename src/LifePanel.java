import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Панель симулятора с редактором поля.
 * Левой кнопкой мыши можно ставить клетки, правой - стирать. Редактирование доступно в любое время, даже когда
 * симуляция запущена.
 * Процесс симуляции выполняется в отдельном потоке.
 */
public class LifePanel extends JPanel implements Runnable {
    /**
     * Цвет мертвой клетки.
     */
    private static final Color c0 = new Color(0x696969);
    /**
     * Цвет живой клетки.
     */
    private static final Color c1 = new Color(0x00FF2E);
    private Thread simThread;
    private LifeModel life;
    /**
     * Задержка в мс между шагами симуляции.
     */
    private int updateDelay = 100;
    /**
     * Размер клетки на экране.
     */
    private int cellSize = 8;
    /**
     * Промежуток между клетками.
     */
    private int cellGap = 1;

    public LifePanel() {
        setBackground(Color.BLACK);

        // редактор поля
        MouseAdapter ma = new MouseAdapter() {
            private boolean pressedLeft = false;    // нажата левая кнопка мыши
            private boolean pressedRight = false;    // нажата правая кнопка мыши

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

            /**
             * Устанавливает/стирает клетку.
             *
             * @param e
             */
            private void setCell(MouseEvent e) {
                if (life != null) {
                    synchronized (life) {
                        // рассчитываем координаты клетки, на которую указывает курсор мыши
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

    /**
     * Запуск симуляции.
     */
    public void startSimulation() {
        if (simThread == null) {
            simThread = new Thread(this);
            simThread.start();
        }
    }

    /**
     * Остановка симуляции.
     */
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
            // синхронизация используется для того, чтобы метод paintComponent не выводил на экран
            // содержимое поля, которое в данный момент меняется
            synchronized (life) {
                life.simulate();
            }
            repaint();
        }
        repaint();
    }

    /*
     * Возвращает размер панели с учетом размера поля и клеток.
     */
    @Override
    public Dimension getPreferredSize() {
        if (life != null) {
            Insets b = getInsets();
            return new Dimension((cellSize + cellGap) * life.getWidth() + cellGap + b.left + b.right,
                    (cellSize + cellGap) * life.getHeight() + cellGap + b.top + b.bottom);
        } else
            return new Dimension(100, 100);
    }

    /*
     * Прорисовка содержимого панели.
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (life != null) {
            synchronized (life) {
                super.paintComponent(g);
                Insets b = getInsets();
                for (int y = 0; y < life.getHeight(); y++) {
                    for (int x = 0; x < life.getWidth(); x++) {
                        byte c = life.getCell(x, y);
                        g.setColor(c == 1 ? c1 : c0);
                        g.fillRect(b.left + cellGap + x * (cellSize + cellGap), b.top + cellGap + y
                                * (cellSize + cellGap), cellSize, cellSize);
                    }
                }
            }
        }
    }

}