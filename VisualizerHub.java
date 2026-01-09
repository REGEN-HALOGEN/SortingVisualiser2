import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * VisualizerHub.java
 * Main hub to switch between SortingVisualiser and DataStructureVisualizer.
 * 
 * Compile:
 * javac VisualizerHub.java SortingVisualiser.java DataStructureVisualizer.java
 * 
 * Run:
 * java VisualizerHub
 * 
 * Uses: Java 8+
 */

public class VisualizerHub {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HubFrame().setVisible(true));
    }
}

class HubFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private final JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
    private final JLabel titleLabel = new JLabel("Visualizer Hub");

    // Reference to visualizer windows
    private JFrame sortingVisualizerWindow;
    private JFrame dataStructureVisualizerWindow;

    public HubFrame() {
        super("Visualizer Hub - Sorting & Data Structures");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Setup main content area with CardLayout
        mainPanel.setBackground(new Color(30, 30, 30));
        setupMainPanel();

        // Setup navigation
        setupNavigationPanel();

        // Add to frame
        add(navigationPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Show initial welcome screen
        cardLayout.show(mainPanel, "welcome");
    }

    private void setupMainPanel() {
        // Welcome panel
        JPanel welcomePanel = createWelcomePanel();
        mainPanel.add(welcomePanel, "welcome");
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel mainTitle = new JLabel("Welcome to Visualizer Hub");
        mainTitle.setFont(new Font("Arial", Font.BOLD, 36));
        mainTitle.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(mainTitle, gbc);

        // Subtitle
        JLabel subtitle = new JLabel("Choose a visualizer to explore algorithms and data structures");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(150, 150, 150));
        gbc.gridy = 1;
        panel.add(subtitle, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(30, 20, 20, 20);

        // Sorting Visualizer card
        JPanel sortingCard = createVisualizerCard(
                "Sorting Visualizer",
                "Visualize popular sorting algorithms\nBubble, Selection, Insertion, Merge, Quick, Heap, Shell, Radix",
                new Color(52, 152, 219)
        );
        sortingCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openSortingVisualizer();
            }
        });
        panel.add(sortingCard, gbc);

        gbc.gridx = 1;
        // Data Structure Visualizer card
        JPanel dsCard = createVisualizerCard(
                "Data Structure Visualizer",
                "Visualize data structures in action\nStack, Queue, LinkedList, Circular, Doubly Linked",
                new Color(46, 204, 113)
        );
        dsCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openDataStructureVisualizer();
            }
        });
        panel.add(dsCard, gbc);

        return panel;
    }

    private JPanel createVisualizerCard(String title, String description, Color accentColor) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 10));
        card.setBackground(new Color(50, 50, 50));
        card.setBorder(BorderFactory.createLineBorder(accentColor, 3));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(300, 200));

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(accentColor);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Description
        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        descLabel.setForeground(new Color(150, 150, 150));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel);
        card.add(descLabel);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(70, 70, 70));
                card.setBorder(BorderFactory.createLineBorder(accentColor, 4));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(new Color(50, 50, 50));
                card.setBorder(BorderFactory.createLineBorder(accentColor, 3));
            }
        });

        return card;
    }

    private void setupNavigationPanel() {
        navigationPanel.setBackground(new Color(40, 40, 40));
        navigationPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(70, 70, 70)));

        // Title
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        // Buttons
        JButton homeBtn = createNavButton("🏠 Home", new Color(52, 152, 219));
        homeBtn.addActionListener(e -> {
            cardLayout.show(mainPanel, "welcome");
            titleLabel.setText("Visualizer Hub");
        });

        JButton sortingBtn = createNavButton("📊 Sorting Visualizer", new Color(52, 152, 219));
        sortingBtn.addActionListener(e -> openSortingVisualizer());

        JButton dsBtn = createNavButton("🔗 Data Structure Visualizer", new Color(46, 204, 113));
        dsBtn.addActionListener(e -> openDataStructureVisualizer());

        JButton exitBtn = createNavButton("❌ Exit", new Color(231, 76, 60));
        exitBtn.addActionListener(e -> System.exit(0));

        navigationPanel.add(homeBtn);
        navigationPanel.add(sortingBtn);
        navigationPanel.add(dsBtn);
        navigationPanel.add(exitBtn);
    }

    private JButton createNavButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
            }
        });

        return btn;
    }

    private void openSortingVisualizer() {
        if (sortingVisualizerWindow == null || !sortingVisualizerWindow.isDisplayable()) {
            sortingVisualizerWindow = new VisualFrame();
            sortingVisualizerWindow.setTitle("Sorting Visualizer");
            sortingVisualizerWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            sortingVisualizerWindow.setVisible(true);

            // Position next to hub
            int hubX = this.getX();
            int hubY = this.getY();
            sortingVisualizerWindow.setLocation(hubX + this.getWidth() + 10, hubY);
        } else {
            sortingVisualizerWindow.toFront();
            sortingVisualizerWindow.requestFocus();
        }
        titleLabel.setText("Visualizer Hub - Sorting Visualizer Active");
    }

    private void openDataStructureVisualizer() {
        if (dataStructureVisualizerWindow == null || !dataStructureVisualizerWindow.isDisplayable()) {
            dataStructureVisualizerWindow = new DSVisualizerFrame();
            dataStructureVisualizerWindow.setTitle("Data Structure Visualizer");
            dataStructureVisualizerWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            dataStructureVisualizerWindow.setVisible(true);

            // Position next to hub
            int hubX = this.getX();
            int hubY = this.getY();
            dataStructureVisualizerWindow.setLocation(hubX + this.getWidth() + 10, hubY);
        } else {
            dataStructureVisualizerWindow.toFront();
            dataStructureVisualizerWindow.requestFocus();
        }
        titleLabel.setText("Visualizer Hub - Data Structure Visualizer Active");
    }
}
