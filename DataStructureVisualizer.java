import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

/**
 * DataStructureVisualizer.java
 * Interactive visualizer for data structures: Stack, Queue, LinkedList,
 * Circular LinkedList, Doubly LinkedList.
 *
 * Compile:
 * javac DataStructureVisualizer.java
 * Run:
 * java DataStructureVisualizer
 *
 * Uses: Java 8+
 */

public class DataStructureVisualizer {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createFrame().setVisible(true));
    }

    /**
     * Factory method used by the hub launcher.
     * This keeps package-private frame construction inside the declaring source
     * file so source-file launches can access it safely.
     */
    public static JFrame createFrame() {
        return new DSVisualizerFrame();
    }
}

    class NodeVisualInfo {
        final String nodeId;
        final String structureType;
        final int index;
        final int value;
        final Rectangle bounds;
        final String nextReference;
        final String previousReference;
        final String note;

        NodeVisualInfo(String nodeId,
            String structureType,
            int index,
            int value,
            Rectangle bounds,
            String nextReference,
            String previousReference,
            String note) {
        this.nodeId = nodeId;
        this.structureType = structureType;
        this.index = index;
        this.value = value;
        this.bounds = bounds;
        this.nextReference = nextReference;
        this.previousReference = previousReference;
        this.note = note;
        }

        String toInspectorText() {
        StringBuilder builder = new StringBuilder();
        builder.append("Structure: ").append(structureType).append('\n');
        builder.append("Index: ").append(index).append('\n');
        builder.append("Value: ").append(value).append('\n');
        builder.append("Node ID: ").append(nodeId).append('\n');
        builder.append("Next Ref: ").append(nextReference == null ? "n/a" : nextReference).append('\n');
        builder.append("Previous Ref: ").append(previousReference == null ? "n/a" : previousReference).append('\n');
        if (note != null && !note.isEmpty()) {
            builder.append("Details: ").append(note).append('\n');
        }
        builder.append("Bounds: ").append(bounds.x)
            .append(',')
            .append(bounds.y)
            .append("  ")
            .append(bounds.width)
            .append('x')
            .append(bounds.height);
        return builder.toString();
        }
    }

class DSOperationCode {
    private final String displayName;
    private final String[] codeLines;

    DSOperationCode(String displayName, String... codeLines) {
        this.displayName = displayName;
        this.codeLines = codeLines;
    }

    String getDisplayName() {
        return displayName;
    }

