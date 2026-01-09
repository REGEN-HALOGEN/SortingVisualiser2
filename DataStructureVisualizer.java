import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * DataStructureVisualizer.java
 * Interactive visualizer for data structures: Stack, Queue, LinkedList, Circular LinkedList, Doubly LinkedList.
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
        SwingUtilities.invokeLater(() -> new DSVisualizerFrame().setVisible(true));
    }
}

/* ============================== Main Frame ============================== */
class DSVisualizerFrame extends JFrame {
    private final JComboBox<String> dsSelect = new JComboBox<>(
            new String[]{"Stack", "Queue", "LinkedList", "Circular LinkedList", "Doubly LinkedList"});
    private final JTextField valueInput = new JTextField(10);
    private final JButton pushPushBtn = new JButton("Push/Add");
    private final JButton popRemoveBtn = new JButton("Pop/Remove");
    private final JButton clearBtn = new JButton("Clear");
    private final JButton randomBtn = new JButton("Random Fill");
    private final JButton insertBeginBtn = new JButton("Insert at Begin");
    private final JButton insertMiddleBtn = new JButton("Insert at Middle");
    private final JButton insertEndBtn = new JButton("Insert at End");
    private final JLabel statusLabel = new JLabel("Status: Ready");
    private final DSVisualizerPanel visualPanel = new DSVisualizerPanel();

    public DSVisualizerFrame() {
        super("Data Structure Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Wrap visualPanel in a JScrollPane
        JScrollPane scrollPane = new JScrollPane(visualPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        add(buildControlPanel(), BorderLayout.SOUTH);

        // Initialize with Stack
        visualPanel.switchDataStructure("Stack");

        // Event Listeners
        dsSelect.addActionListener(e -> {
            String selectedDS = (String) dsSelect.getSelectedItem();
            visualPanel.switchDataStructure(selectedDS);
            updateInsertButtonsVisibility(selectedDS);
            statusLabel.setText("Status: Switched to " + selectedDS);
        });

        pushPushBtn.addActionListener(e -> {
            String input = valueInput.getText().trim();
            if (input.isEmpty()) {
                statusLabel.setText("Status: Please enter a value");
                return;
            }
            try {
                int value = Integer.parseInt(input);
                visualPanel.addElement(value);
                statusLabel.setText("Status: Added " + value);
                valueInput.setText("");
            } catch (NumberFormatException ex) {
                statusLabel.setText("Status: Invalid input - enter an integer");
            }
        });

        insertBeginBtn.addActionListener(e -> {
            String input = valueInput.getText().trim();
            if (input.isEmpty()) {
                statusLabel.setText("Status: Please enter a value");
                return;
            }
            try {
                int value = Integer.parseInt(input);
                visualPanel.insertAtPosition(value, "begin");
                statusLabel.setText("Status: Inserted " + value + " at beginning");
                valueInput.setText("");
            } catch (NumberFormatException ex) {
                statusLabel.setText("Status: Invalid input - enter an integer");
            }
        });

        insertMiddleBtn.addActionListener(e -> {
            String input = valueInput.getText().trim();
            if (input.isEmpty()) {
                statusLabel.setText("Status: Please enter a value");
                return;
            }
            try {
                int value = Integer.parseInt(input);
                visualPanel.insertAtPosition(value, "middle");
                statusLabel.setText("Status: Inserted " + value + " at middle");
                valueInput.setText("");
            } catch (NumberFormatException ex) {
                statusLabel.setText("Status: Invalid input - enter an integer");
            }
        });

        insertEndBtn.addActionListener(e -> {
            String input = valueInput.getText().trim();
            if (input.isEmpty()) {
                statusLabel.setText("Status: Please enter a value");
                return;
            }
            try {
                int value = Integer.parseInt(input);
                visualPanel.insertAtPosition(value, "end");
                statusLabel.setText("Status: Inserted " + value + " at end");
                valueInput.setText("");
            } catch (NumberFormatException ex) {
                statusLabel.setText("Status: Invalid input - enter an integer");
            }
        });

        popRemoveBtn.addActionListener(e -> {
            Integer removed = visualPanel.removeElement();
            if (removed != null) {
                statusLabel.setText("Status: Removed " + removed);
            } else {
                statusLabel.setText("Status: Data structure is empty!");
            }
        });

        clearBtn.addActionListener(e -> {
            visualPanel.clear();
            statusLabel.setText("Status: Cleared");
        });

        randomBtn.addActionListener(e -> {
            visualPanel.fillRandom(8);
            statusLabel.setText("Status: Filled with random elements");
        });

        // Allow Enter key to add element
        valueInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    pushPushBtn.doClick();
                }
            }
        });
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        top.add(new JLabel("Data Structure:"));
        top.add(dsSelect);
        top.add(new JSeparator(JSeparator.VERTICAL));
        top.add(new JLabel("Value:"));
        top.add(valueInput);
        top.add(pushPushBtn);
        top.add(popRemoveBtn);
        top.add(clearBtn);
        top.add(randomBtn);
        top.add(insertBeginBtn);
        top.add(insertMiddleBtn);
        top.add(insertEndBtn);

        // Initially hide insert buttons
        insertBeginBtn.setVisible(false);
        insertMiddleBtn.setVisible(false);
        insertEndBtn.setVisible(false);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(statusLabel);

        panel.add(top, BorderLayout.NORTH);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void updateInsertButtonsVisibility(String dsType) {
        boolean isLinkedList = dsType.contains("LinkedList");
        insertBeginBtn.setVisible(isLinkedList);
        insertMiddleBtn.setVisible(isLinkedList);
        insertEndBtn.setVisible(isLinkedList);
    }
}

