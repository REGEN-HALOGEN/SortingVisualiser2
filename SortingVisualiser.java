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
 * javac -cp ".;flatlaf.jar" SortingVisualiser.java
 * Run:
 * java -cp ".;flatlaf.jar" SortingVisualiser
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
        return createFrame(true);
    }

    public static JFrame createFrame(boolean isDark) {
        try {
            UIManager.setLookAndFeel(isDark ? "com.formdev.flatlaf.FlatDarkLaf" : "com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // ignore fallback errors
            }
        }
        VisualFrame frame = new VisualFrame();
        return frame;
    }
}

@FunctionalInterface
interface SortExecutor {
    void sort(int[] array, List<Operation> ops, SortMetrics metrics);
}

class AlgorithmDefinition {
    private final String displayName;
    private final String timeComplexity;
    private final String spaceComplexity;
    private final SortExecutor executor;
    private final String[] codeLines;

    public AlgorithmDefinition(String displayName, String timeComplexity, String spaceComplexity, SortExecutor executor,
            String... codeLines) {
        this.displayName = displayName;
        this.timeComplexity = timeComplexity;
        this.spaceComplexity = spaceComplexity;
        this.executor = executor;
        this.codeLines = codeLines;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTimeComplexity() {
        return timeComplexity;
    }

    public String getSpaceComplexity() {
        return spaceComplexity;
    }

    public String[] getCodeLines() {
        return codeLines;
    }

    public void sort(int[] array, List<Operation> ops, SortMetrics metrics) {
        executor.sort(array, ops, metrics);
    }

    @Override
    public String toString() {
        return displayName;
    }
}

/* ---------------------------- UI Frame ---------------------------- */
class VisualFrame extends JFrame {
    static final AlgorithmDefinition[] ALGORITHMS = createAlgorithms();