    String[] getCodeLines() {
        return codeLines;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

class DSCodeViewerDialog extends JDialog {
    private final DefaultListModel<String> codeModel = new DefaultListModel<>();
    private final JList<String> codeList = new JList<>(codeModel);
    private DSOperationCode currentCode;
    private int highlightedLine = -1;

    DSCodeViewerDialog(JFrame owner) {
        super(owner, "Operation Code", false);
        setSize(520, 420);
        setLocationRelativeTo(owner);

        codeList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        codeList.setBackground(new Color(18, 18, 24));
        codeList.setForeground(new Color(240, 240, 245));
        codeList.setFixedCellHeight(26);
        codeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        codeList.setFocusable(false);
        codeList.setCellRenderer(new DSCodeLineRenderer());

        JScrollPane scrollPane = new JScrollPane(codeList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    void open(DSOperationCode code) {
        setCode(code);
        setVisible(true);
        toFront();
    }

    void setCode(DSOperationCode code) {
        if (code == null) {
            return;
        }
        if (code == currentCode && !codeModel.isEmpty()) {
            return;
        }

        currentCode = code;
        setTitle("Operation Code - " + code.getDisplayName());
        codeModel.clear();
        for (String line : code.getCodeLines()) {
            codeModel.addElement(line);
        }
        clearHighlight();
    }

    void clearHighlight() {
        highlightedLine = -1;
        codeList.clearSelection();
        codeList.repaint();
    }

    private final class DSCodeLineRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel lineNumberLabel = new JLabel();
        private final JLabel codeLabel = new JLabel();

        private DSCodeLineRenderer() {
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

    final class DSCodeRepository {
        private static final Map<String, List<DSOperationCode>> LIBRARY = createLibrary();

        private DSCodeRepository() {
        }

        public static List<DSOperationCode> getAlgorithms(String structureType) {
        return LIBRARY.getOrDefault(structureType, Collections.emptyList());
        }

        private static Map<String, List<DSOperationCode>> createLibrary() {
        Map<String, List<DSOperationCode>> library = new LinkedHashMap<>();

        library.put("Stack", Arrays.asList(
            code("Stack - Push",
                "void push(int value) {",
                "    if (isFull()) throw overflow;",
                "    top = top + 1;",
                "    items[top] = value;",
                "}"),
            code("Stack - Pop",
                "int pop() {",
                "    if (isEmpty()) throw underflow;",
                "    int value = items[top];",
                "    top = top - 1;",
                "    return value;",
                "}"),
            code("Stack - Traverse",
                "for (int i = top; i >= 0; i--) {",
                "    visit(items[i]);",
                "}")));

        library.put("Queue", Arrays.asList(
            code("Queue - Enqueue",
                "void enqueue(int value) {",
                "    if (isFull()) throw overflow;",
                "    rear = rear + 1;",
                "    items[rear] = value;",
                "}"),
            code("Queue - Dequeue",
                "int dequeue() {",
                "    if (isEmpty()) throw underflow;",
                "    int value = items[front];",
                "    front = front + 1;",
                "    return value;",
                "}"),
            code("Queue - Traverse",
                "for (int i = front; i <= rear; i++) {",
                "    visit(items[i]);",
                "}")));

        library.put("Dequeue", Arrays.asList(
            code("Deque - Add Front",
                "void addFront(int value) {",
                "    if (isFull()) throw overflow;",
                "    shiftRightByOne();",
                "    items[front] = value;",
                "}"),
            code("Deque - Add Rear",
                "void addRear(int value) {",
                "    if (isFull()) throw overflow;",
                "    rear = rear + 1;",
                "    items[rear] = value;",
                "}"),
            code("Deque - Remove Front",
                "int removeFront() {",
                "    if (isEmpty()) throw underflow;",
                "    return removeAt(front);",
                "}"),
            code("Deque - Remove Rear",
                "int removeRear() {",
                "    if (isEmpty()) throw underflow;",
                "    return removeAt(rear);",
                "}")));

        library.put("LinkedList", Arrays.asList(
            code("LinkedList - Insert At Index",
                "Node insertAtIndex(Node head, int index, int value) {",
                "    if (index == 0) return new Node(value, head);",
                "    Node prev = getNode(head, index - 1);",
                "    prev.next = new Node(value, prev.next);",
                "    return head;",
                "}"),
            code("LinkedList - Delete At Index",
                "Node deleteAtIndex(Node head, int index) {",
                "    if (index == 0) return head.next;",
                "    Node prev = getNode(head, index - 1);",
                "    prev.next = prev.next.next;",
                "    return head;",
                "}"),
            code("LinkedList - Traverse",
                "Node current = head;",
                "while (current != null) {",
                "    visit(current.data);",
                "    current = current.next;",
                "}")));

        library.put("Circular LinkedList", Arrays.asList(
            code("Circular List - Insert",
                "Node insert(Node tail, int index, int value) {",
                "    Node node = new Node(value);",
                "    linkIntoCircle(tail, index, node);",
                "    return updateTailIfNeeded(tail, node, index);",
                "}"),
            code("Circular List - Delete",
                "Node delete(Node tail, int index) {",
                "    if (tail == null) throw underflow;",
                "    unlinkTargetNode(tail, index);",
                "    return updateTailAfterDelete(tail, index);",
                "}"),
            code("Circular List - Traverse",
                "Node current = head;",
                "do {",
                "    visit(current.data);",
                "    current = current.next;",
                "} while (current != head);")));

        library.put("Doubly LinkedList", Arrays.asList(
            code("Doubly LinkedList - Insert",
                "Node insert(Node head, int index, int value) {",
                "    Node node = new Node(value);",
                "    Node current = getNode(head, index);",
                "    node.prev = current.prev;",
                "    node.next = current;",
                "    relinkNeighbors(node);",
                "    return updateHeadIfNeeded(head, index, node);",
                "}"),
            code("Doubly LinkedList - Delete",
                "Node delete(Node head, int index) {",
                "    Node target = getNode(head, index);",
                "    if (target.prev != null) target.prev.next = target.next;",
                "    if (target.next != null) target.next.prev = target.prev;",
                "    return index == 0 ? target.next : head;",
                "}"),
            code("Doubly LinkedList - Traverse",
                "for (Node current = head; current != null; current = current.next) {",
                "    visit(current.data);",
                "}")));

        return library;
        }

        private static DSOperationCode code(String title, String... lines) {
        return new DSOperationCode(title, lines);
        }
    }

/* ============================== Main Frame ============================== */
class DSVisualizerFrame extends JFrame {
    private final JComboBox<String> dsSelect = new JComboBox<>(
            new String[] { "Stack", "Queue", "Dequeue", "LinkedList", "Circular LinkedList", "Doubly LinkedList" });
    private final JTextField valueInput = new JTextField(6);
    private final JTextField sizeInput = new JTextField(3);
    private final JButton pushPushBtn = new JButton("Push/Add");
    private final JButton popRemoveBtn = new JButton("Pop/Remove");
    private final JButton clearBtn = new JButton("Clear");
    private final JButton randomBtn = new JButton("Random Fill");
    private final JButton insertBeginBtn = new JButton("Insert at Begin");
    private final JButton insertMiddleBtn = new JButton("Insert at Index");
    private final JButton insertEndBtn = new JButton("Insert at End");
    private final JButton deleteBeginBtn = new JButton("Delete Begin");
    private final JButton deleteMiddleBtn = new JButton("Delete at Index");
    private final JButton deleteEndBtn = new JButton("Delete End");
    private final JButton searchBtn = new JButton("Search");
    private final JButton viewCodeBtn = new JButton("View Code");
    // Dequeue specific buttons
    private final JButton addFrontBtn = new JButton("Add Front");
    private final JButton addRearBtn = new JButton("Add Rear");
    private final JButton removeFrontBtn = new JButton("Remove Front");
    private final JButton removeRearBtn = new JButton("Remove Rear");
    private final JLabel statusLabel = new JLabel("Status: Ready");
    private final DSVisualizerPanel visualPanel = new DSVisualizerPanel();
    private final JTextArea inspectorArea = new JTextArea();
    private final JLabel inspectorTitle = new JLabel("Node Inspector");
    private final DSCodeViewerDialog codeViewer;

    public DSVisualizerFrame() {
        super("Data Structure Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        codeViewer = new DSCodeViewerDialog(this);

        JScrollPane scrollPane = new JScrollPane(visualPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        add(buildInspectorPanel(), BorderLayout.EAST);
        add(buildControlPanel(), BorderLayout.SOUTH);

        visualPanel.setNodeSelectionListener(this::updateInspector);

        visualPanel.switchDataStructure("Stack");
        updateInsertButtonsVisibility("Stack");
        updateInspector(null);

        dsSelect.addActionListener(e -> {
            String selectedDS = (String) dsSelect.getSelectedItem();
            visualPanel.switchDataStructure(selectedDS);
            updateInsertButtonsVisibility(selectedDS);
            setStatus(visualPanel.getLastMessage());
            updateInspector(null);
        });

        pushPushBtn.addActionListener(e -> {
            Integer value = readInputValue();
            if (value == null) {
                return;
            }
            if (visualPanel.addElementWithCheck(value)) {
                valueInput.setText("");
            }
            setStatus(visualPanel.getLastMessage());
            updateInspector(visualPanel.getSelectedNodeInfo());
        });

        insertBeginBtn.addActionListener(e -> {
            Integer value = readInputValue();
            if (value == null) {
                return;
            }
            if (visualPanel.insertAtPosition(value, "begin")) {
                valueInput.setText("");
            }
            setStatus(visualPanel.getLastMessage());
            updateInspector(visualPanel.getSelectedNodeInfo());
        });

        insertMiddleBtn.addActionListener(e -> {
            Integer value = readInputValue();
            if (value == null) {
                return;
            }
            Integer pos = promptForIndex("Insert Index", visualPanel.getElementCount(), true);
            if (pos == null) {
                return;
            }
            if (visualPanel.insertAtIndex(value, pos)) {
                valueInput.setText("");
            }
            setStatus(visualPanel.getLastMessage());
            updateInspector(visualPanel.getSelectedNodeInfo());
        });

        insertEndBtn.addActionListener(e -> {
            Integer value = readInputValue();
            if (value == null) {
                return;
            }
            if (visualPanel.insertAtPosition(value, "end")) {
                valueInput.setText("");
            }
            setStatus(visualPanel.getLastMessage());
            updateInspector(visualPanel.getSelectedNodeInfo());
        });

        deleteBeginBtn.addActionListener(e -> {
            Integer removed = visualPanel.removeAtPosition("begin");
            if (removed != null) {
                updateInspector(visualPanel.getSelectedNodeInfo());
            }
            setStatus(visualPanel.getLastMessage());
        });

        deleteMiddleBtn.addActionListener(e -> {
            if (visualPanel.getElementCount() == 0) {
                setStatus("The selected structure is empty.");
                return;
            }
            Integer pos = promptForIndex("Delete Index", visualPanel.getElementCount() - 1, false);
            if (pos == null) {
                return;
            }
            Integer removed = visualPanel.removeAtIndex(pos);
            if (removed != null) {
                updateInspector(visualPanel.getSelectedNodeInfo());
            }
            setStatus(visualPanel.getLastMessage());
        });

        deleteEndBtn.addActionListener(e -> {
            Integer removed = visualPanel.removeAtPosition("end");
            if (removed != null) {
                updateInspector(visualPanel.getSelectedNodeInfo());
            }
            setStatus(visualPanel.getLastMessage());
        });

        popRemoveBtn.addActionListener(e -> {
            Integer removed = visualPanel.removeElement();
            if (removed != null) {
                updateInspector(visualPanel.getSelectedNodeInfo());
            }
            setStatus(visualPanel.getLastMessage());
        });

        clearBtn.addActionListener(e -> {
            visualPanel.clear();
            updateInspector(null);
            setStatus(visualPanel.getLastMessage());
        });

        randomBtn.addActionListener(e -> {
            int size = 8; // default
            try {
                String sizeText = sizeInput.getText().trim();
                if (!sizeText.isEmpty()) {
                    size = Integer.parseInt(sizeText);
                    if (size <= 0 || size > 50) {
                        setStatus("Size must be between 1 and 50.");
                        return;
                    }
                }
            } catch (NumberFormatException ex) {
                setStatus("Invalid size. Using the default size of 8.");
            }
            visualPanel.fillRandom(size);
            updateInspector(null);
            setStatus(visualPanel.getLastMessage());
        });

        searchBtn.addActionListener(e -> {
            Integer value = readInputValue("Enter a value to search.");
            if (value == null) {
                return;
            }
            visualPanel.searchElement(value);
            updateInspector(visualPanel.getSelectedNodeInfo());
            setStatus(visualPanel.getLastMessage());
        });

        viewCodeBtn.addActionListener(e -> openCodeViewer());

        addFrontBtn.addActionListener(e -> {
            Integer value = readInputValue();
            if (value == null) {
                return;
            }
            if (visualPanel.addElementFront(value)) {
                valueInput.setText("");
            }
            updateInspector(visualPanel.getSelectedNodeInfo());
            setStatus(visualPanel.getLastMessage());
        });

        addRearBtn.addActionListener(e -> {
            Integer value = readInputValue();
            if (value == null) {
                return;
            }
            if (visualPanel.addElementRear(value)) {
                valueInput.setText("");
            }
            updateInspector(visualPanel.getSelectedNodeInfo());
            setStatus(visualPanel.getLastMessage());
        });

        removeFrontBtn.addActionListener(e -> {
            Integer removed = visualPanel.removeElementFront();
            if (removed != null) {
                updateInspector(visualPanel.getSelectedNodeInfo());
            }
            setStatus(visualPanel.getLastMessage());
        });

        removeRearBtn.addActionListener(e -> {
            Integer removed = visualPanel.removeElementRear();
            if (removed != null) {
                updateInspector(visualPanel.getSelectedNodeInfo());
            }
            setStatus(visualPanel.getLastMessage());
        });

        valueInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    triggerPrimaryAction();
                }
            }
        });
    }

    private JPanel buildInspectorPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setPreferredSize(new Dimension(260, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(70, 70, 70)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        panel.setBackground(new Color(36, 36, 36));

        inspectorTitle.setFont(new Font("Arial", Font.BOLD, 16));
        inspectorTitle.setForeground(Color.WHITE);

        inspectorArea.setEditable(false);
        inspectorArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        inspectorArea.setLineWrap(true);
        inspectorArea.setWrapStyleWord(true);
        inspectorArea.setBackground(new Color(24, 24, 24));
        inspectorArea.setForeground(new Color(235, 235, 235));
        inspectorArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inspectorArea.setText("Click any node or element to inspect its internal references.");

        panel.add(inspectorTitle, BorderLayout.NORTH);
        panel.add(new JScrollPane(inspectorArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        top.add(new JLabel("Data Structure:"));
        top.add(dsSelect);
        top.add(new JSeparator(JSeparator.VERTICAL));
        top.add(new JLabel("Value:"));
        top.add(valueInput);
        top.add(new JLabel("Size:"));
        top.add(sizeInput);
        top.add(pushPushBtn);
        top.add(popRemoveBtn);
        top.add(clearBtn);
        top.add(randomBtn);
        top.add(searchBtn);
        top.add(viewCodeBtn);
        top.add(insertBeginBtn);
        top.add(insertMiddleBtn);
        top.add(insertEndBtn);
        top.add(deleteBeginBtn);
        top.add(deleteMiddleBtn);
        top.add(deleteEndBtn);
        // Dequeue specific buttons
        top.add(addFrontBtn);
        top.add(addRearBtn);
        top.add(removeFrontBtn);
        top.add(removeRearBtn);

        insertBeginBtn.setVisible(false);
        insertMiddleBtn.setVisible(false);
        insertEndBtn.setVisible(false);
        deleteBeginBtn.setVisible(false);
        deleteMiddleBtn.setVisible(false);
        deleteEndBtn.setVisible(false);
        searchBtn.setVisible(true);
        addFrontBtn.setVisible(false);
        addRearBtn.setVisible(false);
        removeFrontBtn.setVisible(false);
        removeRearBtn.setVisible(false);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(statusLabel);

        panel.add(top, BorderLayout.NORTH);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void updateInsertButtonsVisibility(String dsType) {
        boolean isLinkedList = dsType.contains("LinkedList");
        boolean isDequeue = dsType.equals("Dequeue");

        insertBeginBtn.setVisible(isLinkedList);
        insertMiddleBtn.setVisible(isLinkedList);
        insertEndBtn.setVisible(isLinkedList);
        deleteBeginBtn.setVisible(isLinkedList);
        deleteMiddleBtn.setVisible(isLinkedList);
        deleteEndBtn.setVisible(isLinkedList);

        addFrontBtn.setVisible(isDequeue);
        addRearBtn.setVisible(isDequeue);
        removeFrontBtn.setVisible(isDequeue);
        removeRearBtn.setVisible(isDequeue);

        pushPushBtn.setVisible(!isDequeue);
        popRemoveBtn.setVisible(!isDequeue);
    }

    private Integer readInputValue() {
        return readInputValue("Please enter an integer value.");
    }

    private Integer readInputValue(String emptyMessage) {
        String input = valueInput.getText().trim();
        if (input.isEmpty()) {
            setStatus(emptyMessage);
            return null;
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            setStatus("Invalid input. Please enter an integer.");
            return null;
        }
    }

    private Integer promptForIndex(String title, int limit, boolean allowEndSlot) {
        String prompt = allowEndSlot
                ? "Enter an index from 0 to " + limit + ":"
                : "Enter an index from 0 to " + limit + ":";
        String posStr = JOptionPane.showInputDialog(this, prompt, title, JOptionPane.QUESTION_MESSAGE);
        if (posStr == null) {
            return null;
        }
        try {
            int index = Integer.parseInt(posStr.trim());
            int max = allowEndSlot ? limit : Math.max(limit, 0);
            if (index < 0 || index > max || (!allowEndSlot && index == max + 1)) {
                setStatus("Invalid index. Allowed range is 0 to " + max + ".");
                return null;
            }
            if (!allowEndSlot && index >= visualPanel.getElementCount()) {
                setStatus("Invalid index. Allowed range is 0 to " + (visualPanel.getElementCount() - 1) + ".");
                return null;
            }
            return index;
        } catch (NumberFormatException ex) {
            setStatus("Invalid input. Please enter an integer index.");
            return null;
        }
    }

    private void triggerPrimaryAction() {
        String selectedDS = (String) dsSelect.getSelectedItem();
        if ("Dequeue".equals(selectedDS)) {
            addRearBtn.doClick();
        } else if (selectedDS != null && selectedDS.contains("LinkedList")) {
            insertEndBtn.doClick();
        } else {
            pushPushBtn.doClick();
        }
    }

    private void openCodeViewer() {
        String selectedDS = (String) dsSelect.getSelectedItem();
        List<DSOperationCode> algorithms = DSCodeRepository.getAlgorithms(selectedDS);
        if (algorithms.isEmpty()) {
            setStatus("No code sample is available for " + selectedDS + ".");
            return;
        }

        DSOperationCode selected = (DSOperationCode) JOptionPane.showInputDialog(
                this,
                "Choose an operation to inspect:",
                "View Code",
                JOptionPane.PLAIN_MESSAGE,
                null,
                algorithms.toArray(),
                algorithms.get(0));

        if (selected != null) {
            codeViewer.open(selected);
            setStatus("Showing code for " + selected.getDisplayName() + ".");
        }
    }

    private void updateInspector(NodeVisualInfo info) {
        if (info == null) {
            inspectorTitle.setText("Node Inspector");
            inspectorArea.setText("Click any node or element to inspect its internal references.");
            return;
        }
        inspectorTitle.setText(info.structureType + " Node");
        inspectorArea.setText(info.toInspectorText());
        inspectorArea.setCaretPosition(0);
    }

    private void setStatus(String message) {
        statusLabel.setText("Status: " + message);
    }
}

/*
 * ============================== Visualization Panel
 * ==============================
 */
class DSVisualizerPanel extends JPanel {
    private BaseDataStructure currentDS;
    private String currentType = "Stack";
    private final Map<String, Color> colorMap = new HashMap<>();
    private int animationPhase = 0;
    private final javax.swing.Timer animationTimer;
    private Consumer<NodeVisualInfo> nodeSelectionListener;

    public DSVisualizerPanel() {
        setPreferredSize(new Dimension(1200, 600));
        setBackground(new Color(30, 30, 30));
        initializeColors();
        animationTimer = new javax.swing.Timer(16, e -> {
            animationPhase = (animationPhase + 1) % 120;
            if (currentDS != null) {
                currentDS.advanceAnimation();
            }
            repaint();
        });
        animationTimer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleNodeClick(e.getPoint());
            }
        });
    }

    public void setNodeSelectionListener(Consumer<NodeVisualInfo> nodeSelectionListener) {
        this.nodeSelectionListener = nodeSelectionListener;
    }

    public String getLastMessage() {
        return currentDS == null ? "Ready." : currentDS.getLastMessage();
    }

    public NodeVisualInfo getSelectedNodeInfo() {
        return currentDS == null ? null : currentDS.getSelectedNodeInfo();
    }

    private void initializeColors() {
        colorMap.put("Stack", new Color(52, 152, 219));
        colorMap.put("Queue", new Color(46, 204, 113));
        colorMap.put("Dequeue", new Color(52, 73, 94));
        colorMap.put("LinkedList", new Color(155, 89, 182));
        colorMap.put("Circular LinkedList", new Color(230, 126, 34));
        colorMap.put("Doubly LinkedList", new Color(231, 76, 60));
    }

    private void handleNodeClick(Point point) {
        if (currentDS == null) {
            return;
        }
        NodeVisualInfo info = currentDS.findNodeAt(point);
        if (info == null) {
            currentDS.clearSelection();
        } else {
            currentDS.selectNode(info.nodeId);
        }
        if (nodeSelectionListener != null) {
            nodeSelectionListener.accept(currentDS.getSelectedNodeInfo());
        }
        repaint();
    }

    public void switchDataStructure(String type) {
        List<Integer> oldValues = new ArrayList<>();
        if (currentDS != null && !currentDS.elements.isEmpty()) {
            oldValues.addAll(currentDS.elements);
        }

        currentType = type;
        currentDS = createDataStructure(type);

        List<Integer> skippedValues = new ArrayList<>();
        for (Integer value : oldValues) {
            if (!currentDS.add(value)) {
                skippedValues.add(value);
            }
        }

        if (skippedValues.isEmpty()) {
            currentDS.setLastMessage("Switched to " + type + ".");
        } else {
            currentDS.setLastMessage("Switched to " + type + ". Skipped incompatible values: " + skippedValues + ".");
        }

        if (nodeSelectionListener != null) {
            nodeSelectionListener.accept(null);
        }
        updatePreferredSize();
        repaint();
    }

    private BaseDataStructure createDataStructure(String type) {
        switch (type) {
            case "Stack":
                return new StackDS();
            case "Queue":
                return new QueueDS();
            case "Dequeue":
                return new DequeueDS();
            case "LinkedList":
                return new LinkedListDS();
            case "Circular LinkedList":
                return new CircularLinkedListDS();
            case "Doubly LinkedList":
                return new DoublyLinkedListDS();
            default:
                return new StackDS();
        }
    }

    public boolean addElementWithCheck(int value) {
        if (currentDS == null) {
            return false;
        }
        currentDS.prepareForMutation();
        boolean added = currentDS.add(value);
        updateAfterMutation();
        return added;
    }

    public Integer removeElement() {
        if (currentDS == null) {
            return null;
        }
        currentDS.prepareForMutation();
        Integer removed = currentDS.remove();
        updateAfterMutation();
        return removed;
    }

    public void clear() {
        if (currentDS == null) {
            return;
        }
        currentDS.prepareForMutation();
        currentDS.clear();
        updateAfterMutation();
        if (nodeSelectionListener != null) {
            nodeSelectionListener.accept(null);
        }
    }

    public void fillRandom(int count) {
        if (currentDS == null) {
            return;
        }
        currentDS.prepareForMutation();
        currentDS.clear();

        int limit = Math.min(count, currentDS.getCapacity());
        Random rnd = new Random();
        Set<Integer> used = new HashSet<>();
        while (currentDS.elements.size() < limit) {
            int value = rnd.nextInt(100) + 1;
            if (used.add(value)) {
                currentDS.add(value);
            }
        }

        if (count > limit) {
            currentDS.setLastMessage(currentDS.getStructureName() + " accepts at most " + currentDS.getCapacity()
                    + " elements. Filled " + limit + " random values instead.");
        } else {
            currentDS.setLastMessage("Filled " + limit + " random elements into " + currentDS.getStructureName() + ".");
        }
        updateAfterMutation();
    }

    public boolean insertAtPosition(int value, String position) {
        if (!(currentDS instanceof LinkedListBase)) {
            if (currentDS != null) {
                currentDS.setLastMessage("Indexed insertion is only available for linked-list structures.");
            }
            return false;
        }
        currentDS.prepareForMutation();
        boolean result = ((LinkedListBase) currentDS).insertAt(value, position);
        updateAfterMutation();
        return result;
    }

    public Integer removeAtPosition(String position) {
        if (!(currentDS instanceof LinkedListBase)) {
            if (currentDS != null) {
                currentDS.setLastMessage("Indexed deletion is only available for linked-list structures.");
            }
            return null;
        }
        currentDS.prepareForMutation();
        Integer removed = ((LinkedListBase) currentDS).removeAt(position);
        updateAfterMutation();
        return removed;
    }

    public int getElementCount() {
        return currentDS != null ? currentDS.elements.size() : 0;
    }

    public boolean insertAtIndex(int value, int index) {
        if (!(currentDS instanceof LinkedListBase)) {
            if (currentDS != null) {
                currentDS.setLastMessage("Insertion at an index is not valid for " + currentDS.getStructureName() + ".");
            }
            return false;
        }
        currentDS.prepareForMutation();
        boolean result = ((LinkedListBase) currentDS).insertAtIndex(value, index);
        updateAfterMutation();
        return result;
    }

    public Integer removeAtIndex(int index) {
        if (!(currentDS instanceof LinkedListBase)) {
            if (currentDS != null) {
                currentDS.setLastMessage("Deletion at an index is not valid for " + currentDS.getStructureName() + ".");
            }
            return null;
        }
        currentDS.prepareForMutation();
        Integer removed = ((LinkedListBase) currentDS).removeAtIndex(index);
        updateAfterMutation();
        return removed;
    }

    public int searchElement(int value) {
        if (currentDS == null) {
            return -1;
        }
        int index = currentDS.search(value);
        if (nodeSelectionListener != null) {
            nodeSelectionListener.accept(currentDS.getSelectedNodeInfo());
        }
        repaint();
        return index;
    }

    public boolean addElementFront(int value) {
        if (!(currentDS instanceof DequeueDS)) {
            if (currentDS != null) {
                currentDS.setLastMessage("Front insertion is only available for Dequeue.");
            }
            return false;
        }
        currentDS.prepareForMutation();
        boolean result = ((DequeueDS) currentDS).addFront(value);
        updateAfterMutation();
        return result;
    }

    public boolean addElementRear(int value) {
        if (!(currentDS instanceof DequeueDS)) {
            if (currentDS != null) {
                currentDS.setLastMessage("Rear insertion is only available for Dequeue.");
            }
            return false;
        }
        currentDS.prepareForMutation();
        boolean result = ((DequeueDS) currentDS).addRear(value);
        updateAfterMutation();
        return result;
    }

    public Integer removeElementFront() {
        if (!(currentDS instanceof DequeueDS)) {
            if (currentDS != null) {
                currentDS.setLastMessage("Front removal is only available for Dequeue.");
            }
            return null;
        }
        currentDS.prepareForMutation();
        Integer result = ((DequeueDS) currentDS).removeFront();
        updateAfterMutation();
        return result;
    }

    public Integer removeElementRear() {
        if (!(currentDS instanceof DequeueDS)) {
            if (currentDS != null) {
                currentDS.setLastMessage("Rear removal is only available for Dequeue.");
            }
            return null;
        }
        currentDS.prepareForMutation();
        Integer result = ((DequeueDS) currentDS).removeRear();
        updateAfterMutation();
        return result;
    }

    private void updateAfterMutation() {
        updatePreferredSize();
        if (nodeSelectionListener != null) {
            nodeSelectionListener.accept(currentDS == null ? null : currentDS.getSelectedNodeInfo());
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        updatePreferredSize();

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.drawString(currentType, 50, 50);

        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(180, 180, 180));
        g2.drawString(getDescription(currentType), 50, 75);

        if (currentDS != null) {
            currentDS.beginRenderCycle();
            currentDS.draw(g2, getWidth(), getHeight(), colorMap.get(currentType), animationPhase);
        }

        g2.dispose();
    }

    private void updatePreferredSize() {
        Dimension preferred = currentDS == null ? new Dimension(1200, 600) : currentDS.getPreferredVisualSize();
        if (!preferred.equals(getPreferredSize())) {
            setPreferredSize(preferred);
            revalidate();
        }
    }

    private String getDescription(String type) {
        switch (type) {
            case "Stack":
                return "LIFO: Last In, First Out";
            case "Queue":
                return "FIFO: First In, First Out";
            case "Dequeue":
                return "Double-ended queue: operations at both ends";
            case "LinkedList":
                return "Singly linked: one-way traversal with index-based edits";
            case "Circular LinkedList":
                return "Circular: tail connects back to the head";
            case "Doubly LinkedList":
                return "Two-way traversal with prev and next references";
            default:
                return "";
        }
    }
}

/*
 * ============================== Base Data Structure
 * ==============================
 */
abstract class BaseDataStructure {
    protected static final int DEFAULT_CAPACITY = 50;

    protected final List<Integer> elements = new ArrayList<>();
    protected final List<String> nodeIds = new ArrayList<>();
    protected final List<NodeVisualInfo> renderedNodes = new ArrayList<>();
    protected final Map<String, Rectangle2D.Float> currentBounds = new HashMap<>();
    protected final Map<String, Rectangle2D.Float> transitionStartBounds = new HashMap<>();
    protected int highlightIndex = -1;
    protected String selectedNodeId;
    private String lastMessage = "Ready.";
    private int nextNodeNumber = 1;
    private float transitionProgress = 1f;
    private float pulseProgress = 1f;

    public abstract String getStructureName();

    public int getCapacity() {
        return DEFAULT_CAPACITY;
    }

    public boolean allowsDuplicates() {
        return false;
    }

    public Dimension getPreferredVisualSize() {
        return new Dimension(1200, 600);
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isFull() {
        return elements.size() >= getCapacity();
    }

    public boolean add(int value) {
        return insertElementAt(elements.size(), value, "Inserted " + value + " into " + getStructureName() + ".");
    }

    public Integer remove() {
        return removeElementAt(elements.size() - 1,
                getStructureName() + " is empty.",
                "Removed %d from " + getStructureName() + ".");
    }

    public void clear() {
        elements.clear();
        nodeIds.clear();
        renderedNodes.clear();
        currentBounds.clear();
        transitionStartBounds.clear();
        highlightIndex = -1;
        selectedNodeId = null;
        setLastMessage("Cleared " + getStructureName() + ".");
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public void prepareForMutation() {
        transitionStartBounds.clear();
        for (Map.Entry<String, Rectangle2D.Float> entry : currentBounds.entrySet()) {
            Rectangle2D.Float rect = entry.getValue();
            transitionStartBounds.put(entry.getKey(), new Rectangle2D.Float(rect.x, rect.y, rect.width, rect.height));
        }
        transitionProgress = 0f;
    }

    public void advanceAnimation() {
        if (transitionProgress < 1f) {
            transitionProgress = Math.min(1f, transitionProgress + 0.12f);
        }
        if (pulseProgress < 1f) {
            pulseProgress = Math.min(1f, pulseProgress + 0.08f);
        }
    }

    public void beginRenderCycle() {
        renderedNodes.clear();
        currentBounds.clear();
    }

    public NodeVisualInfo findNodeAt(Point point) {
        for (NodeVisualInfo info : renderedNodes) {
            if (info.bounds.contains(point)) {
                return info;
            }
        }
        return null;
    }

    public NodeVisualInfo getSelectedNodeInfo() {
        if (selectedNodeId == null) {
            return null;
        }
        for (NodeVisualInfo info : renderedNodes) {
            if (selectedNodeId.equals(info.nodeId)) {
                return info;
            }
        }
        return null;
    }

    public void clearSelection() {
        selectedNodeId = null;
    }

    public void selectNode(String nodeId) {
        selectedNodeId = nodeId;
        pulseProgress = 0f;
    }

    public void selectIndex(int index) {
        if (index >= 0 && index < nodeIds.size()) {
            selectNode(nodeIds.get(index));
        }
    }

    public int search(int value) {
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i) == value) {
                highlightIndex = i;
                selectIndex(i);
                setLastMessage("Found " + value + " at index " + i + " in " + getStructureName() + ".");
                return i;
            }
        }
        highlightIndex = -1;
        selectedNodeId = null;
        setLastMessage(value + " was not found in " + getStructureName() + ".");
        return -1;
    }

    public String getNodeReference(int index) {
        if (index < 0 || index >= nodeIds.size()) {
            return "null";
        }
        return nodeIds.get(index);
    }

    protected boolean insertElementAt(int index, int value, String successMessage) {
        if (index < 0 || index > elements.size()) {
            setLastMessage("Invalid index " + index + ". Allowed range is 0 to " + elements.size() + ".");
            return false;
        }
        if (isFull()) {
            setLastMessage(getStructureName() + " has reached its capacity of " + getCapacity() + ".");
            return false;
        }
        if (!allowsDuplicates() && elements.contains(value)) {
            setLastMessage("Duplicate value " + value + " is not allowed in " + getStructureName() + ".");
            return false;
        }

        elements.add(index, value);
        String nodeId = createNodeId();
        nodeIds.add(index, nodeId);
        highlightIndex = index;
        selectNode(nodeId);
        setLastMessage(successMessage);
        return true;
    }

    protected Integer removeElementAt(int index, String emptyMessage, String successPattern) {
        if (elements.isEmpty()) {
            setLastMessage(emptyMessage);
            return null;
        }
        if (index < 0 || index >= elements.size()) {
            setLastMessage("Invalid index " + index + ". Allowed range is 0 to " + (elements.size() - 1) + ".");
            return null;
        }

        Integer removed = elements.remove(index);
        String removedId = nodeIds.remove(index);
        currentBounds.remove(removedId);
        transitionStartBounds.remove(removedId);

        if (elements.isEmpty()) {
            highlightIndex = -1;
            selectedNodeId = null;
        } else {
            int selectedIndex = Math.min(index, elements.size() - 1);
            highlightIndex = selectedIndex;
            selectIndex(selectedIndex);
        }
        setLastMessage(String.format(successPattern, removed));
        return removed;
    }

    protected Rectangle2D.Float animateBounds(String nodeId, float targetX, float targetY, float width, float height) {
        Rectangle2D.Float start = transitionStartBounds.get(nodeId);
        if (start == null) {
            start = new Rectangle2D.Float(targetX, targetY - 22f, width, height);
        }
        float eased = 1f - (float) Math.pow(1f - transitionProgress, 3);
        float x = start.x + (targetX - start.x) * eased;
        float y = start.y + (targetY - start.y) * eased;
        float w = start.width + (width - start.width) * eased;
        float h = start.height + (height - start.height) * eased;
        Rectangle2D.Float rect = new Rectangle2D.Float(x, y, w, h);
        currentBounds.put(nodeId, rect);
        return rect;
    }

    protected Rectangle scaleRect(Rectangle2D.Float rect, float scale) {
        float scaledWidth = rect.width * scale;
        float scaledHeight = rect.height * scale;
        float x = rect.x - (scaledWidth - rect.width) / 2f;
        float y = rect.y - (scaledHeight - rect.height) / 2f;
        return new Rectangle(Math.round(x), Math.round(y), Math.round(scaledWidth), Math.round(scaledHeight));
    }

    protected float getNodeScale(String nodeId, int index) {
        float scale = 1f;
        if (selectedNodeId != null && selectedNodeId.equals(nodeId)) {
            scale += 0.04f;
        }
        if (index == highlightIndex) {
            scale += 0.10f * (1f - (pulseProgress * 0.7f));
        }
        return scale;
    }

    protected Color emphasize(Color base, String nodeId, int index) {
        Color color = base;
        if (index == highlightIndex) {
            color = mix(color, new Color(255, 235, 59), 0.45f);
        }
        if (selectedNodeId != null && selectedNodeId.equals(nodeId)) {
            color = mix(color, Color.WHITE, 0.18f);
        }
        return color;
    }

    protected Color mix(Color first, Color second, float ratio) {
        float clamped = Math.max(0f, Math.min(1f, ratio));
        int r = Math.round(first.getRed() * (1f - clamped) + second.getRed() * clamped);
        int g = Math.round(first.getGreen() * (1f - clamped) + second.getGreen() * clamped);
        int b = Math.round(first.getBlue() * (1f - clamped) + second.getBlue() * clamped);
        return new Color(r, g, b);
    }

    protected Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    protected int getPointerAlpha() {
        return 90 + Math.round(165 * transitionProgress);
    }

    protected void registerNode(String nodeId,
            int index,
            int value,
            Rectangle bounds,
            String nextReference,
            String previousReference,
            String note) {
        renderedNodes.add(new NodeVisualInfo(nodeId,
                getStructureName(),
                index,
                value,
                bounds,
                nextReference,
                previousReference,
                note));
    }

    protected void drawCenteredString(Graphics2D g, String text, Rectangle rect, Font font, Color color) {
        Font oldFont = g.getFont();
        Color oldColor = g.getColor();
        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(text, textX, textY);
        g.setFont(oldFont);
        g.setColor(oldColor);
    }

    private String createNodeId() {
        return String.format("N%03d", nextNodeNumber++);
    }

    public abstract void draw(Graphics2D g, int w, int h, Color color, int animationPhase);
}

/*
 * ============================== LinkedList Base Interface
 * ==============================
 */
interface LinkedListBase {
    boolean insertAt(int value, String position);

    Integer removeAt(String position);

    boolean insertAtIndex(int value, int index);

    Integer removeAtIndex(int index);
}

/* ============================== Stack ============================== */
class StackDS extends BaseDataStructure {
    @Override
    public String getStructureName() {
        return "Stack";
    }

    @Override
    public Integer remove() {
        return removeElementAt(elements.size() - 1, "Stack is empty.", "Popped %d from the stack.");
    }

    @Override
    public Dimension getPreferredVisualSize() {
        int height = Math.max(600, 220 + elements.size() * 58);
        return new Dimension(1200, height);
    }

    @Override
    public void draw(Graphics2D g, int w, int h, Color color, int animationPhase) {
        int boxWidth = 120;
        int boxHeight = 50;
        int spacing = 5;
        int startX = w / 2 - boxWidth / 2;
        int startY = 150;

        g.setFont(new Font("Arial", Font.BOLD, 14));

        for (int i = 0; i < elements.size(); i++) {
            int displayIndex = elements.size() - 1 - i;
            int y = startY + i * (boxHeight + spacing);

            String nodeId = getNodeReference(displayIndex);
            Rectangle2D.Float target = animateBounds(nodeId, startX, y, boxWidth, boxHeight);
            Rectangle rect = scaleRect(target, getNodeScale(nodeId, displayIndex));

            Color fill = displayIndex == elements.size() - 1 ? new Color(255, 200, 0) : color;
            fill = emphasize(fill, nodeId, displayIndex);
            g.setColor(fill);
            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);

            g.setColor(selectedNodeId != null && selectedNodeId.equals(nodeId) ? Color.WHITE : Color.BLACK);
            g.setStroke(new BasicStroke(selectedNodeId != null && selectedNodeId.equals(nodeId) ? 3f : 2f));
            g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);

            drawCenteredString(g,
                    String.valueOf(elements.get(displayIndex)),
                    rect,
                    new Font("Arial", Font.BOLD, 16),
                    Color.WHITE);

            if (displayIndex == elements.size() - 1) {
                g.setColor(new Color(255, 220, 50));
                g.drawString("TOP →", rect.x - 80, rect.y + rect.height / 2 + 5);
            }

            g.setColor(new Color(180, 180, 180));
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString("[" + displayIndex + "]", rect.x + rect.width + 10, rect.y + rect.height / 2 + 5);
            g.setFont(new Font("Arial", Font.BOLD, 14));

            registerNode(nodeId,
                    displayIndex,
                    elements.get(displayIndex),
                    rect,
                    displayIndex < elements.size() - 1 ? getNodeReference(displayIndex + 1) : "null",
                    displayIndex > 0 ? getNodeReference(displayIndex - 1) : "null",
                    displayIndex == elements.size() - 1 ? "Current top element" : "Stack element");
        }

        if (!elements.isEmpty()) {
            int baseY = startY + (elements.size() - 1) * (boxHeight + spacing);
            g.setColor(new Color(100, 200, 100));
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("BASE", startX - 80, baseY + boxHeight);
        }

        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Size: " + elements.size(), startX, startY + (elements.size() * (boxHeight + spacing)) + 50);
        g.drawString("Capacity: " + getCapacity(), startX + 90, startY + (elements.size() * (boxHeight + spacing)) + 50);
    }
}

/* ============================== Queue ============================== */
class QueueDS extends BaseDataStructure {
    @Override
    public String getStructureName() {
        return "Queue";
    }

    @Override
    public Integer remove() {
        return removeElementAt(0, "Queue is empty.", "Dequeued %d from the queue.");
    }

    @Override
    public Dimension getPreferredVisualSize() {
        int height = Math.max(600, 220 + elements.size() * 58);
        return new Dimension(1200, height);
    }

    @Override
    public void draw(Graphics2D g, int w, int h, Color color, int animationPhase) {
        int boxWidth = 120;
        int boxHeight = 50;
        int spacing = 5;
        int startX = w / 2 - boxWidth / 2;
        int startY = 150;

        g.setFont(new Font("Arial", Font.BOLD, 14));

        for (int i = 0; i < elements.size(); i++) {
            int y = startY + i * (boxHeight + spacing);

            String nodeId = getNodeReference(i);
            Rectangle2D.Float target = animateBounds(nodeId, startX, y, boxWidth, boxHeight);
            Rectangle rect = scaleRect(target, getNodeScale(nodeId, i));

            Color fill;
            if (i == 0) {
                fill = new Color(0, 255, 0);
            } else if (i == elements.size() - 1) {
                fill = new Color(255, 100, 100);
            } else {
                fill = color;
            }
            fill = emphasize(fill, nodeId, i);
            g.setColor(fill);
            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);

            g.setColor(selectedNodeId != null && selectedNodeId.equals(nodeId) ? Color.WHITE : Color.BLACK);
            g.setStroke(new BasicStroke(selectedNodeId != null && selectedNodeId.equals(nodeId) ? 3f : 2f));
            g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);

            drawCenteredString(g,
                    String.valueOf(elements.get(i)),
                    rect,
                    new Font("Arial", Font.BOLD, 16),
                    Color.WHITE);

            if (i == 0) {
                g.setColor(new Color(50, 255, 100));
                g.drawString("FRONT →", rect.x - 90, rect.y + rect.height / 2 + 5);
            }
            if (i == elements.size() - 1) {
                g.setColor(new Color(255, 120, 120));
                g.drawString("REAR →", rect.x - 85, rect.y + rect.height / 2 + 5);
            }

            g.setColor(new Color(180, 180, 180));
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString("[" + i + "]", rect.x + rect.width + 10, rect.y + rect.height / 2 + 5);
            g.setFont(new Font("Arial", Font.BOLD, 14));

            registerNode(nodeId,
                    i,
                    elements.get(i),
                    rect,
                    i < elements.size() - 1 ? getNodeReference(i + 1) : "null",
                    i > 0 ? getNodeReference(i - 1) : "null",
                    i == 0 ? "Current front element" : (i == elements.size() - 1 ? "Current rear element" : "Queue element"));
        }

        g.setColor(withAlpha(new Color(100, 150, 255), getPointerAlpha()));
        g.setStroke(new BasicStroke(2));
        for (int i = 0; i < elements.size() - 1; i++) {
            int y1 = startY + i * (boxHeight + spacing) + boxHeight;
            int y2 = startY + (i + 1) * (boxHeight + spacing);
            int arrowX = startX + boxWidth / 2;
            g.drawLine(arrowX, y1, arrowX, y2);
            // Draw arrowhead
            g.fillPolygon(new int[] { arrowX, arrowX - 5, arrowX + 5 },
                    new int[] { y2, y2 - 8, y2 - 8 }, 3);
        }

        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Size: " + elements.size(), startX, startY + (elements.size() * (boxHeight + spacing)) + 50);
        g.drawString("Capacity: " + getCapacity(), startX + 90, startY + (elements.size() * (boxHeight + spacing)) + 50);
    }
}