/* ============================== Visualization Panel ============================== */
class DSVisualizerPanel extends JPanel {
    private BaseDataStructure currentDS;
    private String currentType = "Stack";
    private final Map<String, Color> colorMap = new HashMap<>();
    private int animationPhase = 0; // For animation effects
    private java.util.Timer animationTimer;

    public DSVisualizerPanel() {
        setPreferredSize(new Dimension(1200, 600));
        setBackground(new Color(30, 30, 30));
        initializeColors();
        startAnimationTimer();
    }

    private void initializeColors() {
        colorMap.put("Stack", new Color(52, 152, 219));      // Blue
        colorMap.put("Queue", new Color(46, 204, 113));      // Green
        colorMap.put("LinkedList", new Color(155, 89, 182)); // Purple
        colorMap.put("Circular LinkedList", new Color(230, 126, 34)); // Orange
        colorMap.put("Doubly LinkedList", new Color(231, 76, 60));    // Red
    }

    private void startAnimationTimer() {
        animationTimer = new java.util.Timer();
        animationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                animationPhase = (animationPhase + 1) % 60;
                SwingUtilities.invokeLater(() -> repaint());
            }
        }, 0, 50);
    }

    public void switchDataStructure(String type) {
        currentType = type;
        switch (type) {
            case "Stack":
                currentDS = new StackDS();
                break;
            case "Queue":
                currentDS = new QueueDS();
                break;
            case "LinkedList":
                currentDS = new LinkedListDS();
                break;
            case "Circular LinkedList":
                currentDS = new CircularLinkedListDS();
                break;
            case "Doubly LinkedList":
                currentDS = new DoublyLinkedListDS();
                break;
            default:
                currentDS = new StackDS();
        }
        repaint();
    }

    public void addElement(int value) {
        if (currentDS != null) {
            currentDS.add(value);
            repaint();
        }
    }

    public Integer removeElement() {
        if (currentDS != null) {
            return currentDS.remove();
        }
        return null;
    }

    public void clear() {
        if (currentDS != null) {
            currentDS.clear();
            repaint();
        }
    }

    public void fillRandom(int count) {
        if (currentDS != null) {
            currentDS.clear();
            Random rnd = new Random();
            for (int i = 0; i < count; i++) {
                currentDS.add(rnd.nextInt(100) + 1);
            }
            repaint();
        }
    }

    public void insertAtPosition(int value, String position) {
        if (currentDS != null && currentDS instanceof LinkedListBase) {
            ((LinkedListBase) currentDS).insertAt(value, position);
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Update preferred size for vertical layouts (Stack, Queue)
        updatePreferredSize();

        // Draw title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.drawString(currentType, 50, 50);

        // Draw description
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(180, 180, 180));
        String desc = getDescription(currentType);
        g2.drawString(desc, 50, 75);

        // Draw the data structure
        if (currentDS != null) {
            currentDS.draw(g2, w, h, colorMap.get(currentType), animationPhase);
        }
    }

    private void updatePreferredSize() {
        if (currentDS != null && (currentType.equals("Stack") || currentType.equals("Queue"))) {
            int boxHeight = 50;
            int spacing = 5;
            int startY = 150;
            int totalHeight = startY + (currentDS.elements.size() * (boxHeight + spacing)) + 100;
            
            if (getParent() != null) {
                int parentHeight = getParent().getHeight();
                if (totalHeight > parentHeight) {
                    setPreferredSize(new Dimension(1200, totalHeight));
                    revalidate();
                    return;
                }
            }
            setPreferredSize(new Dimension(1200, 600));
        } else {
            setPreferredSize(new Dimension(1200, 600));
        }
    }

    private String getDescription(String type) {
        switch (type) {
            case "Stack": return "LIFO: Last In, First Out";
            case "Queue": return "FIFO: First In, First Out";
            case "LinkedList": return "Singly Linked: One-way traversal";
            case "Circular LinkedList": return "Circular: Last node points to first";
            case "Doubly LinkedList": return "Two-way: Forward and backward traversal";
            default: return "";
        }
    }
}

