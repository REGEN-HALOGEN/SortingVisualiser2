import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.swing.*;

/**
 * SortingVisualiser.java
 * Single-file Swing sorting visualizer.
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
        SwingUtilities.invokeLater(() -> createFrame().setVisible(true));
    }

    /**
     * Factory method used by the hub launcher.
     * Keeping frame creation behind a public class avoids source-launcher
     * class-loader access issues with the package-private VisualFrame type.
     */
    public static JFrame createFrame() {
        return new VisualFrame();
    }
}

@FunctionalInterface
interface SortExecutor {
    void sort(int[] array, List<Operation> ops);
}

class AlgorithmDefinition {
    private final String displayName;
    private final SortExecutor executor;
    private final String[] codeLines;

    public AlgorithmDefinition(String displayName, SortExecutor executor, String... codeLines) {
        this.displayName = displayName;
        this.executor = executor;
        this.codeLines = codeLines;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String[] getCodeLines() {
        return codeLines;
    }

    public void sort(int[] array, List<Operation> ops) {
        executor.sort(array, ops);
    }

    @Override
    public String toString() {
        return displayName;
    }
}

/* ---------------------------- UI Frame ---------------------------- */
class VisualFrame extends JFrame {
    private static final AlgorithmDefinition[] ALGORITHMS = createAlgorithms();

    private final VisualPanel visualPanel = new VisualPanel();
    private final JComboBox<AlgorithmDefinition> algoSelect = new JComboBox<>(ALGORITHMS);
    private final JSlider sizeSlider = new JSlider(1, 300, 80);
    private final JSlider speedSlider = new JSlider(10, 200, 80); // lower delay target = faster
    private final JButton randomizeBtn = new JButton("Randomize");
    private final JButton loadCustomBtn = new JButton("Load Custom Array");
    private final JButton startBtn = new JButton("Start");
    private final JButton pauseBtn = new JButton("Pause");
    private final JButton resetBtn = new JButton("Reset");
    private final JButton clearBtn = new JButton("Clear");
    private final JButton viewCodeBtn = new JButton("View Code");
    private final JLabel statusLabel = new JLabel("Status: Ready");
    private final JTextField sizeValueField = new JTextField("80");
    {
        sizeValueField.setPreferredSize(new Dimension(46, 24));
        sizeValueField.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private boolean skipRandomGeneration = false;
    private final JCheckBox numberToggle = new JCheckBox("Show Numbers", true);

    private OperationPlayer player;
    private CodeViewerDialog codeViewer;
    private AlgorithmDefinition activeAlgorithm;

    public VisualFrame() {
        super("Sorting Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        speedSlider.setInverted(true);

        add(visualPanel, BorderLayout.CENTER);
        add(buildControlPanel(), BorderLayout.SOUTH);

        visualPanel.clearArray();
        activeAlgorithm = getSelectedAlgorithm();
        pauseBtn.setEnabled(false);

        sizeValueField.addActionListener(e -> applyTypedSize());
        sizeValueField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                applyTypedSize();
            }
        });

        randomizeBtn.addActionListener(e -> {
            if (player != null && player.isPlaying())
                return;
            visualPanel.generateRandomArray(sizeSlider.getValue());
            statusLabel.setText("Status: Randomized");
        });

        loadCustomBtn.addActionListener(e -> {
            if (player != null && player.isPlaying())
                return;
            String input = JOptionPane.showInputDialog(this,
                    "Enter array elements separated by commas (e.g., 50,20,80,10):",
                    "Custom Array Input",
                    JOptionPane.PLAIN_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                loadCustomArray(input.trim());
            }
        });

        algoSelect.addActionListener(e -> {
            if (player != null && player.isPlaying())
                return;
            activeAlgorithm = getSelectedAlgorithm();
            refreshCodeViewer();
        });

        viewCodeBtn.addActionListener(e -> showCodeViewer());
        startBtn.addActionListener(e -> startSorting());

        pauseBtn.addActionListener(e -> {
            if (player != null) {
                if (player.isPaused()) {
                    player.resume();
                    pauseBtn.setText("Pause");
                    statusLabel.setText("Status: Playing");
                } else {
                    player.pause();
                    pauseBtn.setText("Resume");
                    statusLabel.setText("Status: Paused");
                }
            }
        });

