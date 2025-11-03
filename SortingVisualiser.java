import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;

/**
 * SortingVisualiser.java
 * Single-file Swing sorting visualizer.
 *
 * Author: Ashwin Biju
 * GitHub: https://github.com/REGEN-HALOGEN
 *
 * Compile:
 * javac SortingVisualiser.java
 * Run:
 * java SortingVisualiser
 *
 * Uses: Java 8+
 */

public class SortingVisualiser {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VisualFrame().setVisible(true));
    }
}

/* ---------------------------- UI Frame ---------------------------- */
class VisualFrame extends JFrame {
    private final VisualPanel visualPanel = new VisualPanel();
    private final JComboBox<String> algoSelect = new JComboBox<>(
            // 🔹 UPDATED: Added Shell Sort and Radix Sort
            new String[]{"Bubble Sort", "Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort", "Heap Sort", "Shell Sort", "Radix Sort"});
    private final JSlider sizeSlider = new JSlider(10, 300, 80);
    private final JSlider speedSlider = new JSlider(10, 200, 80); // lower delay = faster
    private final JButton randomizeBtn = new JButton("Randomize");
    private final JButton loadCustomBtn = new JButton("Load Custom Array");
    private final JButton startBtn = new JButton("Start");
    private final JButton pauseBtn = new JButton("Pause");
    private final JButton resetBtn = new JButton("Reset");
    private final JLabel statusLabel = new JLabel("Status: Ready");

    // 🔹 NEW: Checkbox for number visibility
    private final JCheckBox numberToggle = new JCheckBox("Show Numbers", true);

    private OperationPlayer player;