/* ============================== Base Data Structure ============================== */
abstract class BaseDataStructure {
    protected java.util.List<Integer> elements = new ArrayList<>();

    public void add(int value) {
        elements.add(value);
    }

    public Integer remove() {
        if (elements.isEmpty()) return null;
        return elements.remove(elements.size() - 1);
    }

    public void clear() {
        elements.clear();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public abstract void draw(Graphics2D g, int w, int h, Color color, int animationPhase);
}

/* ============================== LinkedList Base Interface ============================== */
interface LinkedListBase {
    void insertAt(int value, String position);
}

/* ============================== Stack ============================== */
class StackDS extends BaseDataStructure {
    @Override
    public Integer remove() {
        if (elements.isEmpty()) return null;
        return elements.remove(elements.size() - 1);
    }

    @Override
    public void draw(Graphics2D g, int w, int h, Color color, int animationPhase) {
        int boxWidth = 120;
        int boxHeight = 50;
        int spacing = 5;
        int startX = w / 2 - boxWidth / 2;
        int startY = 150;

        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.WHITE);

        // Draw from bottom to top (index 0 at bottom)
        for (int i = 0; i < elements.size(); i++) {
            int displayIndex = elements.size() - 1 - i; // Reverse for display
            int y = startY + i * (boxHeight + spacing);

            // Draw box
            if (displayIndex == elements.size() - 1) { // Top of stack (most recent)
                g.setColor(new Color(255, 200, 0)); // Highlight top
                g.fillRect(startX, y, boxWidth, boxHeight);
                g.setColor(Color.BLACK);
                g.drawString("TOP →", startX - 80, y + boxHeight / 2 + 5);
            } else {
                g.setColor(color);
                g.fillRect(startX, y, boxWidth, boxHeight);
            }

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawRect(startX, y, boxWidth, boxHeight);

            // Draw value
            g.setColor(Color.WHITE);
            String val = String.valueOf(elements.get(displayIndex));
            FontMetrics fm = g.getFontMetrics();
            int textX = startX + (boxWidth - fm.stringWidth(val)) / 2;
            int textY = y + ((boxHeight - fm.getHeight()) / 2) + fm.getAscent();
            g.drawString(val, textX, textY);

            // Draw index label
            g.setColor(new Color(180, 180, 180));
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString("[" + displayIndex + "]", startX + boxWidth + 10, y + boxHeight / 2 + 5);
            g.setFont(new Font("Arial", Font.BOLD, 14));
        }

        // Draw base label
        if (!elements.isEmpty()) {
            int baseY = startY + (elements.size() - 1) * (boxHeight + spacing);
            g.setColor(new Color(100, 200, 100));
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("BASE", startX - 80, baseY + boxHeight);
        }

        // Draw info
        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Size: " + elements.size(), startX, startY + (elements.size() * (boxHeight + spacing)) + 50);
    }
}

/* ============================== Queue ============================== */
class QueueDS extends BaseDataStructure {
    @Override
    public Integer remove() {
        if (elements.isEmpty()) return null;
        return elements.remove(0); // Remove from front
    }