        resetBtn.addActionListener(e -> {
            stopPlayback();
            visualPanel.resetToOriginal();
            statusLabel.setText("Status: Reset");
        });

        clearBtn.addActionListener(e -> {
            stopPlayback();
            visualPanel.clearArray();
            statusLabel.setText("Status: Cleared");
        });

        sizeSlider.addChangeListener(e -> {
            sizeValueField.setText(String.valueOf(sizeSlider.getValue()));
            if (!sizeSlider.getValueIsAdjusting()) {
                if (player != null && player.isPlaying())
                    return;
                if (skipRandomGeneration)
                    return;
                visualPanel.generateRandomArray(sizeSlider.getValue());
            }
        });

        speedSlider.addChangeListener(e -> {
            if (player != null)
                player.setDelay(speedToDelay(speedSlider.getValue()));
        });

        numberToggle.addActionListener(e -> visualPanel.setShowNumbers(numberToggle.isSelected()));

        pack();
    }

    private static AlgorithmDefinition[] createAlgorithms() {
        return new AlgorithmDefinition[] {
                new AlgorithmDefinition("Bubble Sort", SortingAlgorithms::bubbleSort,
                        "for (int i = 0; i < n - 1; i++) {",
                        "    boolean swapped = false;",
                        "    for (int j = 0; j < n - 1 - i; j++) {",
                        "        if (a[j] > a[j + 1]) {",
                        "            swap(a, j, j + 1);",
                        "            swapped = true;",
                        "        }",
                        "    }",
                        "    if (!swapped) break;",
                        "}"),
                new AlgorithmDefinition("Selection Sort", SortingAlgorithms::selectionSort,
                        "for (int i = 0; i < n - 1; i++) {",
                        "    int minIdx = i;",
                        "    for (int j = i + 1; j < n; j++) {",
                        "        if (a[j] < a[minIdx]) {",
                        "            minIdx = j;",
                        "        }",
                        "    }",
                        "    if (minIdx != i) {",
                        "        swap(a, i, minIdx);",
                        "    }",
                        "}"),
                new AlgorithmDefinition("Insertion Sort", SortingAlgorithms::insertionSort,
                        "for (int i = 1; i < n; i++) {",
                        "    int key = a[i];",
                        "    int j = i - 1;",
                        "    while (j >= 0 && a[j] > key) {",
                        "        a[j + 1] = a[j];",
                        "        j--;",
                        "    }",
                        "    a[j + 1] = key;",
                        "}"),
                new AlgorithmDefinition("Merge Sort", SortingAlgorithms::mergeSort,
                        "void mergeSort(int left, int right) {",
                        "    if (left >= right) return;",
                        "    int mid = (left + right) / 2;",
                        "    mergeSort(left, mid);",
                        "    mergeSort(mid + 1, right);",
                        "    while (i <= mid && j <= right) {",
                        "        if (a[i] <= a[j]) takeLeft(); else takeRight();",
                        "    }",
                        "    copyRemainingElements();",
                        "    writeMergedValuesBack();",
                        "}"),
                new AlgorithmDefinition("Quick Sort", SortingAlgorithms::quickSort,
                        "void quickSort(int low, int high) {",
                        "    if (low < high) {",
                        "        int pivotIndex = partition(low, high);",
                        "        quickSort(low, pivotIndex - 1);",
                        "        quickSort(pivotIndex + 1, high);",
                        "    }",
                        "}",
                        "int pivot = a[high];",
                        "for (int j = low; j < high; j++) {",
                        "    if (a[j] < pivot) {",
                        "        swap(a, i, j);",
                        "    }",
                        "}",
                        "swap(a, i, high);"),
                new AlgorithmDefinition("Heap Sort", SortingAlgorithms::heapSort,
                        "for (int i = n / 2 - 1; i >= 0; i--) {",
                        "    heapify(a, n, i);",
                        "}",
                        "for (int end = n - 1; end >= 0; end--) {",
                        "    swap(a, 0, end);",
                        "    heapify(a, end, 0);",
                        "}",
                        "if (left < n && a[left] > a[largest]) largest = left;",
                        "if (right < n && a[right] > a[largest]) largest = right;",
                        "if (largest != i) {",
                        "    swap(a, i, largest);",
                        "}"),
                new AlgorithmDefinition("Shell Sort", SortingAlgorithms::shellSort,
                        "for (int gap = n / 2; gap > 0; gap /= 2) {",
                        "    for (int i = gap; i < n; i++) {",
                        "        int key = a[i];",
                        "        while (j >= gap && a[j - gap] > key) {",
                        "            a[j] = a[j - gap];",
                        "            j -= gap;",
                        "        }",
                        "        a[j] = key;",
                        "    }",
                        "}"),
                new AlgorithmDefinition("Radix Sort", SortingAlgorithms::radixSort,
                        "int max = findMax(a);",
                        "for (int exp = 1; max / exp > 0; exp *= 10) {",
                        "    countDigitsForCurrentPlace(a, exp, count);",
                        "    buildPrefixSums(count);",
                        "    placeElementsIntoOutput(a, exp, output, count);",
                        "    for (int i = 0; i < a.length; i++) {",
                        "        a[i] = output[i];",
                        "    }",
                        "}")
        };
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Algorithm:"));
        top.add(algoSelect);
        top.add(viewCodeBtn);
        top.add(new JLabel("Size:"));
        top.add(sizeValueField);
        top.add(sizeSlider);
        top.add(new JLabel("Speed:"));
        top.add(speedSlider);
        top.add(randomizeBtn);
        top.add(loadCustomBtn);
        top.add(startBtn);
        top.add(pauseBtn);
        top.add(resetBtn);
        top.add(clearBtn);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(statusLabel);
        bottom.add(numberToggle);

        panel.add(top, BorderLayout.NORTH);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void loadCustomArray(String input) {
        try {
            String[] parts = input.split(",");
            int[] customArray = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                int value = Integer.parseInt(parts[i].trim());
                if (value <= 0)
                    throw new NumberFormatException("Value must be positive.");
                customArray[i] = value;
            }
            if (customArray.length > 0) {
                visualPanel.setCustomArray(customArray);
                statusLabel.setText("Status: Custom array loaded (" + customArray.length + " elements)");
                syncSizeControls(customArray.length);
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
        int min = 2;
        int max = 200;
        return min + (int) ((max - min) * (sliderValue - 1) / 199.0);
    }

    private void syncSizeControls(int size) {
        skipRandomGeneration = true;
        try {
            if (size > sizeSlider.getMaximum()) {
                sizeSlider.setMaximum(size);
            }
            sizeSlider.setValue(size);
            sizeValueField.setText(String.valueOf(size));
        } finally {
            skipRandomGeneration = false;
        }
    }

    private void applyTypedSize() {
        if (player != null && player.isPlaying()) {
            sizeValueField.setText(String.valueOf(sizeSlider.getValue()));
            return;
        }

        String input = sizeValueField.getText().trim();
        if (input.isEmpty()) {
            sizeValueField.setText(String.valueOf(sizeSlider.getValue()));
            return;
        }

        try {
            int typedValue = Integer.parseInt(input);
            int clampedValue = Math.max(sizeSlider.getMinimum(), Math.min(sizeSlider.getMaximum(), typedValue));
            if (typedValue != clampedValue) {
                statusLabel.setText("Status: Size adjusted to valid range.");
            }
            sizeSlider.setValue(clampedValue);
            sizeValueField.setText(String.valueOf(clampedValue));
        } catch (NumberFormatException ex) {
            sizeValueField.setText(String.valueOf(sizeSlider.getValue()));
            statusLabel.setText("Status: Enter a valid size number.");
        }
    }

    private AlgorithmDefinition getSelectedAlgorithm() {
        return (AlgorithmDefinition) algoSelect.getSelectedItem();
    }

    private void showCodeViewer() {
        if (codeViewer == null) {
            codeViewer = new CodeViewerDialog(this);
        }
        codeViewer.open(getSelectedAlgorithm());
        if (player == null || !player.isPlaying()) {
            codeViewer.clearHighlight();
        }
    }

    private void refreshCodeViewer() {
        if (codeViewer != null) {
            codeViewer.setAlgorithm(getSelectedAlgorithm());
            if (player == null || !player.isPlaying()) {
                codeViewer.clearHighlight();
            }
        }
    }

    private void updateCodeHighlight(int lineNumber) {
        if (codeViewer == null)
            return;
        if (activeAlgorithm != null) {
            codeViewer.setAlgorithm(activeAlgorithm);
        }
        if (lineNumber > 0) {
            codeViewer.highlightLine(lineNumber);
        } else {
            codeViewer.clearHighlight();
        }
    }

    private void setPlaybackControls(boolean playing) {
        algoSelect.setEnabled(!playing);
        sizeSlider.setEnabled(!playing);
        randomizeBtn.setEnabled(!playing);
        loadCustomBtn.setEnabled(!playing);
        startBtn.setEnabled(!playing);
        pauseBtn.setEnabled(playing);
        if (!playing) {
            pauseBtn.setText("Pause");
        }
    }

    private void stopPlayback() {
        if (player != null) {
            player.stop();
            player = null;
        } else {
            setPlaybackControls(false);
            updateCodeHighlight(-1);
        }
    }

    private void startSorting() {
        if (player != null && player.isPlaying())
            return;

        int[] arr = visualPanel.getArrayCopy();
        if (arr == null || arr.length == 0) {
            statusLabel.setText("Status: Load or randomize an array before sorting.");
            JOptionPane.showMessageDialog(this,
                    "Please randomize or load a custom array before starting the sort.",
                    "No Array Loaded",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        AlgorithmDefinition algorithm = getSelectedAlgorithm();
        activeAlgorithm = algorithm;
        visualPanel.resetHighlights();
        updateCodeHighlight(-1);
        setPlaybackControls(true);
        statusLabel.setText("Status: Measuring algorithm and generating animation...");

        new Thread(() -> {
            try {
                List<Operation> ops = new ArrayList<>();
                int[] workingCopy = arr.clone();
                long startNanos = System.nanoTime();
                algorithm.sort(workingCopy, ops);
                long algorithmTimeNanos = System.nanoTime() - startNanos;

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Status: Playing (" + algorithm.getDisplayName() + ")");
                    player = new OperationPlayer(ops,
                            visualPanel,
                            speedToDelay(speedSlider.getValue()),
                            statusLabel,
                            algorithmTimeNanos,
                            op -> {
                                if (op == null) {
                                    updateCodeHighlight(-1);
                                } else if (op.codeLine > 0) {
                                    updateCodeHighlight(op.codeLine);
                                }
                            },
                            () -> {
                                setPlaybackControls(false);
                                player = null;
                            });
                    player.start();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    setPlaybackControls(false);
                    updateCodeHighlight(-1);
                    statusLabel.setText("Status: Error generating operations.");
                    JOptionPane.showMessageDialog(this,
                            "Unable to generate the sorting animation.\n" + ex.getMessage(),
                            "Sorting Error",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "Sorting-Worker").start();
    }
}

/* ---------------------------- Visual Panel ---------------------------- */
class VisualPanel extends JPanel {
    private int[] array;
    private int[] aux; // keep a copy for reset
    private int highlightA = -1, highlightB = -1; // indices being compared/swapped
    private boolean showNumbers = true;

    public VisualPanel() {
        setPreferredSize(new Dimension(1000, 520));
        setBackground(Color.BLACK);
    }

    public void setShowNumbers(boolean show) {
        this.showNumbers = show;
        repaint();
    }

    public void setCustomArray(int[] customArray) {
        this.array = customArray.clone();
        this.aux = customArray.clone();
        resetHighlights();
        repaint();
    }

    public void generateRandomArray(int size) {
        Random rnd = new Random();
        array = new int[size];
        for (int i = 0; i < size; i++)
            array[i] = rnd.nextInt(400) + 5;
        aux = array.clone();
        resetHighlights();
        repaint();
    }

    public void clearArray() {
        this.array = null;
        this.aux = null;
        resetHighlights();
        repaint();
    }

    public int[] getArrayCopy() {
        return array == null ? null : array.clone();
    }

    public void resetHighlights() {
        highlightA = highlightB = -1;
        repaint();
    }

    public void applyOperation(Operation op) {
        applyOperation(op, true);
    }

    public void applyOperation(Operation op, boolean repaintAfter) {
        if (array == null || op == null)
            return;

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
                highlightA = op.i;
                highlightB = -1;
                break;
        }

        if (repaintAfter) {
            repaint();
        }
    }

    public void finishBatch() {
        repaint();
    }

    public void resetToOriginal() {
        if (aux != null)
            array = aux.clone();
        resetHighlights();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (array == null)
            return;

        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();
        int n = array.length;
        double barWidth = Math.max(1, (double) w / n);

        int fontSize = (n <= 50) ? 14 : (n <= 120) ? 10 : 7;
        g2.setFont(new Font("Arial", Font.BOLD, fontSize));

        int max = 1;
        for (int v : array)
            if (v > max)
                max = v;

        for (int i = 0; i < n; i++) {
            int val = array[i];
            int barH = (int) ((val / (double) max) * (h - 20));
            int x = (int) (i * barWidth);
            int y = h - barH;

            if (i == highlightA || i == highlightB) {
                g2.setColor(Color.RED);
            } else {
                float hue = 0.6f - (float) val / max * 0.6f;
                g2.setColor(Color.getHSBColor(hue, 0.9f, 0.9f));
            }
            g2.fillRect(x, y, (int) Math.ceil(barWidth), barH);

            if (showNumbers) {
                String valueStr = String.valueOf(val);
                FontMetrics fm = g2.getFontMetrics();
                int strWidth = fm.stringWidth(valueStr);
                int strHeight = fm.getHeight();

                if (barWidth > strWidth * 0.9 || n < 50) {
                    int textX = x + (int) (barWidth / 2) - (strWidth / 2);
                    int textY = y - 2;

                    g2.setColor(new Color(0, 0, 0, 180));
                    g2.fillRect(textX - 2, textY - strHeight + fm.getAscent(), strWidth + 4, strHeight);

                    g2.setColor(Color.WHITE);
                    g2.drawString(valueStr, textX, textY);
                }
            }
        }
    }
}

/* ---------------------------- Operation model ---------------------------- */
enum OpType {
    COMPARE, SWAP, OVERWRITE, MARK_FINAL
}

class Operation {
    final OpType type;
    final int i, j;
    final int value;
    final int codeLine;

    private Operation(OpType type, int i, int j, int value, int codeLine) {
        this.type = type;
        this.i = i;
        this.j = j;
        this.value = value;
        this.codeLine = codeLine;
    }

    public static Operation compare(int i, int j) {
        return compare(i, j, -1);
    }

    public static Operation compare(int i, int j, int codeLine) {
        return new Operation(OpType.COMPARE, i, j, 0, codeLine);
    }

    public static Operation swap(int i, int j) {
        return swap(i, j, -1);
    }

    public static Operation swap(int i, int j, int codeLine) {
        return new Operation(OpType.SWAP, i, j, 0, codeLine);
    }

    public static Operation overwrite(int i, int value) {
        return overwrite(i, value, -1);
    }

    public static Operation overwrite(int i, int value, int codeLine) {
        return new Operation(OpType.OVERWRITE, i, -1, value, codeLine);
    }

    public static Operation markFinal(int i) {
        return markFinal(i, -1);
    }

    public static Operation markFinal(int i, int codeLine) {
        return new Operation(OpType.MARK_FINAL, i, -1, 0, codeLine);
    }
}

/* ---------------------------- Code Viewer ---------------------------- */
class CodeViewerDialog extends JDialog {
    private final DefaultListModel<String> codeModel = new DefaultListModel<>();
    private final JList<String> codeList = new JList<>(codeModel);
    private AlgorithmDefinition currentAlgorithm;
    private int highlightedLine = -1;

    public CodeViewerDialog(JFrame owner) {
        super(owner, "Algorithm Code", false);
        setSize(520, 420);
        setLocationRelativeTo(owner);

        codeList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        codeList.setBackground(new Color(18, 18, 24));
        codeList.setForeground(new Color(240, 240, 245));
        codeList.setFixedCellHeight(26);
        codeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        codeList.setFocusable(false);
        codeList.setCellRenderer(new CodeLineRenderer());

        JScrollPane scrollPane = new JScrollPane(codeList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    public void open(AlgorithmDefinition algorithm) {
        setAlgorithm(algorithm);
        setVisible(true);
        toFront();
    }

    public void setAlgorithm(AlgorithmDefinition algorithm) {
        if (algorithm == null)
            return;
        if (algorithm == currentAlgorithm && !codeModel.isEmpty())
            return;

        currentAlgorithm = algorithm;
        setTitle("Algorithm Code - " + algorithm.getDisplayName());

        codeModel.clear();
        String[] lines = algorithm.getCodeLines();
        for (int i = 0; i < lines.length; i++) {
            codeModel.addElement(lines[i]);
        }

        clearHighlight();
    }

    public void clearHighlight() {
        highlightedLine = -1;
        codeList.clearSelection();
        codeList.repaint();
    }

    public void highlightLine(int lineNumber) {
        if (currentAlgorithm == null)
            return;
        if (lineNumber < 1 || lineNumber > currentAlgorithm.getCodeLines().length) {
            clearHighlight();
            return;
        }

        highlightedLine = lineNumber - 1;
        codeList.setSelectedIndex(highlightedLine);
        codeList.ensureIndexIsVisible(highlightedLine);
        codeList.repaint();
    }

    private final class CodeLineRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel lineNumberLabel = new JLabel();
        private final JLabel codeLabel = new JLabel();

        private CodeLineRenderer() {
            setLayout(new BorderLayout(12, 0));
            setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            setOpaque(true);

            lineNumberLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
            lineNumberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            lineNumberLabel.setPreferredSize(new Dimension(34, 18));

            codeLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));

            add(lineNumberLabel, BorderLayout.WEST);
            add(codeLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                String value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            boolean active = index == highlightedLine;

            setBackground(active ? new Color(255, 235, 59, 185) : new Color(18, 18, 24));
            lineNumberLabel.setForeground(active ? new Color(60, 46, 0) : new Color(120, 130, 150));
            codeLabel.setForeground(active ? new Color(28, 28, 28) : new Color(240, 240, 245));

            lineNumberLabel.setText(String.format("%2d", index + 1));
            codeLabel.setText(value);

            return this;
        }
    }
}

/*
 * ---------------------------- Operation Player (animator)
 * ----------------------------
 */
class OperationPlayer {
    private static final int FRAME_DELAY_MS = 16;
    private static final int MAX_STEPS_PER_FRAME = 32;

    private final List<Operation> ops;
    private final VisualPanel panel;
    private final AtomicBoolean playing = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final JLabel statusLabel;
    private final long algorithmTimeNanos;
    private final Consumer<Operation> stepListener;
    private final Runnable finishCallback;
    private final javax.swing.Timer timer;

    private int cursor = 0;
    private int targetDelayMs;
    private long lastTickNanos;
    private double stepAccumulator = 0.0;
    private boolean finishNotified = false;

    public OperationPlayer(List<Operation> ops,
            VisualPanel panel,
            int delayMs,
            JLabel statusLabel,
            long algorithmTimeNanos,
            Consumer<Operation> stepListener,
            Runnable finishCallback) {
        this.ops = ops;
        this.panel = panel;
        this.statusLabel = statusLabel;
        this.algorithmTimeNanos = algorithmTimeNanos;
        this.stepListener = stepListener;
        this.finishCallback = finishCallback;
        this.targetDelayMs = Math.max(1, delayMs);

        timer = new javax.swing.Timer(FRAME_DELAY_MS, e -> advanceFrame());
        timer.setCoalesce(true);
    }

    public void start() {
        if (playing.get())
            return;

        cursor = 0;
        stepAccumulator = 0.0;
        finishNotified = false;
        lastTickNanos = System.nanoTime();
        playing.set(true);
        paused.set(false);

        if (stepListener != null) {
            stepListener.accept(null);
        }

        if (ops.isEmpty()) {
            finishPlayback();
            return;
        }

        timer.start();
    }

    public void setDelay(int delayMs) {
        targetDelayMs = Math.max(1, delayMs);
    }

    private void advanceFrame() {
        if (!playing.get() || paused.get())
            return;

        long now = System.nanoTime();
        double elapsedMs = (now - lastTickNanos) / 1_000_000.0;
        lastTickNanos = now;
        stepAccumulator += elapsedMs / targetDelayMs;

        int stepsThisFrame = Math.min(MAX_STEPS_PER_FRAME, (int) stepAccumulator);
        if (stepsThisFrame <= 0)
            return;

        stepAccumulator -= stepsThisFrame;

        Operation lastApplied = null;
        for (int i = 0; i < stepsThisFrame && cursor < ops.size(); i++) {
            lastApplied = ops.get(cursor++);
            panel.applyOperation(lastApplied, false);
        }

        if (lastApplied != null) {
            panel.finishBatch();
            if (stepListener != null) {
                stepListener.accept(lastApplied);
            }
        }

        if (cursor >= ops.size()) {
            finishPlayback();
        }
    }

    public void pause() {
        paused.set(true);
    }

    public void resume() {
        paused.set(false);
        lastTickNanos = System.nanoTime();
    }

    public boolean isPaused() {
        return paused.get();
    }

    public boolean isPlaying() {
        return playing.get();
    }

    public void stop() {
        playing.set(false);
        paused.set(false);
        timer.stop();
        notifyFinished();
    }

    private void finishPlayback() {
        playing.set(false);
        paused.set(false);
        timer.stop();

        String timeStr = String.format("%.3f ms", algorithmTimeNanos / 1_000_000.0);
        statusLabel.setText(
                "Status: Completed Sorting in (" + ops.size() + " Operations) and (" + timeStr + " Algorithm Time)");
        notifyFinished();
    }

    private void notifyFinished() {
        if (finishNotified)
            return;

        finishNotified = true;
        if (stepListener != null) {
            stepListener.accept(null);
        }
        if (finishCallback != null) {
            finishCallback.run();
        }
    }
}

/*
 * ---------------------------- Sorting algorithms that record operations
 * ----------------------------
 */
class SortingAlgorithms {
    public static void bubbleSort(int[] a, List<Operation> ops) {
        int n = a.length;
        boolean swapped;
        final int compareLine = 4;
        final int swapLine = 5;

        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                ops.add(Operation.compare(j, j + 1, compareLine));
                if (a[j] > a[j + 1]) {
                    ops.add(Operation.swap(j, j + 1, swapLine));
                    int tmp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = tmp;
                    swapped = true;
                }
            }
            ops.add(Operation.markFinal(n - 1 - i));
            if (!swapped) {
                for (int k = n - 2 - i; k >= 0; k--) {
                    ops.add(Operation.markFinal(k));
                }
                return;
            }
        }
        if (n == 1) {
            ops.add(Operation.markFinal(0));
        }
    }

    public static void selectionSort(int[] a, List<Operation> ops) {
        int n = a.length;
        final int compareLine = 4;
        final int swapLine = 9;

        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                ops.add(Operation.compare(minIdx, j, compareLine));
                if (a[j] < a[minIdx])
                    minIdx = j;
            }
            if (minIdx != i) {
                ops.add(Operation.swap(i, minIdx, swapLine));
                int tmp = a[i];
                a[i] = a[minIdx];
                a[minIdx] = tmp;
            }
            ops.add(Operation.markFinal(i));
        }
        if (n > 0)
            ops.add(Operation.markFinal(n - 1));
    }

    public static void insertionSort(int[] a, List<Operation> ops) {
        int n = a.length;
        final int compareLine = 4;
        final int shiftLine = 5;
        final int insertLine = 8;

        for (int i = 1; i < n; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= 0) {
                ops.add(Operation.compare(j, j + 1, compareLine));
                if (a[j] > key) {
                    ops.add(Operation.overwrite(j + 1, a[j], shiftLine));
                    a[j + 1] = a[j];
                    j--;
                } else {
                    break;
                }
            }
            ops.add(Operation.overwrite(j + 1, key, insertLine));
            a[j + 1] = key;
        }
        for (int k = 0; k < n; k++)
            ops.add(Operation.markFinal(k));
    }

    public static void mergeSort(int[] a, List<Operation> ops) {
        mergeSortRec(a, 0, a.length - 1, ops);
        for (int k = 0; k < a.length; k++)
            ops.add(Operation.markFinal(k));
    }

    private static void mergeSortRec(int[] a, int l, int r, List<Operation> ops) {
        if (l >= r)
            return;

        int m = (l + r) / 2;
        mergeSortRec(a, l, m, ops);
        mergeSortRec(a, m + 1, r, ops);

        int[] tmp = new int[r - l + 1];
        int i = l, j = m + 1, k = 0;
        final int compareLine = 7;
        final int overwriteLine = 10;

        while (i <= m && j <= r) {
            ops.add(Operation.compare(i, j, compareLine));
            if (a[i] <= a[j]) {
                tmp[k++] = a[i++];
            } else {
                tmp[k++] = a[j++];
            }
        }
        while (i <= m)
            tmp[k++] = a[i++];
        while (j <= r)
            tmp[k++] = a[j++];

        for (int t = 0; t < tmp.length; t++) {
            ops.add(Operation.overwrite(l + t, tmp[t], overwriteLine));
            a[l + t] = tmp[t];
        }
    }

    public static void quickSort(int[] a, List<Operation> ops) {
        quickSortRec(a, 0, a.length - 1, ops);
        for (int k = 0; k < a.length; k++)
            ops.add(Operation.markFinal(k));
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
        final int compareLine = 10;
        final int swapLine = 11;
        final int finalSwapLine = 14;

        for (int j = low; j < high; j++) {
            ops.add(Operation.compare(j, high, compareLine));
            if (a[j] < pivot) {
                ops.add(Operation.swap(i, j, swapLine));
                int tmp = a[i];
                a[i] = a[j];
                a[j] = tmp;
                i++;
            }
        }
        ops.add(Operation.swap(i, high, finalSwapLine));
        int tmp = a[i];
        a[i] = a[high];
        a[high] = tmp;
        return i;
    }

    public static void heapSort(int[] a, List<Operation> ops) {
        int n = a.length;
        final int extractSwapLine = 5;

        for (int i = n / 2 - 1; i >= 0; i--)
            heapify(a, n, i, ops);

        for (int i = n - 1; i >= 0; i--) {
            ops.add(Operation.swap(0, i, extractSwapLine));
            int tmp = a[0];
            a[0] = a[i];
            a[i] = tmp;
            heapify(a, i, 0, ops);
            ops.add(Operation.markFinal(i));
        }
    }

    private static void heapify(int[] a, int n, int i, List<Operation> ops) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        final int leftCompareLine = 8;
        final int rightCompareLine = 9;
        final int swapLine = 11;

        if (l < n) {
            ops.add(Operation.compare(l, largest, leftCompareLine));
            if (a[l] > a[largest])
                largest = l;
        }
        if (r < n) {
            ops.add(Operation.compare(r, largest, rightCompareLine));
            if (a[r] > a[largest])
                largest = r;
        }
        if (largest != i) {
            ops.add(Operation.swap(i, largest, swapLine));
            int tmp = a[i];
            a[i] = a[largest];
            a[largest] = tmp;
            heapify(a, n, largest, ops);
        }
    }