    private final VisualPanel visualPanel = new VisualPanel();
    private final JComboBox<AlgorithmDefinition> algoSelect = new JComboBox<>(ALGORITHMS);
    private final JSlider sizeSlider = new JSlider(1, 300, 80);
    private final JSlider speedSlider = new JSlider(10, 200, 80); // lower delay target = faster
    private final JButton randomizeBtn = new JButton("🔀 Randomize");
    private final JButton loadCustomBtn = new JButton("📂 Load Custom Array");
    private final JButton startBtn = new JButton("▶ Start");
    private final JButton pauseBtn = new JButton("⏸ Pause");
    private final JButton resetBtn = new JButton("🔄 Reset");
    private final JButton clearBtn = new JButton("✖ Clear");
    private final JButton viewCodeBtn = new JButton("📝 View Code");
    private final JButton analysisBtn = new JButton("📊 Sort Analysis");
    private final JButton compareBtn = new JButton("🏁 Compare");
    private final JLabel statusLabel = new JLabel("Status: Ready");
    private final JTextField sizeValueField = new JTextField("80");
    {
        sizeValueField.setPreferredSize(new Dimension(46, 24));
        sizeValueField.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private boolean skipRandomGeneration = false;
    private final JCheckBox numberToggle = new JCheckBox("Show Numbers", true);
    private final JCheckBox auxToggle = new JCheckBox("Show Aux", true);

    private OperationPlayer player;
    private CodeViewerDialog codeViewer;
    private AlgorithmDefinition activeAlgorithm;

    private final List<SortStats> history = new ArrayList<>();

    public VisualFrame() {
        super("Sorting Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Add tooltips to UI components
        algoSelect.setToolTipText("Select the sorting algorithm to visualize.");
        sizeSlider.setToolTipText("Adjust the size (number of elements) of the array.");
        speedSlider.setToolTipText("Adjust the sorting animation speed.");
        randomizeBtn.setToolTipText("Generate a new random array of the selected size.");
        loadCustomBtn.setToolTipText("Input a custom list of numbers to sort.");
        startBtn.setToolTipText("Start the sorting visualization.");
        pauseBtn.setToolTipText("Pause or resume the current visualization.");
        resetBtn.setToolTipText("Reset the array to its initial unsorted state.");
        clearBtn.setToolTipText("Clear the array entirely.");
        viewCodeBtn.setToolTipText("View the source code for the selected algorithm.");
        analysisBtn.setToolTipText("View statistics and history of past sorts.");
        compareBtn.setToolTipText("Compare multiple algorithms running simultaneously.");
        sizeValueField.setToolTipText("Current array size.");
        numberToggle.setToolTipText("Toggle display of numeric values on the bars.");
        auxToggle.setToolTipText(
                "Toggle display of auxiliary space arrays during out-of-place sorting (e.g., Merge Sort).");

        speedSlider.setInverted(true);

        add(visualPanel, BorderLayout.CENTER);
        JPanel controlPanel = buildControlPanel();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(controlPanel, BorderLayout.SOUTH);

        visualPanel.clearArray();
        activeAlgorithm = getSelectedAlgorithm();
        pauseBtn.setEnabled(false);
        analysisBtn.setEnabled(true);

        analysisBtn.addActionListener(e -> new SortAnalysisDialog(this, history).setVisible(true));
        compareBtn.addActionListener(e -> new CompareFrame(ALGORITHMS).setVisible(true));

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
                    pauseBtn.setText("⏸ Pause");
                    statusLabel.setText("Status: Playing");
                } else {
                    player.pause();
                    pauseBtn.setText("▶ Resume");
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
        auxToggle.addActionListener(e -> visualPanel.setShowAux(auxToggle.isSelected()));

        pack();
    }

    private static AlgorithmDefinition[] createAlgorithms() {
        return new AlgorithmDefinition[] {
                new AlgorithmDefinition("Bubble Sort", "O(N²)", "O(1)", SortingAlgorithms::bubbleSort,
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
                new AlgorithmDefinition("Selection Sort", "O(N²)", "O(1)", SortingAlgorithms::selectionSort,
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
                new AlgorithmDefinition("Insertion Sort", "O(N²)", "O(1)", SortingAlgorithms::insertionSort,
                        "for (int i = 1; i < n; i++) {",
                        "    int key = a[i];",
                        "    int j = i - 1;",
                        "    while (j >= 0 && a[j] > key) {",
                        "        a[j + 1] = a[j];",
                        "        j--;",
                        "    }",
                        "    a[j + 1] = key;",
                        "}"),
                new AlgorithmDefinition("Merge Sort", "O(N log N)", "O(N)", SortingAlgorithms::mergeSort,
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
                new AlgorithmDefinition("Quick Sort", "O(N log N)", "O(log N)", SortingAlgorithms::quickSort,
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
                new AlgorithmDefinition("Heap Sort", "O(N log N)", "O(1)", SortingAlgorithms::heapSort,
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
                new AlgorithmDefinition("Shell Sort", "O(N log² N)", "O(1)", SortingAlgorithms::shellSort,
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
                new AlgorithmDefinition("Radix Sort", "O(NK)", "O(N)", SortingAlgorithms::radixSort,
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
        bottom.add(Box.createHorizontalStrut(20));
        bottom.add(numberToggle);
        bottom.add(auxToggle);
        bottom.add(analysisBtn);
        bottom.add(compareBtn);

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
                SortMetrics metrics = new SortMetrics();

                System.gc(); // Hint GC to run to get a better memory baseline
                long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                long startNanos = System.nanoTime();
                algorithm.sort(workingCopy, ops, metrics);
                long algorithmTimeNanos = System.nanoTime() - startNanos;
                long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                long memUsed = Math.max(0, memAfter - memBefore);
                SortStats stats = new SortStats(algorithm.getDisplayName(), algorithmTimeNanos, memUsed,
                        algorithm.getTimeComplexity(), algorithm.getSpaceComplexity(), metrics.swaps,
                        metrics.arrayWrites, metrics.arrayReads, metrics.comparisons, metrics.peakAuxSpace, arr.length);

                SwingUtilities.invokeLater(() -> {
                    history.add(stats);
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
                                analysisBtn.setEnabled(true);
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
    private int[] visualAuxSpace; // aux array for visualization
    private int highlightA = -1, highlightB = -1; // indices being compared/swapped
    private int peakAuxElements = 0;
    private boolean showNumbers = true;
    private boolean showAux = true;

    public VisualPanel() {
        setPreferredSize(new Dimension(1000, 520));
        setBackground(Color.BLACK);
    }

    public void setShowNumbers(boolean show) {
        this.showNumbers = show;
        repaint();
    }

    public void setShowAux(boolean show) {
        this.showAux = show;
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
        this.visualAuxSpace = null;
        this.peakAuxElements = 0;
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
        if (op == null)
            return;

        switch (op.type) {
            case COMPARE:
                highlightA = op.i;
                highlightB = op.j;
                break;
            case SWAP:
                if (array != null) {
                    int tmp = array[op.i];
                    array[op.i] = array[op.j];
                    array[op.j] = tmp;
                }
                highlightA = op.i;
                highlightB = op.j;
                break;
            case OVERWRITE:
                if (array != null) {
                    array[op.i] = op.value;
                }
                highlightA = op.i;
                highlightB = -1;
                break;
            case MARK_FINAL:
                highlightA = op.i;
                highlightB = -1;
                break;
            case AUX_ALLOCATE:
                if (visualAuxSpace == null || visualAuxSpace.length != op.value) {
                    visualAuxSpace = new int[op.value];
                }
                if (op.value > peakAuxElements)
                    peakAuxElements = op.value;
                break;
            case AUX_WRITE:
                if (visualAuxSpace != null && op.i >= 0 && op.i < visualAuxSpace.length) {
                    visualAuxSpace[op.i] = op.value;
                }
                break;
            case AUX_CLEAR:
                visualAuxSpace = null;
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
        visualAuxSpace = null;
        peakAuxElements = 0;
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

        int mainH = (visualAuxSpace != null && showAux) ? (int) (h * 0.7) : h;
        int auxH = h - mainH;

        int n = array.length;
        double barWidth = Math.max(1, (double) w / n);

        int fontSize = (n <= 50) ? 14 : (n <= 120) ? 10 : 7;
        g2.setFont(new Font("Arial", Font.BOLD, fontSize));

        int max = 1;
        for (int v : array)
            if (v > max)
                max = v;

        // Draw main array
        for (int i = 0; i < n; i++) {
            int val = array[i];
            int barH = (int) ((val / (double) max) * (mainH - 20));
            int x = (int) (i * barWidth);
            int y = mainH - barH;

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

        // Draw aux array if present
        if (visualAuxSpace != null && showAux) {
            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(0, mainH, w, mainH);

            g2.setColor(Color.LIGHT_GRAY);
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString("Current Aux Space Elements: " + visualAuxSpace.length + " | Peak: " + peakAuxElements, 10,
                    mainH + 15);

            int auxN = visualAuxSpace.length;
            if (auxN > 0) {
                double auxBarWidth = Math.max(1, (double) w / auxN);
                for (int i = 0; i < auxN; i++) {
                    int val = visualAuxSpace[i];
                    if (val <= 0)
                        continue; // don't draw uninitialized

                    int barH = (int) ((val / (double) max) * (auxH - 25));
                    int x = (int) (i * auxBarWidth);
                    int y = h - barH;

                    g2.setColor(Color.getHSBColor(0.2f, 0.8f, 0.8f)); // Use a different hue for aux array
                    g2.fillRect(x, y, (int) Math.ceil(auxBarWidth), barH);
                }
            }
        }
    }
}

/* ---------------------------- Operation model ---------------------------- */
enum OpType {
    COMPARE, SWAP, OVERWRITE, MARK_FINAL, AUX_ALLOCATE, AUX_WRITE, AUX_CLEAR
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

    public static Operation auxAllocate(int size) {
        return new Operation(OpType.AUX_ALLOCATE, -1, -1, size, -1);
    }

    public static Operation auxWrite(int i, int value) {
        return new Operation(OpType.AUX_WRITE, i, -1, value, -1); // value could be any
    }

    public static Operation auxClear() {
        return new Operation(OpType.AUX_CLEAR, -1, -1, 0, -1);
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
    public static void bubbleSort(int[] array, List<Operation> ops, SortMetrics metrics) {
        TrackedArray a = new TrackedArray(array, ops, metrics);
        int n = a.length();
        boolean swapped;
        final int compareLine = 4;
        final int swapLine = 5;

        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (a.getVisualCompare(j, j + 1, compareLine) > 0) {
                    a.swap(j, j + 1, swapLine);
                    swapped = true;
                }
            }
            a.markFinal(n - 1 - i);
            if (!swapped) {
                for (int k = n - 2 - i; k >= 0; k--)
                    a.markFinal(k);
                return;
            }
        }
        if (n == 1)
            a.markFinal(0);
    }

    public static void selectionSort(int[] array, List<Operation> ops, SortMetrics metrics) {
        TrackedArray a = new TrackedArray(array, ops, metrics);
        int n = a.length();
        final int compareLine = 4;
        final int swapLine = 9;

        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (a.getVisualCompare(j, minIdx, compareLine) < 0)
                    minIdx = j;
            }
            if (minIdx != i) {
                a.swap(i, minIdx, swapLine);
            }
            a.markFinal(i);
        }
        if (n > 0)
            a.markFinal(n - 1);
    }

    public static void insertionSort(int[] array, List<Operation> ops, SortMetrics metrics) {
        TrackedArray a = new TrackedArray(array, ops, metrics);
        int n = a.length();
        final int compareLine = 4;
        final int shiftLine = 5;
        final int insertLine = 8;

        for (int i = 1; i < n; i++) {
            int key = a.get(i);
            int j = i - 1;
            while (j >= 0) {
                a.compareVisual(j, j + 1, compareLine);
                metrics.comparisons++;
                if (a.get(j) > key) {
                    a.setVisual(j + 1, a.get(j), shiftLine); // includes read and write implicitly visually
                    j--;
                } else {
                    break;
                }
            }
            a.setVisual(j + 1, key, insertLine);
        }
        for (int k = 0; k < n; k++)
            a.markFinal(k);
    }

    public static void mergeSort(int[] array, List<Operation> ops, SortMetrics metrics) {
        TrackedArray a = new TrackedArray(array, ops, metrics);
        mergeSortRec(a, 0, a.length() - 1);
        for (int k = 0; k < a.length(); k++)
            a.markFinal(k);
    }

    private static void mergeSortRec(TrackedArray a, int l, int r) {
        if (l >= r)
            return;
        int m = (l + r) / 2;
        mergeSortRec(a, l, m);
        mergeSortRec(a, m + 1, r);

        a.allocateAux(r - l + 1);
        int[] tmp = new int[r - l + 1];
        int i = l, j = m + 1, k = 0;
        final int compareLine = 7;
        final int overwriteLine = 10;

        while (i <= m && j <= r) {
            a.compareVisual(i, j, compareLine);
            a.metrics.comparisons++;
            if (a.get(i) <= a.get(j)) {
                tmp[k] = a.get(i++);
                a.writeAux(k, tmp[k]);
                k++;
            } else {
                tmp[k] = a.get(j++);
                a.writeAux(k, tmp[k]);
                k++;
            }
        }
        while (i <= m) {
            tmp[k] = a.get(i++);
            a.writeAux(k, tmp[k]);
            k++;
        }
        while (j <= r) {
            tmp[k] = a.get(j++);
            a.writeAux(k, tmp[k]);
            k++;
        }

        for (int t = 0; t < tmp.length; t++) {
            a.setVisual(l + t, tmp[t], overwriteLine); // arrayWrites implicitly in setVisual
        }
        a.clearAux(r - l + 1);
    }

    public static void quickSort(int[] array, List<Operation> ops, SortMetrics metrics) {
        TrackedArray a = new TrackedArray(array, ops, metrics);
        quickSortRec(a, 0, a.length() - 1);
        for (int k = 0; k < a.length(); k++)
            a.markFinal(k);
    }

    private static void quickSortRec(TrackedArray a, int low, int high) {
        if (low < high) {
            int p = partition(a, low, high);
            quickSortRec(a, low, p - 1);
            quickSortRec(a, p + 1, high);
        }
    }

    private static int partition(TrackedArray a, int low, int high) {
        int pivot = a.get(high);
        int i = low;
        final int compareLine = 10;
        final int swapLine = 11;
        final int finalSwapLine = 14;

        for (int j = low; j < high; j++) {
            a.compareVisual(j, high, compareLine);
            a.metrics.comparisons++;
            if (a.get(j) < pivot) {
                a.swap(i, j, swapLine);
                i++;
            }
        }
        a.swap(i, high, finalSwapLine);
        return i;
    }

    public static void heapSort(int[] array, List<Operation> ops, SortMetrics metrics) {
        TrackedArray a = new TrackedArray(array, ops, metrics);
        int n = a.length();
        final int extractSwapLine = 5;

        for (int i = n / 2 - 1; i >= 0; i--)
            heapify(a, n, i);

        for (int i = n - 1; i >= 0; i--) {
            a.swap(0, i, extractSwapLine);
            heapify(a, i, 0);
            a.markFinal(i);
        }
    }

    private static void heapify(TrackedArray a, int n, int i) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        final int leftCompareLine = 8;
        final int rightCompareLine = 9;
        final int swapLine = 11;

        if (l < n) {
            a.compareVisual(l, largest, leftCompareLine);
            a.metrics.comparisons++;
            if (a.get(l) > a.get(largest))
                largest = l;
        }
        if (r < n) {
            a.compareVisual(r, largest, rightCompareLine);
            a.metrics.comparisons++;
            if (a.get(r) > a.get(largest))
                largest = r;
        }
        if (largest != i) {
            a.swap(i, largest, swapLine);
            heapify(a, n, largest);
        }
    }

    public static void shellSort(int[] array, List<Operation> ops, SortMetrics metrics) {
        TrackedArray a = new TrackedArray(array, ops, metrics);
        int n = a.length();
        final int compareLine = 4;
        final int shiftLine = 5;
        final int insertLine = 8;

        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                int key = a.get(i);
                int j = i;
                while (j >= gap) {
                    a.compareVisual(j - gap, j, compareLine);
                    a.metrics.comparisons++;
                    if (a.get(j - gap) > key) {
                        a.setVisual(j, a.get(j - gap), shiftLine);
                        j -= gap;
                    } else {
                        break;
                    }
                }
                a.setVisual(j, key, insertLine);
            }
        }
        for (int k = 0; k < n; k++)
            a.markFinal(k);
    }

    public static void radixSort(int[] array, List<Operation> ops, SortMetrics metrics) {
        TrackedArray a = new TrackedArray(array, ops, metrics);
        if (a.length() == 0)
            return;

        int max = a.get(0);
        for (int i = 1; i < a.length(); i++) {
            a.metrics.comparisons++; // logical comparison
            if (a.get(i) > max)
                max = a.get(i);
        }

        for (int exp = 1; max / exp > 0; exp *= 10) {
            countingSortForRadix(a, exp);
        }
        for (int k = 0; k < a.length(); k++)
            a.markFinal(k);
    }

    private static void countingSortForRadix(TrackedArray a, int exp) {
        int n = a.length();
        a.allocateAux(n); // Output array
        int[] output = new int[n];
        int[] count = new int[10];
        final int overwriteLine = 7;

        for (int i = 0; i < n; i++)
            count[(a.get(i) / exp) % 10]++;
        for (int i = 1; i < 10; i++)
            count[i] += count[i - 1];

        for (int i = n - 1; i >= 0; i--) {
            int digit = (a.get(i) / exp) % 10;
            output[count[digit] - 1] = a.get(i);
            a.writeAux(count[digit] - 1, output[count[digit] - 1]); // auxiliary array write
            count[digit]--;
        }

        for (int i = 0; i < n; i++) {
            a.setVisual(i, output[i], overwriteLine); // writes array implicitly
        }
        a.clearAux(n);
    }
}

/*
 * ---------------------------- Sort Stats & Analysis
 * ----------------------------
 */
class SortMetrics {
    public long comparisons = 0;
    public long swaps = 0;
    public long arrayReads = 0;
    public long arrayWrites = 0;
    public long currentAuxSpace = 0;
    public long peakAuxSpace = 0;
}

class SortStats {
    public final String algorithmName;
    public final long timeNanos;
    public final long memoryBytes;
    public final String timeComplexity;
    public final String spaceComplexity;
    public final long swaps;
    public final long writes;
    public final long reads;
    public final long comparisons;
    public final long peakAuxElements;
    public final int arraySize;

    public SortStats(String algorithmName, long timeNanos, long memoryBytes, String timeComplexity,
            String spaceComplexity, long swaps, long writes, long reads, long comparisons, long peakAuxElements,
            int arraySize) {
        this.algorithmName = algorithmName;
        this.timeNanos = timeNanos;
        this.memoryBytes = memoryBytes;
        this.timeComplexity = timeComplexity;
        this.spaceComplexity = spaceComplexity;
        this.swaps = swaps;
        this.writes = writes;
        this.reads = reads;
        this.comparisons = comparisons;
        this.peakAuxElements = peakAuxElements;
        this.arraySize = arraySize;
    }
}

class SortAnalysisDialog extends JDialog {
    public SortAnalysisDialog(JFrame owner, List<SortStats> history) {
        super(owner, "Sort Analysis", false);
        setSize(900, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel topContainer = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Array Size:"));
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(1000, 10, 1000000, 100));
        sizeSpinner.setToolTipText("Set the size of the array to be sorted in the analysis");
        topPanel.add(sizeSpinner);

        topPanel.add(new JLabel("Distribution:"));
        String[] distOptions = { "Random", "Nearly Sorted", "Reversed", "Few Unique", "Gaussian" };
        JComboBox<String> distributionCombo = new JComboBox<>(distOptions);
        distributionCombo.setToolTipText("Select the data distribution pattern for the array");
        topPanel.add(distributionCombo);

        JButton autoRunBtn = new JButton("▶ Run Auto Analysis");
        autoRunBtn.setToolTipText("Run all algorithms on the specified dataset and record the results");
        JButton autoResearchBtn = new JButton("📈 Auto Research");
        autoResearchBtn.setToolTipText("Run 30 trials for array sizes 100 to 10,000 and calculate Mean/StdDev");
        JButton exportBtn = new JButton("💾 Export to CSV");
        exportBtn.setToolTipText("Save the analysis history below to a CSV file");
        JButton clearDataBtn = new JButton("✖ Clear Data");
        clearDataBtn.setToolTipText("Clear all previously recorded sort analysis data");

        topPanel.add(autoRunBtn);
        topPanel.add(autoResearchBtn);
        topPanel.add(exportBtn);
        topPanel.add(clearDataBtn);

        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        JLabel statusLabel = new JLabel("");
        progressPanel.add(progressBar);
        progressPanel.add(statusLabel);

        topContainer.add(topPanel, BorderLayout.NORTH);
        topContainer.add(progressPanel, BorderLayout.SOUTH);

        String[] columns = { "Algorithm", "Array Size", "Time (ms)", "Actual Memory Diff", "Peak Aux Elements",
                "Time Complexity", "Space Complexity", "Swaps", "Array Writes", "Array Reads", "Comparisons" };
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (SortStats s : history) {
            model.addRow(new Object[] {
                    s.algorithmName, s.arraySize,
                    String.format(java.util.Locale.US, "%.3f", s.timeNanos / 1_000_000.0),
                    (s.memoryBytes > 0 ? s.memoryBytes + " bytes" : "< 1 KB"),
                    s.peakAuxElements,
                    s.timeComplexity, s.spaceComplexity, s.swaps, s.writes, s.reads, s.comparisons
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        table.getTableHeader().setToolTipText("Click on a header to see what it means");
        table.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col >= 0) {
                    String colName = table.getColumnName(col);
                    String message = "";
                    switch (colName) {
                        case "Algorithm":
                            message = "<html><body style='width: 300px; padding: 10px;'>"
                                    + "<h2>Algorithm</h2>"
                                    + "<hr>"
                                    + "<p>The name of the sorting algorithm used to sort the array, along with the distribution pattern of the input data (e.g., Random, Nearly Sorted).</p>"
                                    + "<p>Different algorithms perform vastly differently depending on the initial state of the data.</p>"
                                    + "</body></html>";
                            break;
                        case "Array Size":
                            message = "<html><body style='width: 300px; padding: 10px;'>"
                                    + "<h2>Array Size (<i>N</i>)</h2>"
                                    + "<hr>"
                                    + "<p>The total number of elements in the array that was sorted.</p>"
                                    + "<p>This is the <b><i>N</i></b> value referred to in Time and Space Complexity (e.g., O(N²)). Larger sizes will exponentially increase the time taken for slower algorithms.</p>"
                                    + "</body></html>";
                            break;
                        case "Time (ms)":
                            message = "<html><body style='width: 300px; padding: 10px;'>"
                                    + "<h2>Time (ms)</h2>"
                                    + "<hr>"
                                    + "<p>The actual CPU time taken to execute the sorting algorithm in milliseconds.</p>"
                                    + "<p><b>Note:</b> This only measures the raw sorting execution, not the UI rendering time. It relies on the system's high-resolution timer (`System.nanoTime()`), but can still be affected by background system processes and warm-up (JIT compilation bounds).</p>"
                                    + "</body></html>";
                            break;
                        case "Actual Memory Diff":
                            message = "<html><body style='width: 300px; padding: 10px;'>"
                                    + "<h2>Actual Memory Difference</h2>"
                                    + "<hr>"
                                    + "<p>The difference in the JVM's memory usage taken immediately before and after the algorithm executes.</p>"
                                    + "<p><b>Important:</b> This is an approximation. The Java Garbage Collector (GC) runs non-deterministically, meaning objects might be cleaned up during the sort, skewing these numbers. Values less than 1KB are usually negligible internal overhead.</p>"
                                    + "</body></html>";
                            break;
                        case "Peak Aux Elements":
                            message = "<html><body style='width: 300px; padding: 10px;'>"
                                    + "<h2>Peak Auxiliary Elements</h2>"
                                    + "<hr>"
                                    + "<p>The maximum size of any additional array(s) created by the algorithm during execution.</p>"
                                    + "<ul>"
                                    + "<li><b>In-place sorts</b> (like Bubble, Quicksort) will show <b>0</b> here, as they sort within the original array boundaries.</li>"
                                    + "<li><b>Out-of-place sorts</b> (like Merge Sort) typically allocate additional array space proportional to the input size (e.g., N), showing higher values here.</li>"
                                    + "</ul>"
                                    + "</body></html>";
                            break;
                        case "Time Complexity":
                            message = "<html><body style='width: 300px; padding: 10px;'>"
                                    + "<h2>Time Complexity (Average Case)</h2>"
                                    + "<hr>"
                                    + "<p>The theoretical, mathematical description of how the algorithm's runtime grows as the array size (N) increases.</p>"
                                    + "<ul>"
                                    + "<li><b>O(N²)</b>: Slow. 10x more items = 100x more time. (e.g., Bubble Sort)</li>"
                                    + "<li><b>O(N log N)</b>: Fast and standard for general sorts. (e.g., Quick Sort, Merge Sort)</li>"
                                    + "<li><b>O(N)</b>: Extremely fast, usually limited by constraints. (e.g., Radix Sort)</li>"
                                    + "</ul>"
                                    + "</body></html>";
                            break;
                        case "Space Complexity":
                            message = "<html><body style='width: 300px; padding: 10px;'>"
                                    + "<h2>Space Complexity (Worst Case)</h2>"
                                    + "<hr>"
                                    + "<p>The theoretical amount of additional memory the algorithm requires, expressed mathematically.</p>"
                                    + "<ul>"
                                    + "<li><b>O(1)</b>: Minimal extra memory needed. (In-place)</li>"
                                    + "<li><b>O(log N)</b>: A small amount of extra memory, usually stack frames for recursion (e.g., Quicksort).</li>"
                                    + "<li><b>O(N)</b>: Requires copying the array, effectively doubling memory usage. (e.g., Merge sort)</li>"
                                    + "</ul>"
                                    + "</body></html>";
                            break;
                        case "Swaps":
                            message = "<html><body style='width: 300px; padding: 10px;'>"
                                    + "<h2>Total Swaps</h2>"
                                    + "<hr>"
                                    + "<p>The total number of times two elements in the array directly exchanged positions.</p>"
                                    + "<p>Algorithms like Selection Sort minimize swaps, while Bubble Sort relies heavily on them.</p>"
                                    + "</body></html>";
                            break;
                        case "Array Writes":
                            message = "<html><body style='width: 300px; padding: 10px;'>"
                                    + "<h2>Array Writes</h2>"
                                    + "<hr>"
                                    + "<p>The total number of times an assignment was made to modify the main array's contents.</p>"
                                    + "<p><b>Context:</b> A single 'Swap' operation usually entails writing to the array twice (plus a temporary variable).</p>"
                                    + "</body></html>";
                            break;
                        case "Array Reads":
                            message = "<html><body style='width: 300px; padding: 10px;'>"
                                    + "<h2>Array Reads</h2>"
                                    + "<hr>"
                                    + "<p>The total number of times an element was fetched (accessed) from the main array.</p>"
                                    + "<p>This number is driven up by comparisons and copying operations.</p>"
                                    + "</body></html>";
                            break;
                        case "Comparisons":
                            message = "<html><body style='width: 300px; padding: 10px;'>"
                                    + "<h2>Comparisons</h2>"
                                    + "<hr>"
                                    + "<p>The total number of times the algorithm compared two elements against each other (e.g., asking 'Is A greater than B?').</p>"
                                    + "<p>This is a primary metric for determining the efficiency and Time Complexity class of non-linear sorting algorithms.</p>"
                                    + "</body></html>";
                            break;
                        default:
                            return;
                    }
                    javax.swing.JOptionPane.showMessageDialog(SortAnalysisDialog.this,
                            message, "Explanation: " + colName, javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        add(topContainer, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        clearDataBtn.addActionListener(e -> {
            history.clear();
            model.setRowCount(0);
            statusLabel.setText("Analysis data cleared.");
        });

        autoRunBtn.addActionListener(e -> {
            int size = (Integer) sizeSpinner.getValue();
            String distribution = (String) distributionCombo.getSelectedItem();
            autoRunBtn.setEnabled(false);
            autoResearchBtn.setEnabled(false);

            progressBar.setValue(0);
            progressBar.setVisible(true);
            statusLabel.setText("Preparing...");

            long startTimeMillis = System.currentTimeMillis();
            int totalAlgorithms = VisualFrame.ALGORITHMS.length;
            int[] completed = { 0 };


            javax.swing.Timer timer = new javax.swing.Timer(1000, evt -> {
                long elapsedSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000;
                int c = completed[0];
                if (c == 0) {
                    statusLabel
                            .setText(String.format("Running analysis... (Elapsed: %ds)", elapsedSeconds));
                } else {
                    long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
                    long etaMillis = (long) ((elapsedMillis / (double) c) * (totalAlgorithms - c));
                    statusLabel.setText(String.format("Running... %d/%d (Elapsed: %ds | ETA: %ds)", c, totalAlgorithms,
                            elapsedSeconds, etaMillis / 1000));
                }
            });
            timer.start();

            Thread t = new Thread(() -> {
                int[] arr = new int[size];
                java.util.Random rnd = new java.util.Random();

                switch (distribution) {
                    case "Nearly Sorted":
                        for (int i = 0; i < size; i++)
                            arr[i] = i;
                        for (int i = 0; i < size * 0.05; i++) { // 5% noise
                            int i1 = rnd.nextInt(size);
                            int i2 = rnd.nextInt(size);
                            int temp = arr[i1];
                            arr[i1] = arr[i2];
                            arr[i2] = temp;
                        }
                        break;
                    case "Reversed":
                        for (int i = 0; i < size; i++)
                            arr[i] = size - i;
                        break;
                    case "Few Unique":
                        for (int i = 0; i < size; i++)
                            arr[i] = (rnd.nextInt(5) + 1) * (size / 5);
                        break;
                    case "Gaussian":
                        for (int i = 0; i < size; i++) {
                            int val = (int) (rnd.nextGaussian() * (size / 4) + (size / 2));
                            arr[i] = Math.max(1, Math.min(size, val));
                        }
                        break;
                    case "Random":
                    default:
                        for (int i = 0; i < size; i++)
                            arr[i] = rnd.nextInt(400) + 5;
                        break;
                }

                // Single-threaded: eliminates CPU contention noise between concurrent benchmarks
                java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors
                        .newSingleThreadExecutor();

                for (AlgorithmDefinition alg : VisualFrame.ALGORITHMS) {
                    executor.submit(() -> {
                        // JMH-style warmup: 3 iterations with raw algorithm to trigger JIT
                        for (int w = 0; w < 3; w++) {
                            int[] warmup = arr.clone();
                            RawBenchmarkRunner.sortRaw(alg.getDisplayName(), warmup);
                        }

                        // Timed run with raw algorithm (zero tracking overhead)
                        int[] timedCopy = arr.clone();
                        System.gc(); // Hint GC
                        long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                        long startNanos = System.nanoTime();
                        RawBenchmarkRunner.sortRaw(alg.getDisplayName(), timedCopy);
                        long algorithmTimeNanos = System.nanoTime() - startNanos;
                        // DCE fence: prevent JVM from eliminating sort as dead code
                        if (timedCopy[0] == Integer.MIN_VALUE) throw new AssertionError("DCE");
                        long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                        long memUsed = Math.max(0, memAfter - memBefore);

                        // Metrics collection (separate run with tracked algorithm)
                        int[] metricsCopy = arr.clone();
                        SortMetrics metrics = new SortMetrics();
                        alg.sort(metricsCopy, null, metrics);

                        String algNameWithDist = alg.getDisplayName() + " (" + distribution + ")";
                        SortStats stats = new SortStats(algNameWithDist, algorithmTimeNanos, memUsed,
                                alg.getTimeComplexity(), alg.getSpaceComplexity(), metrics.swaps, metrics.arrayWrites,
                                metrics.arrayReads, metrics.comparisons, metrics.peakAuxSpace, size);

                        SwingUtilities.invokeLater(() -> {
                            completed[0]++;
                            int percent = (int) ((completed[0] * 100.0) / totalAlgorithms);

                            history.add(stats);
                            model.addRow(new Object[] {
                                    stats.algorithmName, stats.arraySize,
                                    String.format(java.util.Locale.US, "%.3f", stats.timeNanos / 1_000_000.0),
                                    (stats.memoryBytes > 0 ? stats.memoryBytes + " bytes" : "< 1 KB"),
                                    stats.peakAuxElements,
                                    stats.timeComplexity, stats.spaceComplexity, stats.swaps, stats.writes, stats.reads,
                                    stats.comparisons
                            });
                            progressBar.setValue(percent);

                            if (completed[0] == totalAlgorithms) {
                                timer.stop();
                                autoRunBtn.setEnabled(true);
                                autoResearchBtn.setEnabled(true);
                                progressBar.setVisible(false);
                                statusLabel.setText("Analysis complete.");
                            }
                        });
                    });
                }
                executor.shutdown();
            });
            t.start();
        });

        autoResearchBtn.addActionListener(e -> {
            String distribution = (String) distributionCombo.getSelectedItem();
            autoRunBtn.setEnabled(false);
            autoResearchBtn.setEnabled(false);

            progressBar.setValue(0);
            progressBar.setVisible(true);
            statusLabel.setText("Preparing Research...");

            long startTimeMillis = System.currentTimeMillis();
            int numSizes = 100;
            int trialsPerSize = 30;
            int totalAlgorithms = VisualFrame.ALGORITHMS.length;
            int totalTasks = totalAlgorithms * numSizes * trialsPerSize;
            int[] completed = { 0 };


            javax.swing.Timer timer = new javax.swing.Timer(1000, evt -> {
                long elapsedSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000;
                int c = completed[0];
                if (c == 0) {
                    statusLabel.setText(
                            String.format("Researching... (Elapsed: %ds)", elapsedSeconds));
                } else {
                    long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
                    long etaMillis = (long) ((elapsedMillis / (double) c) * (totalTasks - c));
                    statusLabel.setText(String.format("Researching... %d/%d (Elapsed: %ds | ETA: %ds)", c, totalTasks,
                            elapsedSeconds, etaMillis / 1000));
                }
            });
            timer.start();

            Thread t = new Thread(() -> {
                // Single-threaded: eliminates CPU contention noise between concurrent benchmarks
                java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors
                        .newSingleThreadExecutor();
                java.util.Random rnd = new java.util.Random();

                for (int sz = 100; sz <= 10000; sz += 100) {
                    final int currentSize = sz;
                    for (AlgorithmDefinition alg : VisualFrame.ALGORITHMS) {
                        final String algNameWithDist = alg.getDisplayName() + " (" + distribution + ")";

                        executor.submit(() -> {
                            List<SortStats> trialStats = new ArrayList<>();
                            // JMH-style warmup before measurement trials
                            for (int w = 0; w < 3; w++) {
                                int[] warmup = new int[currentSize];
                                java.util.Random warmupRnd = new java.util.Random();
                                for (int i = 0; i < currentSize; i++) warmup[i] = warmupRnd.nextInt(400) + 5;
                                RawBenchmarkRunner.sortRaw(alg.getDisplayName(), warmup);
                            }
                            for (int trial = 0; trial < trialsPerSize; trial++) {
                                int[] arr = new int[currentSize];
                                switch (distribution) {
                                    case "Nearly Sorted":
                                        for (int i = 0; i < currentSize; i++)
                                            arr[i] = i;
                                        for (int i = 0; i < currentSize * 0.05; i++) {
                                            int i1 = rnd.nextInt(currentSize);
                                            int i2 = rnd.nextInt(currentSize);
                                            int temp = arr[i1];
                                            arr[i1] = arr[i2];
                                            arr[i2] = temp;
                                        }
                                        break;
                                    case "Reversed":
                                        for (int i = 0; i < currentSize; i++)
                                            arr[i] = currentSize - i;
                                        break;
                                    case "Few Unique":
                                        for (int i = 0; i < currentSize; i++)
                                            arr[i] = (rnd.nextInt(5) + 1) * (currentSize / 5);
                                        break;
                                    case "Gaussian":
                                        for (int i = 0; i < currentSize; i++) {
                                            int val = (int) (rnd.nextGaussian() * (currentSize / 4)
                                                    + (currentSize / 2));
                                            arr[i] = Math.max(1, Math.min(currentSize, val));
                                        }
                                        break;
                                    case "Random":
                                    default:
                                        for (int i = 0; i < currentSize; i++)
                                            arr[i] = rnd.nextInt(400) + 5;
                                        break;
                                }

                                // Timed run with raw algorithm (zero tracking overhead)
                                int[] timedCopy = arr.clone();

                                // Omitted System.gc() to prevent JVM stop-the-world stall over 2,400 trials.
                                long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                                long startNanos = System.nanoTime();
                                RawBenchmarkRunner.sortRaw(alg.getDisplayName(), timedCopy);
                                long algorithmTimeNanos = System.nanoTime() - startNanos;
                                // DCE fence: prevent JVM from eliminating sort as dead code
                                if (timedCopy[0] == Integer.MIN_VALUE) throw new AssertionError("DCE");
                                long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                                long memUsed = Math.max(0, memAfter - memBefore);

                                // Metrics collection (tracked algorithm)
                                int[] metricsCopy = arr.clone();
                                SortMetrics metrics = new SortMetrics();
                                alg.sort(metricsCopy, null, metrics);

                                SortStats stats = new SortStats(algNameWithDist, algorithmTimeNanos, memUsed,
                                        alg.getTimeComplexity(), alg.getSpaceComplexity(), metrics.swaps,
                                        metrics.arrayWrites, metrics.arrayReads, metrics.comparisons,
                                        metrics.peakAuxSpace, currentSize);
                                trialStats.add(stats);
                            }

                            SwingUtilities.invokeLater(() -> {
                                completed[0] += trialsPerSize;
                                int percent = (int) ((completed[0] * 100.0) / totalTasks);
                                progressBar.setValue(percent);
                            });

                            long sumTime = 0, sumMem = 0, sumComps = 0, sumSwaps = 0;
                            for (SortStats s : trialStats) {
                                sumTime += s.timeNanos;
                                sumMem += s.memoryBytes;
                                sumComps += s.comparisons;
                                sumSwaps += s.swaps;
                            }
                            double meanTime = (double) sumTime / trialsPerSize;
                            double meanMem = (double) sumMem / trialsPerSize;
                            double meanComps = (double) sumComps / trialsPerSize;
                            double meanSwaps = (double) sumSwaps / trialsPerSize;

                            double varTime = 0, varMem = 0, varComps = 0, varSwaps = 0;
                            for (SortStats s : trialStats) {
                                varTime += Math.pow(s.timeNanos - meanTime, 2);
                                varMem += Math.pow(s.memoryBytes - meanMem, 2);
                                varComps += Math.pow(s.comparisons - meanComps, 2);
                                varSwaps += Math.pow(s.swaps - meanSwaps, 2);
                            }
                            double stdDevTime = Math.sqrt(varTime / trialsPerSize);
                            double stdDevMem = Math.sqrt(varMem / trialsPerSize);
                            double stdDevComps = Math.sqrt(varComps / trialsPerSize);
                            double stdDevSwaps = Math.sqrt(varSwaps / trialsPerSize);

                            SortStats meanStats = new SortStats(algNameWithDist + " (Mean)", (long) meanTime,
                                    (long) meanMem, alg.getTimeComplexity(), alg.getSpaceComplexity(), (long) meanSwaps,
                                    0, 0, (long) meanComps, 0, currentSize);
                            SortStats stdDevStats = new SortStats(algNameWithDist + " (StdDev)", (long) stdDevTime,
                                    (long) stdDevMem, alg.getTimeComplexity(), alg.getSpaceComplexity(),
                                    (long) stdDevSwaps, 0, 0, (long) stdDevComps, 0, currentSize);

                            SwingUtilities.invokeLater(() -> {
                                for (SortStats s : trialStats) {
                                    history.add(s);
                                    model.addRow(new Object[] {
                                            s.algorithmName, s.arraySize,
                                            String.format(java.util.Locale.US, "%.3f", s.timeNanos / 1_000_000.0),
                                            (s.memoryBytes > 0 ? s.memoryBytes + " bytes" : "< 1 KB"),
                                            s.peakAuxElements,
                                            s.timeComplexity, s.spaceComplexity, s.swaps, s.writes, s.reads,
                                            s.comparisons
                                    });
                                }
                                history.add(meanStats);
                                model.addRow(new Object[] {
                                        meanStats.algorithmName, meanStats.arraySize,
                                        String.format(java.util.Locale.US, "%.3f", meanStats.timeNanos / 1_000_000.0),
                                        (meanStats.memoryBytes > 0 ? meanStats.memoryBytes + " bytes" : "0 bytes"),
                                        "-", meanStats.timeComplexity, meanStats.spaceComplexity, meanStats.swaps, "-",
                                        "-", meanStats.comparisons
                                });
                                history.add(stdDevStats);
                                model.addRow(new Object[] {
                                        stdDevStats.algorithmName, stdDevStats.arraySize,
                                        String.format(java.util.Locale.US, "%.3f", stdDevStats.timeNanos / 1_000_000.0),
                                        (stdDevStats.memoryBytes > 0 ? stdDevStats.memoryBytes + " bytes" : "0 bytes"),
                                        "-", stdDevStats.timeComplexity, stdDevStats.spaceComplexity, stdDevStats.swaps,
                                        "-", "-", stdDevStats.comparisons
                                });

                                if (completed[0] == totalTasks) {
                                    timer.stop();
                                    autoRunBtn.setEnabled(true);
                                    autoResearchBtn.setEnabled(true);
                                    progressBar.setVisible(false);
                                    statusLabel.setText("Research complete.");
                                }
                            });
                        });
                    }
                }
                executor.shutdown();
            });
            t.start();
        });

        exportBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Export as CSV");
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new java.io.File(file.getParentFile(), file.getName() + ".csv");
                }
                try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                    writer.println(
                            "Algorithm,Array Size,Time (ms),Actual Memory Diff,Peak Aux Elements,Time Complexity,Space Complexity,Swaps,Array Writes,Array Reads,Comparisons");
                    for (SortStats s : history) {
                        writer.printf(java.util.Locale.US, "%s,%d,%.3f,%d,%d,%s,%s,%d,%d,%d,%d%n",
                                s.algorithmName, s.arraySize, (s.timeNanos / 1_000_000.0),
                                s.memoryBytes, s.peakAuxElements, s.timeComplexity, s.spaceComplexity, s.swaps,
                                s.writes, s.reads, s.comparisons);
                    }
                    JOptionPane.showMessageDialog(this, "Export complete!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}

/* ---------------------------- Comparison Frame ---------------------------- */
class CompareFrame extends JFrame {
    private final VisualPanel panel1 = new VisualPanel();
    private final VisualPanel panel2 = new VisualPanel();
    private final JComboBox<AlgorithmDefinition> algoSelect1;
    private final JComboBox<AlgorithmDefinition> algoSelect2;
    private final JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(150, 10, 800, 10));
    private final JButton startRaceBtn = new JButton("🏁 Start Race");
    private final JCheckBox numberToggle = new JCheckBox("Show Numbers", true);
    private final JLabel statusLabel = new JLabel("Status: Ready to Race");

    private final JLabel statsLabel1 = new JLabel("<html><br><br><br><br></html>", SwingConstants.CENTER);
    private final JLabel statsLabel2 = new JLabel("<html><br><br><br><br></html>", SwingConstants.CENTER);

    private OperationPlayer player1;
    private OperationPlayer player2;

    public CompareFrame(AlgorithmDefinition[] algorithms) {
        super("Algorithm Comparison");
        setSize(1200, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        algoSelect1 = new JComboBox<>(algorithms);
        algoSelect2 = new JComboBox<>(algorithms);
        if (algorithms.length > 1) {
            algoSelect2.setSelectedIndex(1);
        }

        statsLabel1.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        statsLabel2.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        statsLabel1.setFont(new Font("Arial", Font.PLAIN, 13));
        statsLabel2.setFont(new Font("Arial", Font.PLAIN, 13));

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Left Algorithm:"));
        topPanel.add(algoSelect1);
        topPanel.add(Box.createHorizontalStrut(30));
        topPanel.add(new JLabel("Size:"));
        topPanel.add(sizeSpinner);
        topPanel.add(Box.createHorizontalStrut(30));
        topPanel.add(startRaceBtn);
        topPanel.add(Box.createHorizontalStrut(50));
        topPanel.add(new JLabel("Right Algorithm:"));
        topPanel.add(algoSelect2);
        topPanel.add(Box.createHorizontalStrut(30));
        topPanel.add(numberToggle);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.add(new JLabel("Player 1", SwingConstants.CENTER), BorderLayout.NORTH);
        leftContainer.add(panel1, BorderLayout.CENTER);
        leftContainer.add(statsLabel1, BorderLayout.SOUTH);

        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(new JLabel("Player 2", SwingConstants.CENTER), BorderLayout.NORTH);
        rightContainer.add(panel2, BorderLayout.CENTER);
        rightContainer.add(statsLabel2, BorderLayout.SOUTH);

        centerPanel.add(leftContainer);
        centerPanel.add(rightContainer);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        numberToggle.addActionListener(e -> {
            boolean show = numberToggle.isSelected();
            panel1.setShowNumbers(show);
            panel2.setShowNumbers(show);
        });

        startRaceBtn.addActionListener(e -> runComparison());
    }

    private void runComparison() {
        if ((player1 != null && player1.isPlaying()) || (player2 != null && player2.isPlaying())) {
            return;
        }

        AlgorithmDefinition alg1 = (AlgorithmDefinition) algoSelect1.getSelectedItem();
        AlgorithmDefinition alg2 = (AlgorithmDefinition) algoSelect2.getSelectedItem();

        int size = (Integer) sizeSpinner.getValue();
        panel1.generateRandomArray(size);
        panel2.setCustomArray(panel1.getArrayCopy()); // Make them identical

        panel1.resetHighlights();
        panel2.resetHighlights();

        startRaceBtn.setEnabled(false);
        algoSelect1.setEnabled(false);
        algoSelect2.setEnabled(false);
        sizeSpinner.setEnabled(false);
        statusLabel.setText("Status: Racing " + alg1.getDisplayName() + " vs " + alg2.getDisplayName() + "...");
        statsLabel1.setText("<html><i>Calculations running...</i><br><br><br><br></html>");
        statsLabel2.setText("<html><i>Calculations running...</i><br><br><br><br></html>");

        new Thread(() -> {
            try {
                int[] arr1 = panel1.getArrayCopy();
                List<Operation> ops1 = new ArrayList<>();
                System.gc(); // Hint GC to run to get a better memory baseline
                long memBefore1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                long t1 = System.nanoTime();
                SortMetrics m1 = new SortMetrics();
                alg1.sort(arr1, ops1, m1);
                long time1 = System.nanoTime() - t1;
                long memAfter1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                long memUsed1 = Math.max(0, memAfter1 - memBefore1);

                final long swaps1 = m1.swaps;
                final long writes1 = m1.arrayWrites;
                final long reads1 = m1.arrayReads;
                final long comps1 = m1.comparisons;
                final long peakAux1 = m1.peakAuxSpace;

                int[] arr2 = panel2.getArrayCopy();
                List<Operation> ops2 = new ArrayList<>();
                System.gc(); // Hint GC
                long memBefore2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                long t2 = System.nanoTime();
                SortMetrics m2 = new SortMetrics();
                alg2.sort(arr2, ops2, m2);
                long time2 = System.nanoTime() - t2;
                long memAfter2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                long memUsed2 = Math.max(0, memAfter2 - memBefore2);

                final long swaps2 = m2.swaps;
                final long writes2 = m2.arrayWrites;
                final long reads2 = m2.arrayReads;
                final long comps2 = m2.comparisons;
                final long peakAux2 = m2.peakAuxSpace;

                SwingUtilities.invokeLater(() -> {
                    statsLabel1.setText("<html><i>Visualizing...</i><br><br><br><br></html>");
                    statsLabel2.setText("<html><i>Visualizing...</i><br><br><br><br></html>");

                    AtomicBoolean p1Done = new AtomicBoolean(false);
                    AtomicBoolean p2Done = new AtomicBoolean(false);

                    Runnable checkDone = () -> {
                        if (p1Done.get() && p2Done.get()) {
                            startRaceBtn.setEnabled(true);
                            algoSelect1.setEnabled(true);
                            algoSelect2.setEnabled(true);
                            sizeSpinner.setEnabled(true);
                            statusLabel.setText("Status: Race Finished!");
                        }
                    };

                    player1 = new OperationPlayer(ops1, panel1, 5, new JLabel(), time1, null, () -> {
                        p1Done.set(true);
                        statsLabel1.setText(String.format(
                                "<html><b>Algorithm:</b> %s<br><b>Time:</b> %.3f ms<br><b>Memory Diff:</b> %s<br><b>Peak Aux:</b> %d Elements<br><b>Time Complexity:</b> %s &nbsp;&nbsp; <b>Space Complexity:</b> %s<br><b>Swaps:</b> %d &nbsp;&nbsp; <b>Comparisons:</b> %d<br><b>Array Reads:</b> %d &nbsp;&nbsp; <b>Array Writes:</b> %d</html>",
                                alg1.getDisplayName(), time1 / 1_000_000.0,
                                (memUsed1 > 0 ? memUsed1 + " bytes" : "< 1 KB"), peakAux1, alg1.getTimeComplexity(),
                                alg1.getSpaceComplexity(), swaps1, comps1, reads1, writes1));
                        checkDone.run();
                    });

                    player2 = new OperationPlayer(ops2, panel2, 5, new JLabel(), time2, null, () -> {
                        p2Done.set(true);
                        statsLabel2.setText(String.format(
                                "<html><b>Algorithm:</b> %s<br><b>Time:</b> %.3f ms<br><b>Memory Diff:</b> %s<br><b>Peak Aux:</b> %d Elements<br><b>Time Complexity:</b> %s &nbsp;&nbsp; <b>Space Complexity:</b> %s<br><b>Swaps:</b> %d &nbsp;&nbsp; <b>Comparisons:</b> %d<br><b>Array Reads:</b> %d &nbsp;&nbsp; <b>Array Writes:</b> %d</html>",
                                alg2.getDisplayName(), time2 / 1_000_000.0,
                                (memUsed2 > 0 ? memUsed2 + " bytes" : "< 1 KB"), peakAux2, alg2.getTimeComplexity(),
                                alg2.getSpaceComplexity(), swaps2, comps2, reads2, writes2));
                        checkDone.run();
                    });

                    player1.start();
                    player2.start();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    startRaceBtn.setEnabled(true);
                    algoSelect1.setEnabled(true);
                    algoSelect2.setEnabled(true);
                    sizeSpinner.setEnabled(true);
                    statusLabel.setText("Status: Error during comparison.");
                });
            }
        }, "Compare-Thread").start();
    }
}

class TrackedArray {
    private final int[] a;
    public final SortMetrics metrics;
    private final List<Operation> ops;

    public TrackedArray(int[] a, List<Operation> ops, SortMetrics metrics) {
        this.a = a;
        this.ops = ops;
        this.metrics = metrics;
    }

    public int getVisualCompare(int i, int j, int codeLine) {
        metrics.comparisons++;
        metrics.arrayReads += 2;
        if (ops != null)
            ops.add(Operation.compare(i, j, codeLine));
        return Integer.compare(a[i], a[j]);
    }

    public int get(int i) {
        metrics.arrayReads++;
        return a[i];
    }

    public void setVisual(int i, int val, int codeLine) {
        metrics.arrayWrites++;
        a[i] = val;
        if (ops != null)
            ops.add(Operation.overwrite(i, val, codeLine));
    }

    // silent set for temp buffers
    public void set(int i, int val) {
        metrics.arrayWrites++;
        a[i] = val;
    }

    public void swap(int i, int j, int codeLine) {
        metrics.swaps++;
        metrics.arrayReads += 2;
        metrics.arrayWrites += 2;
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
        if (ops != null)
            ops.add(Operation.swap(i, j, codeLine));
    }

    public int length() {
        return a.length;
    }

    public void compareVisual(int i, int j, int codeLine) {
        if (ops != null)
            ops.add(Operation.compare(i, j, codeLine));
    }

    public void markFinal(int i) {
        if (ops != null)
            ops.add(Operation.markFinal(i));
    }

    public void allocateAux(int size) {
        metrics.currentAuxSpace += size;
        if (metrics.currentAuxSpace > metrics.peakAuxSpace) {
            metrics.peakAuxSpace = metrics.currentAuxSpace;
        }
        if (ops != null)
            ops.add(Operation.auxAllocate(size));
    }

    public void writeAux(int i, int val) {
        metrics.arrayWrites++;
        if (ops != null)
            ops.add(Operation.auxWrite(i, val));
    }

    public void clearAux(int size) {
        metrics.currentAuxSpace -= size;
        if (ops != null)
            ops.add(Operation.auxClear());
    }
}

/* ---------------------------- Raw Benchmark Runner ---------------------------- */
class RawBenchmarkRunner {
    /**
     * Routes to the zero-overhead raw sorting algorithm by display name.
     * These are the same pure implementations used in the JMH benchmark module.
     */
    static void sortRaw(String algorithmName, int[] a) {
        // Strip any distribution suffix: "Bubble Sort (Random)" -> "Bubble Sort"
        String base = algorithmName.contains("(") ? algorithmName.substring(0, algorithmName.indexOf('(')).trim() : algorithmName;
        switch (base) {
            case "Bubble Sort":    RawSortingAlgorithms.bubbleSort(a);    break;
            case "Selection Sort": RawSortingAlgorithms.selectionSort(a); break;
            case "Insertion Sort": RawSortingAlgorithms.insertionSort(a); break;
            case "Merge Sort":    RawSortingAlgorithms.mergeSort(a);     break;
            case "Quick Sort":    RawSortingAlgorithms.quickSort(a);     break;
            case "Heap Sort":     RawSortingAlgorithms.heapSort(a);      break;
            case "Shell Sort":    RawSortingAlgorithms.shellSort(a);     break;
            case "Radix Sort":    RawSortingAlgorithms.radixSort(a);     break;
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }
    }
}

/* ---------------------------- Raw Sorting Algorithms (JMH-quality, zero overhead) ---------------------------- */
/**
 * Pure sorting algorithm implementations for accurate benchmarking.
 * These are zero-overhead ports — no TrackedArray, no Operation recording,
 * no SortMetrics tracking — ensuring measurements capture only the sorting work.
 * Ported from jmh-benchmark/src/main/java/sorting/benchmark/RawSortingAlgorithms.java
 */
final class RawSortingAlgorithms {

    private RawSortingAlgorithms() { /* utility class */ }

    // ───────────────────────── Bubble Sort ─────────────────────────

    public static void bubbleSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    int tmp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = tmp;
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }

    // ───────────────────────── Selection Sort ─────────────────────────

    public static void selectionSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (a[j] < a[minIdx]) {
                    minIdx = j;
                }
            }
            if (minIdx != i) {
                int tmp = a[i];
                a[i] = a[minIdx];
                a[minIdx] = tmp;
            }
        }
    }

    // ───────────────────────── Insertion Sort ─────────────────────────

    public static void insertionSort(int[] a) {
        int n = a.length;
        for (int i = 1; i < n; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    // ───────────────────────── Merge Sort ─────────────────────────

    public static void mergeSort(int[] a) {
        mergeSortRec(a, 0, a.length - 1);
    }

    private static void mergeSortRec(int[] a, int l, int r) {
        if (l >= r) return;
        int m = (l + r) / 2;
        mergeSortRec(a, l, m);
        mergeSortRec(a, m + 1, r);
        merge(a, l, m, r);
    }

    private static void merge(int[] a, int l, int m, int r) {
        int[] tmp = new int[r - l + 1];
        int i = l, j = m + 1, k = 0;
        while (i <= m && j <= r) {
            if (a[i] <= a[j]) {
                tmp[k++] = a[i++];
            } else {
                tmp[k++] = a[j++];
            }
        }
        while (i <= m) tmp[k++] = a[i++];
        while (j <= r) tmp[k++] = a[j++];
        System.arraycopy(tmp, 0, a, l, tmp.length);
    }

    // ───────────────────────── Quick Sort ─────────────────────────

    public static void quickSort(int[] a) {
        quickSortRec(a, 0, a.length - 1);
    }

    private static void quickSortRec(int[] a, int low, int high) {
        if (low < high) {
            int p = partition(a, low, high);
            quickSortRec(a, low, p - 1);
            quickSortRec(a, p + 1, high);
        }
    }

    private static int partition(int[] a, int low, int high) {
        int pivot = a[high];
        int i = low;
        for (int j = low; j < high; j++) {
            if (a[j] < pivot) {
                int tmp = a[i];
                a[i] = a[j];
                a[j] = tmp;
                i++;
            }
        }
        int tmp = a[i];
        a[i] = a[high];
        a[high] = tmp;
        return i;
    }

    // ───────────────────────── Heap Sort ─────────────────────────

    public static void heapSort(int[] a) {
        int n = a.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(a, n, i);
        }
        for (int i = n - 1; i >= 0; i--) {
            int tmp = a[0];
            a[0] = a[i];
            a[i] = tmp;
            heapify(a, i, 0);
        }
    }

    private static void heapify(int[] a, int n, int i) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        if (l < n && a[l] > a[largest]) largest = l;
        if (r < n && a[r] > a[largest]) largest = r;
        if (largest != i) {
            int tmp = a[i];
            a[i] = a[largest];
            a[largest] = tmp;
            heapify(a, n, largest);
        }
    }

    // ───────────────────────── Shell Sort ─────────────────────────

    public static void shellSort(int[] a) {
        int n = a.length;
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                int key = a[i];
                int j = i;
                while (j >= gap && a[j - gap] > key) {
                    a[j] = a[j - gap];
                    j -= gap;
                }
                a[j] = key;
            }
        }
    }

    // ───────────────────────── Radix Sort (LSD, base 10) ─────────────────────────

    public static void radixSort(int[] a) {
        if (a.length == 0) return;
        int max = a[0];
        for (int i = 1; i < a.length; i++) {
            if (a[i] > max) max = a[i];
        }
        for (int exp = 1; max / exp > 0; exp *= 10) {
            countingSortByDigit(a, exp);
        }
    }

    private static void countingSortByDigit(int[] a, int exp) {
        int n = a.length;
        int[] output = new int[n];
        int[] count = new int[10];

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
        System.arraycopy(output, 0, a, 0, n);
    }
}