    @Override
    public void draw(Graphics2D g, int w, int h, Color color, int animationPhase) {
        int boxWidth = 120;
        int boxHeight = 50;
        int spacing = 5;
        int startX = w / 2 - boxWidth / 2;
        int startY = 150;

        g.setFont(new Font("Arial", Font.BOLD, 14));

        // Draw from top to bottom (index 0 at top - front of queue)
        for (int i = 0; i < elements.size(); i++) {
            int y = startY + i * (boxHeight + spacing);

            // Draw box
            if (i == 0) { // Front of queue
                g.setColor(new Color(0, 255, 0));
                g.fillRect(startX, y, boxWidth, boxHeight);
                g.setColor(Color.BLACK);
                g.drawString("FRONT →", startX - 90, y + boxHeight / 2 + 5);
            } else if (i == elements.size() - 1) { // Rear
                g.setColor(new Color(255, 100, 100));
                g.fillRect(startX, y, boxWidth, boxHeight);
                g.setColor(Color.BLACK);
                g.drawString("REAR →", startX - 90, y + boxHeight / 2 + 5);
            } else {
                g.setColor(color);
                g.fillRect(startX, y, boxWidth, boxHeight);
            }

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawRect(startX, y, boxWidth, boxHeight);

            // Draw value
            g.setColor(Color.WHITE);
            String val = String.valueOf(elements.get(i));
            FontMetrics fm = g.getFontMetrics();
            int textX = startX + (boxWidth - fm.stringWidth(val)) / 2;
            int textY = y + ((boxHeight - fm.getHeight()) / 2) + fm.getAscent();
            g.drawString(val, textX, textY);

            // Draw index label
            g.setColor(new Color(180, 180, 180));
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString("[" + i + "]", startX + boxWidth + 10, y + boxHeight / 2 + 5);
            g.setFont(new Font("Arial", Font.BOLD, 14));
        }

        // Draw arrows between elements
        g.setColor(new Color(100, 150, 255));
        g.setStroke(new BasicStroke(2));
        for (int i = 0; i < elements.size() - 1; i++) {
            int y1 = startY + i * (boxHeight + spacing) + boxHeight;
            int y2 = startY + (i + 1) * (boxHeight + spacing);
            int arrowX = startX + boxWidth / 2;
            g.drawLine(arrowX, y1, arrowX, y2);
            // Draw arrowhead
            g.fillPolygon(new int[]{arrowX, arrowX - 5, arrowX + 5}, 
                         new int[]{y2, y2 - 8, y2 - 8}, 3);
        }

        // Draw info
        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Size: " + elements.size(), startX, startY + (elements.size() * (boxHeight + spacing)) + 50);
    }
}

/* ============================== LinkedList (Singly) ============================== */
class LinkedListDS extends BaseDataStructure implements LinkedListBase {
    private java.util.Random random = new java.util.Random();
    private java.util.Map<Integer, String> addressMap = new java.util.HashMap<>();

    public LinkedListDS() {
        super();
    }

    @Override
    public void add(int value) {
        super.add(value);
        if (!addressMap.containsKey(elements.size() - 1)) {
            addressMap.put(elements.size() - 1, String.format("%04X", random.nextInt(65536)));
        }
    }

    @Override
    public void insertAt(int value, String position) {
        int index;
        switch (position) {
            case "begin":
                index = 0;
                break;
            case "middle":
                index = elements.size() / 2;
                break;
            case "end":
            default:
                index = elements.size();
                break;
        }
        elements.add(index, value);
        // Regenerate address map
        addressMap.clear();
        for (int i = 0; i < elements.size(); i++) {
            addressMap.put(i, String.format("%04X", random.nextInt(65536)));
        }
    }