abstract class AbstractLinkedStructure extends BaseDataStructure implements LinkedListBase {
    @Override
    public boolean allowsDuplicates() {
        return true;
    }

    @Override
    public boolean insertAt(int value, String position) {
        return insertAtIndex(value, positionToIndex(position, true));
    }

    @Override
    public Integer removeAt(String position) {
        return removeAtIndex(positionToIndex(position, false));
    }

    @Override
    public boolean insertAtIndex(int value, int index) {
        return insertElementAt(index, value, "Inserted " + value + " at index " + index + " in " + getStructureName() + ".");
    }

    @Override
    public Integer removeAtIndex(int index) {
        return removeElementAt(index,
                getStructureName() + " is empty.",
                "Removed %d from index " + index + " in " + getStructureName() + ".");
    }

    protected int positionToIndex(String position, boolean inserting) {
        switch (position) {
            case "begin":
                return 0;
            case "middle":
                return inserting ? elements.size() / 2 : Math.max(0, elements.size() / 2);
            case "end":
            default:
                return inserting ? elements.size() : Math.max(elements.size() - 1, 0);
        }
    }

    @Override
    public Dimension getPreferredVisualSize() {
        int width = Math.max(1200, 180 + elements.size() * 150);
        return new Dimension(width, 620);
    }
}

