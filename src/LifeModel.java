import java.io.Serializable;
import java.util.Arrays;

/**
 * ћодель данных "∆изни".
 * ѕоле завернуто в тороид, т.е. и левый и правый кра€, и верхний и нижний, €вл€ютс€ замкнутыми.
 * ѕри симул€ции используетс€ принцип двойной буферизации: данные берутс€ из главного массива mainField, после расчета
 * результат складываетс€ во вспомогательный массив backField. ѕо окончании расчета одного шага ссылки на эти массивы
 * мен€ютс€ местами.
 * ¬ массивах хран€тс€ значени€: 0, если клетка мертва, и 1, если жива.
 */
public class LifeModel implements Serializable {

    private static final long serialVersionUID = 3400265056061021533L;

    private byte[] mainField;
    private byte[] backField;

    private int width;
    private int height;
    private int[] neighborOffsets;
    private int[][] neighborXYOffsets;

    /**
     * »нициализаци€ модели.
     *
     * @param width  ширина пол€ данных
     * @param height высота пол€ данных
     */
    public LifeModel(int width, int height) {
        this.width = width;
        this.height = height;
        mainField = new byte[width * height];
        backField = new byte[width * height];
        neighborOffsets = new int[]{-width - 1, -width, -width + 1, -1, 1, width - 1, width, width + 1};
        neighborXYOffsets = new int[][]{{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void clear() {
        Arrays.fill(mainField, (byte) 0);
    }

    public void setCell(int x, int y, byte c) {
        mainField[y * width + x] = c;
    }

    public byte getCell(int x, int y) {
        return mainField[y * width + x];
    }

    /**
     * ќдин шаг симул€ции.
     */
    public void simulate() {
        // обрабатываем клетки, не касающиес€ краев пол€
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int j = y * width + x;
                byte n = countNeighbors(j);
                backField[j] = simulateCell(mainField[j], n);
            }
        }

        // обрабатываем граничные клетки
        // верхн€€ и нижн€€ строки
        for (int x = 0; x < width; x++) {
            int j = width * (height - 1);
            byte n = countBorderNeighbors(x, 0);
            backField[x] = simulateCell(mainField[x], n);
            n = countBorderNeighbors(x, height - 1);
            backField[x + j] = simulateCell(mainField[x + j], n);
        }
        // крайние левый и правый столбцы
        for (int y = 1; y < height - 1; y++) {
            int j = width * y;
            byte n = countBorderNeighbors(0, y);
            backField[j] = simulateCell(mainField[j], n);
            n = countBorderNeighbors(width - 1, y);
            backField[j + width - 1] = simulateCell(mainField[j + width - 1], n);
        }

        // обмениваем пол€ местами
        byte[] t = mainField;
        mainField = backField;
        backField = t;
    }

    /**
     * ѕодсчет соседей дл€ не касающихс€ краев клеток.
     *
     * @param j смещение клетки в массиве
     * @return кол-во соседей
     */
    private byte countNeighbors(int j) {
        byte n = 0;
        for (int i = 0; i < 8; i++) {
            n += mainField[j + neighborOffsets[i]];
        }
        return n;
    }

    /**
     * ѕодсчет соседей дл€ граничных клеток.
     *
     * @param x
     * @param y
     * @return кол-во соседей
     */
    private byte countBorderNeighbors(int x, int y) {
        byte n = 0;
        for (int i = 0; i < 8; i++) {
            int bx = (x + neighborXYOffsets[i][0] + width) % width;
            int by = (y + neighborXYOffsets[i][1] + height) % height;
            n += mainField[by * width + bx];
        }
        return n;
    }

    /**
     * —имул€ци€ дл€ одной клетки.
     *
     * @param self      собственное состо€ние клетки: 0/1
     * @param neighbors кол-во соседей
     * @return новое состо€ние клетки: 0/1
     */
    private byte simulateCell(byte self, byte neighbors) {
        return (byte) (self == 0 ? (neighbors == 3 ? 1 : 0) : neighbors == 2 || neighbors == 3 ? 1 : 0);
    }
}