    @Override
    public void clear() {
        super.clear();
        addressMap.clear();
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

            // Generate memory address if not exists
            if (!addressMap.containsKey(i)) {
                addressMap.put(i, String.format("%04X", random.nextInt(65536)));
            }
            String address = addressMap.get(i);

            // Draw address label above node
            g.setColor(new Color(200, 200, 200));
            g.setFont(new Font("Courier", Font.BOLD, 11));
            g.drawString(address, x + 20, y - 10);

            // Draw node box (split into data and next)
            int dataWidth = nodeWidth / 2;
            int nextWidth = nodeWidth / 2;

            // Draw DATA section (left half - black background)
            g.setColor(new Color(30, 30, 30));
            g.fillRect(x, y, dataWidth, nodeHeight);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            g.drawRect(x, y, dataWidth, nodeHeight);

            // Draw data label
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 9));
            g.drawString("data", x + 8, y + nodeHeight - 8);

            // Draw data value (centered in data section)
            g.setFont(new Font("Arial", Font.BOLD, 16));
            String val = String.valueOf(elements.get(i));
            FontMetrics fm = g.getFontMetrics();
            int textX = x + (dataWidth - fm.stringWidth(val)) / 2;
            int textY = y + (nodeHeight / 2) + fm.getAscent() / 2;
            g.drawString(val, textX, textY);

            // Draw NEXT pointer section (right half - cyan background)
            g.setColor(new Color(0, 200, 255));
            g.fillRect(x + dataWidth, y, nextWidth, nodeHeight);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawRect(x + dataWidth, y, nextWidth, nodeHeight);

            // Draw next label
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 9));
            g.drawString("next", x + dataWidth + 8, y + nodeHeight - 8);

            // Draw next pointer value or NULL
            g.setFont(new Font("Courier", Font.BOLD, 11));
            String nextVal;
            if (i < elements.size() - 1) {
                nextVal = addressMap.getOrDefault(i + 1, "????");
            } else {
                nextVal = "null";
            }
            fm = g.getFontMetrics();
            textX = x + dataWidth + (nextWidth - fm.stringWidth(nextVal)) / 2;
            textY = y + (nodeHeight / 2) + fm.getAscent() / 2;
            g.drawString(nextVal, textX, textY);

            // Draw pointer arrow to next node
            if (i < elements.size() - 1) {
                int arrowStartX = x + nodeWidth + 5;
                int arrowEndX = startX + (i + 1) * (nodeWidth + spacing) - 5;
                int arrowY = centerY + nodeHeight / 2;

                g.setColor(new Color(100, 150, 255));
                g.setStroke(new BasicStroke(2));
                g.drawLine(arrowStartX, arrowY, arrowEndX, arrowY);
                
                // Draw arrowhead
                int[] xPoints = {arrowEndX, arrowEndX - 10, arrowEndX - 10};
                int[] yPoints = {arrowY, arrowY - 5, arrowY + 5};
                g.fillPolygon(xPoints, yPoints, 3);
                g.setColor(Color.BLACK);
                g.drawPolygon(xPoints, yPoints, 3);
            }
        }

        // Draw head label
        if (!elements.isEmpty()) {
            g.setColor(new Color(255, 200, 0));
            g.setFont(new Font("Arial", Font.BOLD, 11));
            g.drawString("head →", startX - 65, centerY + 15);
        }

        // Draw info
        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Size: " + elements.size(), startX, h - 50);
        g.drawString("Singly LinkedList - One-way traversal", startX, h - 25);
    }
}

/* ============================== Circular LinkedList ============================== */
class CircularLinkedListDS extends BaseDataStructure implements LinkedListBase {
    private java.util.Random random = new java.util.Random();
    private java.util.Map<Integer, String> addressMap = new java.util.HashMap<>();

    public CircularLinkedListDS() { super(); }

    @Override
    public void add(int value) {
        super.add(value);
        if (!addressMap.containsKey(elements.size() - 1)) {
            addressMap.put(elements.size() - 1, String.format("%04X", random.nextInt(65536)));
        }
    }

    @Override
    public void insertAt(int value, String position) {
        int index;
        switch (position) {
            case "begin":
                index = 0;
                break;
            case "middle":
                index = elements.size() / 2;
                break;
            case "end":
            default:
                index = elements.size();
                break;
        }
        elements.add(index, value);
        // Regenerate address map
        addressMap.clear();
        for (int i = 0; i < elements.size(); i++) {
            addressMap.put(i, String.format("%04X", random.nextInt(65536)));
        }
    }