/*
 * ============================== LinkedList (Singly)
 * ==============================
 */
class LinkedListDS extends AbstractLinkedStructure {
    @Override
    public String getStructureName() {
        return "LinkedList";
    }

    @Override
    public void draw(Graphics2D g, int w, int h, Color color, int animationPhase) {
        int nodeWidth = 100;
        int nodeHeight = 70;
        int spacing = 40;

        int startX = 80;
        int centerY = h / 2 - 40;

        g.setFont(new Font("Arial", Font.BOLD, 12));

        for (int i = 0; i < elements.size(); i++) {
            int x = startX + i * (nodeWidth + spacing);
            int y = centerY;

            String nodeId = getNodeReference(i);
            Rectangle2D.Float target = animateBounds(nodeId, x, y, nodeWidth, nodeHeight);
            Rectangle rect = scaleRect(target, getNodeScale(nodeId, i));

            g.setColor(new Color(200, 200, 200));
            g.setFont(new Font("Courier", Font.BOLD, 11));
            g.drawString(nodeId, rect.x + 18, rect.y - 10);

            int dataWidth = rect.width / 2;
            int nextWidth = rect.width - dataWidth;

            g.setColor(new Color(30, 30, 30));
            g.fillRoundRect(rect.x, rect.y, dataWidth, rect.height, 16, 16);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(selectedNodeId != null && selectedNodeId.equals(nodeId) ? 3f : 2f));
            g.drawRoundRect(rect.x, rect.y, dataWidth, rect.height, 16, 16);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 9));
            g.drawString("data", rect.x + 8, rect.y + rect.height - 8);

            drawCenteredString(g,
                    String.valueOf(elements.get(i)),
                    new Rectangle(rect.x, rect.y, dataWidth, rect.height),
                    new Font("Arial", Font.BOLD, 16),
                    Color.WHITE);

            g.setColor(emphasize(new Color(0, 200, 255), nodeId, i));
            g.fillRoundRect(rect.x + dataWidth, rect.y, nextWidth, rect.height, 16, 16);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(rect.x + dataWidth, rect.y, nextWidth, rect.height, 16, 16);

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 9));
            g.drawString("next", rect.x + dataWidth + 8, rect.y + rect.height - 8);

            drawCenteredString(g,
                    i < elements.size() - 1 ? getNodeReference(i + 1) : "null",
                    new Rectangle(rect.x + dataWidth, rect.y, nextWidth, rect.height),
                    new Font("Courier", Font.BOLD, 11),
                    Color.BLACK);

            if (i < elements.size() - 1) {
                int arrowStartX = rect.x + rect.width + 5;
                int arrowEndX = startX + (i + 1) * (nodeWidth + spacing) - 5;
                int arrowY = rect.y + rect.height / 2;

                g.setColor(withAlpha(new Color(100, 150, 255), getPointerAlpha()));
                g.setStroke(new BasicStroke(2));
                g.drawLine(arrowStartX, arrowY, arrowEndX, arrowY);
                int[] xPoints = { arrowEndX, arrowEndX - 10, arrowEndX - 10 };
                int[] yPoints = { arrowY, arrowY - 5, arrowY + 5 };
                g.fillPolygon(xPoints, yPoints, 3);
                g.setColor(Color.BLACK);
                g.drawPolygon(xPoints, yPoints, 3);
            }

            registerNode(nodeId,
                    i,
                    elements.get(i),
                    rect,
                    i < elements.size() - 1 ? getNodeReference(i + 1) : "null",
                    "null",
                    i == 0 ? "Head node" : "Singly linked node");
        }

        if (!elements.isEmpty()) {
            g.setColor(new Color(255, 200, 0));
            g.setFont(new Font("Arial", Font.BOLD, 11));
            g.drawString("head →", startX - 65, centerY + 15);
        }

        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Size: " + elements.size(), startX, h - 50);
        g.drawString("Singly LinkedList - One-way traversal", startX, h - 25);
        g.drawString("Capacity: " + getCapacity(), startX + 230, h - 50);
    }
}