    public VisualFrame() {
        super("Sorting Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 🔹 Invert slider direction so moving right = faster
        speedSlider.setInverted(true);

        add(visualPanel, BorderLayout.CENTER);
        add(buildControlPanel(), BorderLayout.SOUTH);

        // Initial array
        visualPanel.generateRandomArray(sizeSlider.getValue());

        // --- Event Listeners ---

        randomizeBtn.addActionListener(e -> {
            if (player != null && player.isPlaying()) return;
            visualPanel.generateRandomArray(sizeSlider.getValue());
            statusLabel.setText("Status: Randomized");
        });

        // Load Custom Array listener
        loadCustomBtn.addActionListener(e -> {
            if (player != null && player.isPlaying()) return;
            String input = JOptionPane.showInputDialog(this,
                    "Enter array elements separated by commas (e.g., 50,20,80,10):",
                    "Custom Array Input",
                    JOptionPane.PLAIN_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                loadCustomArray(input.trim());
            }
        });

        startBtn.addActionListener(e -> startSorting());
        pauseBtn.addActionListener(e -> {
            if (player != null) {
                if (player.isPaused()) {
                    player.resume();
                    statusLabel.setText("Status: Playing");
                } else {
                    player.pause();
                    statusLabel.setText("Status: Paused");
                }
            }
        });

        resetBtn.addActionListener(e -> {
            if (player != null) player.stop();
            visualPanel.resetHighlights();
            visualPanel.resetToOriginal(); // Use resetToOriginal to restore from aux
            statusLabel.setText("Status: Reset");
        });

        sizeSlider.addChangeListener(e -> {
            if (!sizeSlider.getValueIsAdjusting()) {
                if (player != null && player.isPlaying()) return;
                visualPanel.generateRandomArray(sizeSlider.getValue());
            }
        });

        speedSlider.addChangeListener(e -> {
            if (player != null) player.setDelay(speedToDelay(speedSlider.getValue()));
        });

        // 🔹 NEW: Toggle visibility in VisualPanel
        numberToggle.addActionListener(e -> {
            visualPanel.setShowNumbers(numberToggle.isSelected());
        });

        pack();
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Algorithm:"));
        top.add(algoSelect);
        top.add(new JLabel("Size:"));
        top.add(sizeSlider);
        top.add(new JLabel("Speed:"));
        top.add(speedSlider);
        top.add(randomizeBtn);
        top.add(loadCustomBtn);
        top.add(startBtn);
        top.add(pauseBtn);
        top.add(resetBtn);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(statusLabel);
        bottom.add(numberToggle); // 🔹 NEW: Add checkbox to controls

        panel.add(top, BorderLayout.NORTH);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // Method to parse and load the custom array
    private void loadCustomArray(String input) {
        try {
            String[] parts = input.split(",");
            int[] customArray = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                int value = Integer.parseInt(parts[i].trim());
                if (value <= 0) throw new NumberFormatException("Value must be positive.");
                customArray[i] = value;
            }
            if (customArray.length > 0) {
                visualPanel.setCustomArray(customArray);
                statusLabel.setText("Status: Custom array loaded (" + customArray.length + " elements)");
                sizeSlider.setValue(customArray.length); // Update slider to reflect size
            } else {
                statusLabel.setText("Status: Error - Empty array.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid input format. Please enter positive integers separated by commas.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Status: Error parsing array.");
        }
    }

    private int speedToDelay(int sliderValue) {
        // sliderValue: 1 (fast) -> 200 (slow)
        // convert to milliseconds delay
        int min = 2;
        int max = 200;
        return min + (int) ((max - min) * (sliderValue - 1) / 199.0);
    }

    private void startSorting() {
        if (player != null && player.isPlaying()) return;
        String algo = (String) algoSelect.getSelectedItem();
        int[] arr = visualPanel.getArrayCopy();
        statusLabel.setText("Status: Generating operations...");
        visualPanel.resetHighlights();

        // Generate operations in background thread
        new Thread(() -> {
            List<Operation> ops = new ArrayList<>();
            switch (algo) {
                case "Bubble Sort": SortingAlgorithms.bubbleSort(arr.clone(), ops); break;
                case "Selection Sort": SortingAlgorithms.selectionSort(arr.clone(), ops); break;
                case "Insertion Sort": SortingAlgorithms.insertionSort(arr.clone(), ops); break;
                case "Merge Sort": SortingAlgorithms.mergeSort(arr.clone(), ops); break;
                case "Quick Sort": SortingAlgorithms.quickSort(arr.clone(), ops); break;
                case "Heap Sort": SortingAlgorithms.heapSort(arr.clone(), ops); break;
                // 🔹 UPDATED: Added Shell Sort and Radix Sort
                case "Shell Sort": SortingAlgorithms.shellSort(arr.clone(), ops); break;
                case "Radix Sort": SortingAlgorithms.radixSort(arr.clone(), ops); break;
                default: SortingAlgorithms.bubbleSort(arr.clone(), ops); break;
            }

            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Status: Playing (" + algo + ")");
                player = new OperationPlayer(ops, visualPanel, speedToDelay(speedSlider.getValue()), statusLabel);
                player.start();
            });
        }).start();
    }
}

/* ---------------------------- Visual Panel ---------------------------- */
class VisualPanel extends JPanel {
    private int[] array;
    private int[] aux; // keep a copy for reset
    private int highlightA = -1, highlightB = -1; // indices being compared/swapped
    private int[] colorIndices = null; // optional per-index color (not used now)

    // 🔹 NEW: State variable to track number visibility
    private boolean showNumbers = true;

    public VisualPanel() {
        setPreferredSize(new Dimension(1000, 520));
        setBackground(Color.BLACK);
    }

    // 🔹 NEW: Setter method for number visibility
    public void setShowNumbers(boolean show) {
        this.showNumbers = show;
        repaint(); // Immediately redraw to reflect the change
    }

    // Method to set a user-defined array
    public void setCustomArray(int[] customArray) {
        this.array = customArray;
        this.aux = customArray.clone();
        resetHighlights();
        repaint();
    }

    public void generateRandomArray(int size) {
        Random rnd = new Random();
        array = new int[size];
        for (int i = 0; i < size; i++) array[i] = rnd.nextInt(400) + 5;
        aux = array.clone();
        resetHighlights();
        repaint();
    }

    public int[] getArrayCopy() {
        return array.clone();
    }

    public void resetHighlights() {
        highlightA = highlightB = -1;
    }