    @Override
    public void clear() {
        super.clear();
        addressMap.clear();
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

            if (!addressMap.containsKey(i)) addressMap.put(i, String.format("%04X", random.nextInt(65536)));
            String address = addressMap.get(i);

            // address label
            g.setColor(new Color(200,200,200));
            g.setFont(new Font("Courier", Font.BOLD, 11));
            g.drawString(address, x + 10, y - 10);

            // draw data section (left - dark)
            int dataW = nodeWidth * 55 / 100;
            int ptrW = nodeWidth - dataW;

            g.setColor(new Color(30,30,30));
            g.fillRect(x, y, dataW, nodeHeight);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            g.drawRect(x, y, dataW, nodeHeight);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            String val = String.valueOf(elements.get(i));
            FontMetrics fm = g.getFontMetrics();
            int tx = x + (dataW - fm.stringWidth(val)) / 2;
            int ty = y + (nodeHeight/2) + fm.getAscent()/2 - 2;
            g.drawString(val, tx, ty);
            g.setFont(new Font("Arial", Font.PLAIN, 9));
            g.drawString("data", x + 6, y + nodeHeight - 6);

            // draw ptr section (right - cyan)
            g.setColor(new Color(0,200,255));
            g.fillRect(x + dataW, y, ptrW, nodeHeight);
            g.setColor(Color.BLACK);
            g.drawRect(x + dataW, y, ptrW, nodeHeight);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 9));
            g.drawString("ptr", x + dataW + 6, y + nodeHeight - 6);

            // ptr address text
            g.setFont(new Font("Courier", Font.BOLD, 11));
            String ptrAddr = (i < elements.size() - 1) ? addressMap.getOrDefault(i+1, "????") : addressMap.getOrDefault(0, "????");
            fm = g.getFontMetrics();
            int pax = x + dataW + (ptrW - fm.stringWidth(ptrAddr)) / 2;
            int pay = y + (nodeHeight/2) + fm.getAscent()/2 - 2;
            g.drawString(ptrAddr, pax, pay);

            // arrow to next node (simple line)
            if (i < elements.size() - 1) {
                int ax = x + nodeWidth + 6;
                int ay = y + nodeHeight/2;
                int bx = startX + (i+1)*(nodeWidth+spacing) - 6;
                int by = ay;
                g.setColor(new Color(100,150,255));
                g.setStroke(new BasicStroke(2));
                g.drawLine(ax, ay, bx, by);
                g.fillPolygon(new int[]{bx, bx-10, bx-10}, new int[]{by, by-6, by+6}, 3);
            }
        }

        // Draw the rectangular wrap-around arrow from last node back to first (like reference image)
        int lastRight = startX + (elements.size()-1)*(nodeWidth+spacing) + nodeWidth;
        int arrowTop = centerY + nodeHeight/2;
        int wrapDown = centerY + nodeHeight + 70;
        int leftX = startX - 50;

        g.setColor(new Color(0,200,100));
        g.setStroke(new BasicStroke(3));

        // vertical from last node to bottom
        int sx = lastRight + 10;
        int sy = arrowTop;
        g.drawLine(sx, sy, sx, wrapDown);
        // bottom horizontal to leftX
        g.drawLine(sx, wrapDown, leftX, wrapDown);
        // up to just above first node
    int upY = centerY - 20;
        g.drawLine(leftX, wrapDown, leftX, upY);
        // horizontal right to just left of first node's ptr
        int toX = startX - 6;
        g.drawLine(leftX, upY, toX, upY);
        // arrowhead pointing right into first node
        g.fillPolygon(new int[]{toX, toX-8, toX-8}, new int[]{upY, upY-6, upY+6}, 3);

        // Draw HEAD label with small arrow down to first node
        g.setColor(new Color(255,200,0));
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("HEAD", startX - 70, centerY + 10);
        // small arrow from HEAD to first node
        g.setColor(new Color(255,200,0));
        g.setStroke(new BasicStroke(2));
        g.drawLine(startX - 20, centerY + 12, startX + 6, centerY + 12);
        g.fillPolygon(new int[]{startX + 6, startX, startX}, new int[]{centerY + 12, centerY + 8, centerY + 16}, 3);

        // Info
        g.setColor(new Color(150,150,150));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Size: " + elements.size(), startX, h - 50);
        g.drawString("Circular LinkedList - linear layout with wrap arrow", startX, h - 25);
    }
}

/* ============================== Doubly LinkedList ============================== */
class DoublyLinkedListDS extends BaseDataStructure implements LinkedListBase {
    private java.util.Random random = new java.util.Random();
    private java.util.Map<Integer, String> addressMap = new java.util.HashMap<>();

    public DoublyLinkedListDS() {
        super();
    }

    @Override
    public void add(int value) {
        super.add(value);
        if (!addressMap.containsKey(elements.size() - 1)) {
            addressMap.put(elements.size() - 1, String.format("%04X", random.nextInt(65536)));
        }
    }

    @Override
    public void insertAt(int value, String position) {
        int index;
        switch (position) {
            case "begin":
                index = 0;
                break;
            case "middle":
                index = elements.size() / 2;
                break;
            case "end":
            default:
                index = elements.size();
                break;
        }
        elements.add(index, value);
        // Regenerate address map
        addressMap.clear();
        for (int i = 0; i < elements.size(); i++) {
            addressMap.put(i, String.format("%04X", random.nextInt(65536)));
        }
    }