/*
 * ============================== Circular LinkedList
 * ==============================
 */
class CircularLinkedListDS extends AbstractLinkedStructure {
    @Override
    public String getStructureName() {
        return "Circular LinkedList";
    }

    @Override
    public void draw(Graphics2D g, int w, int h, Color color, int animationPhase) {
        if (elements.isEmpty()) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.drawString("Circular LinkedList (Empty)", 100, 100);
            return;
        }

        int nodeWidth = 100;
        int nodeHeight = 60;
        int spacing = 30;
        int startX = 80;
        int centerY = h / 2 - 20;

        g.setFont(new Font("Arial", Font.BOLD, 12));

        // Draw linear nodes (data | ptr) left to right
        for (int i = 0; i < elements.size(); i++) {
            int x = startX + i * (nodeWidth + spacing);
            int y = centerY;

            String nodeId = getNodeReference(i);
            Rectangle2D.Float target = animateBounds(nodeId, x, y, nodeWidth, nodeHeight);
            Rectangle rect = scaleRect(target, getNodeScale(nodeId, i));

            g.setColor(new Color(200, 200, 200));
            g.setFont(new Font("Courier", Font.BOLD, 11));
            g.drawString(nodeId, rect.x + 10, rect.y - 10);

            int dataW = rect.width * 55 / 100;
            int ptrW = rect.width - dataW;

            g.setColor(new Color(30, 30, 30));
            g.fillRoundRect(rect.x, rect.y, dataW, rect.height, 16, 16);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(rect.x, rect.y, dataW, rect.height, 16, 16);
            drawCenteredString(g,
                    String.valueOf(elements.get(i)),
                    new Rectangle(rect.x, rect.y, dataW, rect.height),
                    new Font("Arial", Font.BOLD, 16),
                    Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 9));
            g.drawString("data", rect.x + 6, rect.y + rect.height - 6);