    // Apply operation (called by player on EDT)
    public void applyOperation(Operation op) {
        switch (op.type) {
            case COMPARE:
                highlightA = op.i;
                highlightB = op.j;
                break;
            case SWAP:
                int tmp = array[op.i];
                array[op.i] = array[op.j];
                array[op.j] = tmp;
                highlightA = op.i;
                highlightB = op.j;
                break;
            case OVERWRITE:
                array[op.i] = op.value;
                highlightA = op.i;
                highlightB = -1;
                break;
            case MARK_FINAL:
                highlightA = op.i; // can be used to mark sorted portion
                highlightB = -1;
                break;
        }
        repaint();
    }

    // Resets array back to original random/custom state
    public void resetToOriginal() {
        if (aux != null) array = aux.clone();
        resetHighlights();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (array == null) return;
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();
        int n = array.length;
        double barWidth = Math.max(1, (double) w / n);

        // Use a smaller font for large arrays (Adjusted for better visibility on medium size)
        int fontSize = (n <= 50) ? 14 : (n <= 120) ? 10 : 7;
        g2.setFont(new Font("Arial", Font.BOLD, fontSize));

        int max = 1;
        for (int v : array) if (v > max) max = v;

        for (int i = 0; i < n; i++) {
            int val = array[i];
            int barH = (int) ((val / (double) max) * (h - 20));
            int x = (int) (i * barWidth);
            int y = h - barH;

            // 1. Draw the Bar
            if (i == highlightA || i == highlightB) {
                g2.setColor(Color.RED);
            } else {
                // gradient color by value
                float hue = 0.6f - (float) val / max * 0.6f; // green->blueish
                g2.setColor(Color.getHSBColor(hue, 0.9f, 0.9f));
            }
            g2.fillRect(x, y, (int) Math.ceil(barWidth), barH);

            // 2. Draw the Array Value - CHECK VISIBILITY STATE HERE
            if (showNumbers) { // 🔹 NEW: Only draw if showNumbers is true
                String valueStr = String.valueOf(val);
                FontMetrics fm = g2.getFontMetrics();
                int strWidth = fm.stringWidth(valueStr);
                int strHeight = fm.getHeight();

                // Condition: Draw number only if the bar is wide enough to prevent extreme clutter
                if (barWidth > strWidth * 0.9 || n < 50) {

                    int textX = x + (int) (barWidth / 2) - (strWidth / 2);
                    int textY = y - 2; // 2 pixels above the bar top

                    // Draw a semi-transparent black background for contrast against bar color
                    g2.setColor(new Color(0, 0, 0, 180));
                    // Draw rectangle slightly larger than the text
                    g2.fillRect(textX - 2, textY - strHeight + fm.getAscent(), strWidth + 4, strHeight);

                    // Draw the white text on top of the dark background
                    g2.setColor(Color.WHITE);
                    g2.drawString(valueStr, textX, textY);
                }
            }
        }
    }
}

/* ---------------------------- Operation model ---------------------------- */
enum OpType { COMPARE, SWAP, OVERWRITE, MARK_FINAL }

class Operation {
    final OpType type;
    final int i, j;
    final int value; // used for OVERWRITE

    private Operation(OpType type, int i, int j, int value) {
        this.type = type;
        this.i = i;
        this.j = j;
        this.value = value;
    }

    public static Operation compare(int i, int j) { return new Operation(OpType.COMPARE, i, j, 0); }
    public static Operation swap(int i, int j) { return new Operation(OpType.SWAP, i, j, 0); }
    public static Operation overwrite(int i, int value) { return new Operation(OpType.OVERWRITE, i, -1, value); }
    public static Operation markFinal(int i) { return new Operation(OpType.MARK_FINAL, i, -1, 0); }
}

/* ---------------------------- Operation Player (animator) ---------------------------- */
class OperationPlayer {
    private final List<Operation> ops;
    private final VisualPanel panel;
    private final AtomicBoolean playing = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private javax.swing.Timer timer;
    private int cursor = 0;
    private final JLabel statusLabel;

    public OperationPlayer(List<Operation> ops, VisualPanel panel, int delayMs, JLabel status) {
        this.ops = ops;
        this.panel = panel;
        this.statusLabel = status;
        setDelay(delayMs);
    }

    public void start() {
        if (playing.get()) return;
        playing.set(true);
        paused.set(false);
        cursor = 0;
        timer.start();
    }