    public static void shellSort(int[] a, List<Operation> ops) {
        int n = a.length;
        final int compareLine = 4;
        final int shiftLine = 5;
        final int insertLine = 8;

        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                int key = a[i];
                int j = i;
                while (j >= gap) {
                    ops.add(Operation.compare(j - gap, j, compareLine));
                    if (a[j - gap] > key) {
                        ops.add(Operation.overwrite(j, a[j - gap], shiftLine));
                        a[j] = a[j - gap];
                        j -= gap;
                    } else {
                        break;
                    }
                }
                ops.add(Operation.overwrite(j, key, insertLine));
                a[j] = key;
            }
        }
        for (int k = 0; k < n; k++)
            ops.add(Operation.markFinal(k));
    }

    public static void radixSort(int[] a, List<Operation> ops) {
        if (a.length == 0)
            return;

        int max = a[0];
        for (int val : a) {
            if (val > max)
                max = val;
        }

        for (int exp = 1; max / exp > 0; exp *= 10) {
            countingSortForRadix(a, exp, ops);
        }
        for (int k = 0; k < a.length; k++)
            ops.add(Operation.markFinal(k));
    }

    private static void countingSortForRadix(int[] a, int exp, List<Operation> ops) {
        int n = a.length;
        int[] output = new int[n];
        int[] count = new int[10];
        Arrays.fill(count, 0);
        final int overwriteLine = 7;

        for (int i = 0; i < n; i++) {
            count[(a[i] / exp) % 10]++;
        }

        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }

        for (int i = n - 1; i >= 0; i--) {
            int digit = (a[i] / exp) % 10;
            output[count[digit] - 1] = a[i];
            count[digit]--;
        }

        for (int i = 0; i < n; i++) {
            ops.add(Operation.overwrite(i, output[i], overwriteLine));
            a[i] = output[i];
        }
    }
}