            g.setColor(emphasize(new Color(0, 200, 255), nodeId, i));
            g.fillRoundRect(rect.x + dataW, rect.y, ptrW, rect.height, 16, 16);
            g.setColor(Color.BLACK);
            g.drawRoundRect(rect.x + dataW, rect.y, ptrW, rect.height, 16, 16);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 9));
            g.drawString("ptr", rect.x + dataW + 6, rect.y + rect.height - 6);

            drawCenteredString(g,
                    i < elements.size() - 1 ? getNodeReference(i + 1) : getNodeReference(0),
                    new Rectangle(rect.x + dataW, rect.y, ptrW, rect.height),
                    new Font("Courier", Font.BOLD, 11),
                    Color.BLACK);

            if (i < elements.size() - 1) {
                int ax = rect.x + rect.width + 6;
                int ay = rect.y + rect.height / 2;
                int bx = startX + (i + 1) * (nodeWidth + spacing) - 6;
                int by = ay;
                g.setColor(withAlpha(new Color(100, 150, 255), getPointerAlpha()));
                g.setStroke(new BasicStroke(2));
                g.drawLine(ax, ay, bx, by);
                g.fillPolygon(new int[] { bx, bx - 10, bx - 10 }, new int[] { by, by - 6, by + 6 }, 3);
            }

            registerNode(nodeId,
                    i,
                    elements.get(i),
                    rect,
                    i < elements.size() - 1 ? getNodeReference(i + 1) : getNodeReference(0),
                    i > 0 ? getNodeReference(i - 1) : getNodeReference(elements.size() - 1),
                    i == 0 ? "Head node inside the circular chain" : "Circular node");
        }

        int lastRight = startX + (elements.size() - 1) * (nodeWidth + spacing) + nodeWidth;
        int arrowTop = centerY + nodeHeight / 2;
        int wrapDown = centerY + nodeHeight + 70;
        int leftX = startX - 50;

        g.setColor(withAlpha(new Color(0, 200, 100), getPointerAlpha()));
        g.setStroke(new BasicStroke(3));

        int sx = lastRight + 10;
        int sy = arrowTop;
        g.drawLine(sx, sy, sx, wrapDown);
        g.drawLine(sx, wrapDown, leftX, wrapDown);
        int upY = centerY - 20;
        g.drawLine(leftX, wrapDown, leftX, upY);
        int toX = startX - 6;
        g.drawLine(leftX, upY, toX, upY);
        g.fillPolygon(new int[] { toX, toX - 8, toX - 8 }, new int[] { upY, upY - 6, upY + 6 }, 3);

        g.setColor(new Color(255, 200, 0));
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("HEAD", startX - 70, centerY + 10);
        g.setColor(new Color(255, 200, 0));
        g.setStroke(new BasicStroke(2));
        g.drawLine(startX - 20, centerY + 12, startX + 6, centerY + 12);
        g.fillPolygon(new int[] { startX + 6, startX, startX }, new int[] { centerY + 12, centerY + 8, centerY + 16 },
                3);

        int tailX = startX + (elements.size() - 1) * (nodeWidth + spacing);
        g.setColor(new Color(255, 100, 100));
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("TAIL", tailX + nodeWidth + 15, centerY + 10);
        g.setStroke(new BasicStroke(2));
        g.drawLine(tailX + nodeWidth + 10, centerY + 12, tailX + nodeWidth - 6, centerY + 12);
        g.fillPolygon(new int[] { tailX + nodeWidth - 6, tailX + nodeWidth, tailX + nodeWidth },
                new int[] { centerY + 12, centerY + 8, centerY + 16 }, 3);

        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Size: " + elements.size(), startX, h - 50);
        g.drawString("Circular LinkedList - linear layout with wrap arrow", startX, h - 25);
        g.drawString("Capacity: " + getCapacity(), startX + 250, h - 50);
    }
}