    @Override
    public void clear() {
        super.clear();
        addressMap.clear();
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

            // Generate memory address if not exists
            if (!addressMap.containsKey(i)) {
                addressMap.put(i, String.format("%04X", random.nextInt(65536)));
            }
            String address = addressMap.get(i);

            // Draw address label above node
            g.setColor(new Color(200, 200, 200));
            g.setFont(new Font("Courier", Font.BOLD, 11));
            g.drawString(address, x + 30, y - 10);

            // Node has 3 sections: prev (left, orange), data (center, black), next (right, cyan)
            int prevWidth = 30;
            int dataWidth = 60;
            int nextWidth = 30;

            // Draw PREV pointer (left section - orange)
            g.setColor(new Color(255, 150, 100));
            g.fillRect(x, y, prevWidth, nodeHeight);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawRect(x, y, prevWidth, nodeHeight);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 8));
            g.drawString("prev", x + 2, y + nodeHeight - 8);

            // Draw DATA section (center - black)
            g.setColor(new Color(30, 30, 30));
            g.fillRect(x + prevWidth, y, dataWidth, nodeHeight);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            g.drawRect(x + prevWidth, y, dataWidth, nodeHeight);

            // Draw data label
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 9));
            g.drawString("data", x + prevWidth + 15, y + nodeHeight - 8);

            // Draw data value (centered)
            g.setFont(new Font("Arial", Font.BOLD, 16));
            String val = String.valueOf(elements.get(i));
            FontMetrics fm = g.getFontMetrics();
            int textX = x + prevWidth + (dataWidth - fm.stringWidth(val)) / 2;
            int textY = y + (nodeHeight / 2) + fm.getAscent() / 2;
            g.drawString(val, textX, textY);

            // Draw NEXT pointer section (right - cyan)
            g.setColor(new Color(0, 200, 255));
            g.fillRect(x + prevWidth + dataWidth, y, nextWidth, nodeHeight);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawRect(x + prevWidth + dataWidth, y, nextWidth, nodeHeight);

            // Draw next label
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 8));
            g.drawString("next", x + prevWidth + dataWidth + 2, y + nodeHeight - 8);

            // Draw pointers with arrows
            // Forward pointer (to next node)
            if (i < elements.size() - 1) {
                int nextNodeX = startX + (i + 1) * (nodeWidth + spacing);
                g.setColor(new Color(0, 200, 255));
                g.setStroke(new BasicStroke(2));
                g.drawLine(x + nodeWidth + 5, y + nodeHeight / 2, nextNodeX - 5, y + nodeHeight / 2);
                g.fillPolygon(new int[]{nextNodeX - 5, nextNodeX - 13, nextNodeX - 13}, 
                             new int[]{y + nodeHeight / 2, y + nodeHeight / 2 - 5, y + nodeHeight / 2 + 5}, 3);
            }

            // Backward pointer (to previous node)
            if (i > 0) {
                int prevNodeX = startX + (i - 1) * (nodeWidth + spacing);
                g.setColor(new Color(255, 150, 100));
                g.setStroke(new BasicStroke(2));
                g.drawLine(x - 5, y - 15, prevNodeX + nodeWidth + 5, y - 15);
                g.fillPolygon(new int[]{x - 5, x + 3, x + 3}, 
                             new int[]{y - 15, y - 20, y - 10}, 3);
            }
        }

        // Draw head label
        if (!elements.isEmpty()) {
            g.setColor(new Color(255, 200, 0));
            g.setFont(new Font("Arial", Font.BOLD, 11));
            g.drawString("head →", startX - 55, centerY + 15);
        }

        // Draw legend
        g.setColor(new Color(255, 150, 100));
        g.fillRect(50, h - 70, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString("Prev Pointer", 70, h - 62);

        g.setColor(new Color(0, 200, 255));
        g.fillRect(200, h - 70, 10, 10);
        g.drawString("Next Pointer", 220, h - 62);

        // Draw info
        g.setColor(new Color(150, 150, 150));
        g.drawString("Size: " + elements.size(), startX, h - 35);
        g.drawString("Doubly LinkedList - Two-way traversal", startX, h - 12);
    }
}