    public void setDelay(int delayMs) {
        if (timer != null) timer.setDelay(delayMs);
        // create timer if null
        if (timer == null) {
            timer = new javax.swing.Timer(Math.max(1, delayMs), e -> {
                if (!playing.get()) return;
                if (paused.get()) return;
                if (cursor >= ops.size()) {
                    stop();
                    statusLabel.setText("Status: Completed (" + ops.size() + " ops)");
                    return;
                }
                Operation op = ops.get(cursor++);
                panel.applyOperation(op);
            });
        } else {
            timer.setDelay(Math.max(1, delayMs));
        }
    }

    public void pause() { paused.set(true); }
    public void resume() { paused.set(false); }
    public boolean isPaused() { return paused.get(); }
    public boolean isPlaying() { return playing.get(); }

    public void stop() {
        playing.set(false);
        paused.set(false);
        if (timer != null) timer.stop();
    }
}

/* ---------------------------- Sorting algorithms that record operations ---------------------------- */
class SortingAlgorithms {
    // Bubble sort
    public static void bubbleSort(int[] a, List<Operation> ops) {
        int n = a.length;
        boolean swapped;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                ops.add(Operation.compare(j, j + 1));
                if (a[j] > a[j + 1]) {
                    ops.add(Operation.swap(j, j + 1));
                    int tmp = a[j]; a[j] = a[j + 1]; a[j + 1] = tmp;
                    swapped = true;
                }
            }
            ops.add(Operation.markFinal(n - 1 - i));
            if (!swapped) break;
        }
        // mark rest final
        for (int k = n - 1 - Math.max(0, n - 1); k >= 0; k--) {
            // no-op: already marked; left here for completeness
        }
    }

    // Selection sort
    public static void selectionSort(int[] a, List<Operation> ops) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                ops.add(Operation.compare(minIdx, j));
                if (a[j] < a[minIdx]) minIdx = j;
            }
            if (minIdx != i) {
                ops.add(Operation.swap(i, minIdx));
                int tmp = a[i]; a[i] = a[minIdx]; a[minIdx] = tmp;
            }
            ops.add(Operation.markFinal(i));
        }
        if (n > 0) ops.add(Operation.markFinal(n - 1));
    }

    // Insertion sort
    public static void insertionSort(int[] a, List<Operation> ops) {
        int n = a.length;
        for (int i = 1; i < n; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= 0) {
                ops.add(Operation.compare(j, j + 1));
                if (a[j] > key) {
                    ops.add(Operation.overwrite(j + 1, a[j]));
                    a[j + 1] = a[j];
                    j--;
                } else break;
            }
            ops.add(Operation.overwrite(j + 1, key));
            a[j + 1] = key;
        }
        for (int k = 0; k < n; k++) ops.add(Operation.markFinal(k));
    }

    // Merge sort (top-down)
    public static void mergeSort(int[] a, List<Operation> ops) {
        mergeSortRec(a, 0, a.length - 1, ops);
        for (int k = 0; k < a.length; k++) ops.add(Operation.markFinal(k));
    }

    private static void mergeSortRec(int[] a, int l, int r, List<Operation> ops) {
        if (l >= r) return;
        int m = (l + r) / 2;
        mergeSortRec(a, l, m, ops);
        mergeSortRec(a, m + 1, r, ops);
        // merge
        int[] tmp = new int[r - l + 1];
        int i = l, j = m + 1, k = 0;
        while (i <= m && j <= r) {
            ops.add(Operation.compare(i, j));
            if (a[i] <= a[j]) {
                tmp[k++] = a[i++];
            } else {
                tmp[k++] = a[j++];
            }
        }
        while (i <= m) tmp[k++] = a[i++];
        while (j <= r) tmp[k++] = a[j++];
        // write back
        for (int t = 0; t < tmp.length; t++) {
            ops.add(Operation.overwrite(l + t, tmp[t]));
            a[l + t] = tmp[t];
        }
    }

    // Quick sort (Lomuto partition)
    public static void quickSort(int[] a, List<Operation> ops) {
        quickSortRec(a, 0, a.length - 1, ops);
        for (int k = 0; k < a.length; k++) ops.add(Operation.markFinal(k));
    }

    private static void quickSortRec(int[] a, int low, int high, List<Operation> ops) {
        if (low < high) {
            int p = partition(a, low, high, ops);
            quickSortRec(a, low, p - 1, ops);
            quickSortRec(a, p + 1, high, ops);
        }
    }

    private static int partition(int[] a, int low, int high, List<Operation> ops) {
        int pivot = a[high];
        int i = low;
        for (int j = low; j < high; j++) {
            ops.add(Operation.compare(j, high));
            if (a[j] < pivot) {
                ops.add(Operation.swap(i, j));
                int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
                i++;
            }
        }
        ops.add(Operation.swap(i, high));
        int tmp = a[i]; a[i] = a[high]; a[high] = tmp;
        return i;
    }

    // Heap sort
    public static void heapSort(int[] a, List<Operation> ops) {
        int n = a.length;
        // build heap
        for (int i = n / 2 - 1; i >= 0; i--) heapify(a, n, i, ops);
        // extract
        for (int i = n - 1; i >= 0; i--) {
            ops.add(Operation.swap(0, i));
            int tmp = a[0]; a[0] = a[i]; a[i] = tmp;
            heapify(a, i, 0, ops);
            ops.add(Operation.markFinal(i));
        }
    }

    private static void heapify(int[] a, int n, int i, List<Operation> ops) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        if (l < n) {
            ops.add(Operation.compare(l, largest));
            if (a[l] > a[largest]) largest = l;
        }
        if (r < n) {
            ops.add(Operation.compare(r, largest));
            if (a[r] > a[largest]) largest = r;
        }
        if (largest != i) {
            ops.add(Operation.swap(i, largest));
            int tmp = a[i]; a[i] = a[largest]; a[largest] = tmp;
            heapify(a, n, largest, ops);
        }
    }
    
    // 🔹 NEW: Shell Sort
    public static void shellSort(int[] a, List<Operation> ops) {
        int n = a.length;
        // Start with a large gap, then reduce the gap
        for (int gap = n / 2; gap > 0; gap /= 2) {
            // Do a gapped insertion sort for this gap size.
            for (int i = gap; i < n; i++) {
                int key = a[i];
                int j = i;
                // Shift earlier gap-sorted elements up until the correct location for a[i] is found
                while (j >= gap) {
                    ops.add(Operation.compare(j - gap, j));
                    if (a[j - gap] > key) {
                        ops.add(Operation.overwrite(j, a[j - gap]));
                        a[j] = a[j - gap];
                        j -= gap;
                    } else {
                        break;
                    }
                }
                // Put key (the original a[i]) in its correct location
                ops.add(Operation.overwrite(j, key));
                a[j] = key;
            }
        }
        for (int k = 0; k < n; k++) ops.add(Operation.markFinal(k));
    }

    // 🔹 NEW: Radix Sort
    public static void radixSort(int[] a, List<Operation> ops) {
        if (a.length == 0) return;
        // Find the maximum number to know number of digits
        int max = a[0];
        for (int val : a) {
            if (val > max) max = val;
        }

        // Do counting sort for every digit.
        // The visualization will happen inside countingSort pass
        for (int exp = 1; max / exp > 0; exp *= 10) {
            countingSortForRadix(a, exp, ops);
        }
        for (int k = 0; k < a.length; k++) ops.add(Operation.markFinal(k));
    }
    
    // 🔹 NEW: Helper for Radix Sort
    private static void countingSortForRadix(int[] a, int exp, List<Operation> ops) {
        int n = a.length;
        int[] output = new int[n]; // output array
        int[] count = new int[10];
        Arrays.fill(count, 0);

        // Store count of occurrences in count[]
        for (int i = 0; i < n; i++) {
            count[(a[i] / exp) % 10]++;
        }

        // Change count[i] so that count[i] now contains
        // actual position of this digit in output[]
        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }

        // Build the output array
        for (int i = n - 1; i >= 0; i--) {
            int digit = (a[i] / exp) % 10;
            output[count[digit] - 1] = a[i];
            count[digit]--;
        }

        // Copy the output array to a[], so that a[] now
        // contains sorted numbers according to current digit
        // This is the step we visualize
        for (int i = 0; i < n; i++) {
            ops.add(Operation.overwrite(i, output[i]));
            a[i] = output[i];
        }
    }
}