/*
 * ============================== Doubly LinkedList
 * ==============================
 */
class DoublyLinkedListDS extends AbstractLinkedStructure {
    @Override
    public String getStructureName() {
        return "Doubly LinkedList";
    }

    @Override
    public void draw(Graphics2D g, int w, int h, Color color, int animationPhase) {
        int nodeWidth = 120;
        int nodeHeight = 70;
        int spacing = 50;

        int startX = 60;
        int centerY = h / 2 - 30;

        g.setFont(new Font("Arial", Font.BOLD, 12));

        for (int i = 0; i < elements.size(); i++) {
            int x = startX + i * (nodeWidth + spacing);
            int y = centerY;

                String nodeId = getNodeReference(i);
                Rectangle2D.Float target = animateBounds(nodeId, x, y, nodeWidth, nodeHeight);
                Rectangle rect = scaleRect(target, getNodeScale(nodeId, i));

            g.setColor(new Color(200, 200, 200));
            g.setFont(new Font("Courier", Font.BOLD, 11));
                g.drawString(nodeId, rect.x + 28, rect.y - 10);

            int prevWidth = 30;
                int dataWidth = rect.width - 60;
                int nextWidth = 30;

            g.setColor(new Color(255, 150, 100));
                g.fillRoundRect(rect.x, rect.y, prevWidth, rect.height, 16, 16);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
                g.drawRoundRect(rect.x, rect.y, prevWidth, rect.height, 16, 16);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 8));
                g.drawString("prev", rect.x + 2, rect.y + rect.height - 8);

            g.setColor(new Color(30, 30, 30));
                g.fillRoundRect(rect.x + prevWidth, rect.y, dataWidth, rect.height, 16, 16);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
                g.drawRoundRect(rect.x + prevWidth, rect.y, dataWidth, rect.height, 16, 16);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 9));
                g.drawString("data", rect.x + prevWidth + 15, rect.y + rect.height - 8);

                drawCenteredString(g,
                    String.valueOf(elements.get(i)),
                    new Rectangle(rect.x + prevWidth, rect.y, dataWidth, rect.height),
                    new Font("Arial", Font.BOLD, 16),
                    Color.WHITE);

                g.setColor(emphasize(new Color(0, 200, 255), nodeId, i));
                g.fillRoundRect(rect.x + prevWidth + dataWidth, rect.y, nextWidth, rect.height, 16, 16);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
                g.drawRoundRect(rect.x + prevWidth + dataWidth, rect.y, nextWidth, rect.height, 16, 16);

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 8));
                g.drawString("next", rect.x + prevWidth + dataWidth + 2, rect.y + rect.height - 8);

                drawCenteredString(g,
                    i > 0 ? getNodeReference(i - 1) : "null",
                    new Rectangle(rect.x, rect.y, prevWidth, rect.height),
                    new Font("Courier", Font.BOLD, 8),
                    Color.WHITE);
                drawCenteredString(g,
                    i < elements.size() - 1 ? getNodeReference(i + 1) : "null",
                    new Rectangle(rect.x + prevWidth + dataWidth, rect.y, nextWidth, rect.height),
                    new Font("Courier", Font.BOLD, 8),
                    Color.BLACK);

            if (i < elements.size() - 1) {
                int nextNodeX = startX + (i + 1) * (nodeWidth + spacing);
                g.setColor(withAlpha(new Color(0, 200, 255), getPointerAlpha()));
                g.setStroke(new BasicStroke(2));
                g.drawLine(rect.x + rect.width + 5, rect.y + rect.height / 2, nextNodeX - 5, rect.y + rect.height / 2);
                g.fillPolygon(new int[] { nextNodeX - 5, nextNodeX - 13, nextNodeX - 13 },
                    new int[] { rect.y + rect.height / 2, rect.y + rect.height / 2 - 5, rect.y + rect.height / 2 + 5 }, 3);
            }

            if (i > 0) {
                int prevNodeX = startX + (i - 1) * (nodeWidth + spacing);
                g.setColor(withAlpha(new Color(255, 150, 100), getPointerAlpha()));
                g.setStroke(new BasicStroke(2));
                g.drawLine(rect.x - 5, rect.y - 15, prevNodeX + nodeWidth + 5, rect.y - 15);
                g.fillPolygon(new int[] { rect.x - 5, rect.x + 3, rect.x + 3 },
                    new int[] { rect.y - 15, rect.y - 20, rect.y - 10 }, 3);
            }

                registerNode(nodeId,
                    i,
                    elements.get(i),
                    rect,
                    i < elements.size() - 1 ? getNodeReference(i + 1) : "null",
                    i > 0 ? getNodeReference(i - 1) : "null",
                    i == 0 ? "Head node with bidirectional links" : "Doubly linked node");
        }

        if (!elements.isEmpty()) {
            g.setColor(new Color(255, 200, 0));
            g.setFont(new Font("Arial", Font.BOLD, 11));
            g.drawString("head →", startX - 55, centerY + 15);
        }

        g.setColor(new Color(255, 150, 100));
        g.fillRect(50, h - 70, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString("Prev Pointer", 70, h - 62);

        g.setColor(new Color(0, 200, 255));
        g.fillRect(200, h - 70, 10, 10);
        g.drawString("Next Pointer", 220, h - 62);

        g.setColor(new Color(150, 150, 150));
        g.drawString("Size: " + elements.size(), startX, h - 35);
        g.drawString("Doubly LinkedList - Two-way traversal", startX, h - 12);
        g.drawString("Capacity: " + getCapacity(), startX + 250, h - 35);
    }
}

/*
 * ============================== Dequeue (Double-Ended Queue)
 * ==============================
 */
class DequeueDS extends BaseDataStructure {
    @Override
    public String getStructureName() {
        return "Dequeue";
    }

    public boolean addFront(int value) {
        return insertElementAt(0, value, "Added " + value + " at the front of the dequeue.");
    }

    public boolean addRear(int value) {
        return insertElementAt(elements.size(), value, "Added " + value + " at the rear of the dequeue.");
    }

    public Integer removeFront() {
        return removeElementAt(0, "Dequeue is empty.", "Removed %d from the front of the dequeue.");
    }

    public Integer removeRear() {
        return removeElementAt(elements.size() - 1, "Dequeue is empty.", "Removed %d from the rear of the dequeue.");
    }

    @Override
    public boolean add(int value) {
        return addRear(value);
    }

    @Override
    public Integer remove() {
        return removeFront();
    }

    @Override
    public Dimension getPreferredVisualSize() {
        int width = Math.max(1200, 180 + elements.size() * 110);
        return new Dimension(width, 620);
    }

    @Override
    public void draw(Graphics2D g, int w, int h, Color color, int animationPhase) {
        int boxWidth = 80;
        int boxHeight = 50;
        int spacing = 10;
        int startX = 100;
        int centerY = h / 2 - boxHeight / 2;

        g.setFont(new Font("Arial", Font.BOLD, 14));

        if (elements.isEmpty()) {
            g.setColor(Color.WHITE);
            g.drawString("Dequeue is empty", startX, centerY + boxHeight / 2);
            return;
        }

        for (int i = 0; i < elements.size(); i++) {
            int x = startX + i * (boxWidth + spacing);
            int y = centerY;

            String nodeId = getNodeReference(i);
            Rectangle2D.Float target = animateBounds(nodeId, x, y, boxWidth, boxHeight);
            Rectangle rect = scaleRect(target, getNodeScale(nodeId, i));

            Color fill;
            if (i == 0) {
                fill = new Color(50, 200, 50);
            } else if (i == elements.size() - 1) {
                fill = new Color(200, 50, 50);
            } else {
                fill = color;
            }
            g.setColor(emphasize(fill, nodeId, i));
            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 16, 16);

            g.setColor(selectedNodeId != null && selectedNodeId.equals(nodeId) ? Color.WHITE : Color.BLACK);
            g.setStroke(new BasicStroke(selectedNodeId != null && selectedNodeId.equals(nodeId) ? 3f : 2f));
            g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 16, 16);

            drawCenteredString(g,
                    String.valueOf(elements.get(i)),
                    rect,
                    new Font("Arial", Font.BOLD, 16),
                    Color.WHITE);

            g.setColor(new Color(180, 180, 180));
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString("[" + i + "]", rect.x + rect.width / 2 - 8, rect.y + rect.height + 15);
            g.setFont(new Font("Arial", Font.BOLD, 14));

            if (i < elements.size() - 1) {
                int arrowStartX = rect.x + rect.width;
                int arrowEndX = x + boxWidth + spacing;
                int arrowY = rect.y + rect.height / 2;
                g.setColor(withAlpha(new Color(100, 150, 255), getPointerAlpha()));
                g.setStroke(new BasicStroke(2));
                g.drawLine(arrowStartX, arrowY - 5, arrowEndX, arrowY - 5);
                g.fillPolygon(new int[] { arrowEndX, arrowEndX - 6, arrowEndX - 6 },
                        new int[] { arrowY - 5, arrowY - 9, arrowY - 1 }, 3);
                g.drawLine(arrowEndX, arrowY + 5, arrowStartX, arrowY + 5);
                g.fillPolygon(new int[] { arrowStartX, arrowStartX + 6, arrowStartX + 6 },
                        new int[] { arrowY + 5, arrowY + 1, arrowY + 9 }, 3);
            }

            registerNode(nodeId,
                    i,
                    elements.get(i),
                    rect,
                    i < elements.size() - 1 ? getNodeReference(i + 1) : "null",
                    i > 0 ? getNodeReference(i - 1) : "null",
                    i == 0 ? "Front element" : (i == elements.size() - 1 ? "Rear element" : "Deque element"));
        }

        g.setColor(new Color(50, 255, 100));
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("FRONT", startX + boxWidth / 2 - 20, centerY - 15);

        int rearX = startX + (elements.size() - 1) * (boxWidth + spacing);
        g.setColor(new Color(255, 100, 100));
        g.drawString("REAR", rearX + boxWidth / 2 - 15, centerY - 15);

        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Size: " + elements.size(), startX, h - 50);
        g.drawString("Capacity: " + getCapacity(), startX + 90, h - 50);
        g.drawString("Dequeue - Double-Ended Queue (add/remove from both ends)", startX, h - 25);
    }
}
