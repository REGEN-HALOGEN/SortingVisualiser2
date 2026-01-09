import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

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
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default
        }
        SwingUtilities.invokeLater(() -> new HubFrame().setVisible(true));
    }
}

class HubFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private final JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));
    private final JLabel titleLabel = new JLabel("Visualizer Hub");

    // Modern color palette
    private static final Color BG_DARK = new Color(18, 18, 24);
    private static final Color BG_CARD = new Color(28, 28, 38);
    private static final Color BG_NAV = new Color(22, 22, 30);
    private static final Color ACCENT_BLUE = new Color(99, 102, 241);
    private static final Color ACCENT_GREEN = new Color(16, 185, 129);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color ACCENT_PURPLE = new Color(139, 92, 246);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color BORDER_COLOR = new Color(51, 65, 85);

    // Reference to visualizer windows
    private JFrame sortingVisualizerWindow;
    private JFrame dataStructureVisualizerWindow;

    public HubFrame() {
        super("Visualizer Hub - Sorting & Data Structures");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);

        // Setup main content area with CardLayout
        mainPanel.setBackground(BG_DARK);
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
        JPanel panel = new GradientPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Icon/Logo area
        JLabel iconLabel = new JLabel("⚡");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setForeground(ACCENT_PURPLE);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(iconLabel, gbc);

        // Title
        JLabel mainTitle = new JLabel("Visualizer Hub");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 48));
        mainTitle.setForeground(TEXT_PRIMARY);
        mainTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        panel.add(mainTitle, gbc);

        // Subtitle
        JLabel subtitle = new JLabel("Interactive Algorithm & Data Structure Visualization");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 20, 40, 20);
        panel.add(subtitle, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(20, 30, 20, 15);

        // Sorting Visualizer card
        JPanel sortingCard = createModernCard(
                "📊",
                "Sorting Visualizer",
                "Bubble • Selection • Insertion • Merge\nQuick • Heap • Shell • Radix Sort",
                ACCENT_BLUE,
                new Color(99, 102, 241, 40));
        sortingCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openSortingVisualizer();
            }
        });
        panel.add(sortingCard, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(20, 15, 20, 30);

        // Data Structure Visualizer card
        JPanel dsCard = createModernCard(
                "🔗",
                "Data Structure Visualizer",
                "Stack • Queue • LinkedList\nCircular • Doubly Linked List",
                ACCENT_GREEN,
                new Color(16, 185, 129, 40));
        dsCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openDataStructureVisualizer();
            }
        });
        panel.add(dsCard, gbc);

        // Footer info
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(40, 20, 10, 20);
        JLabel footer = new JLabel("SIES College of Management Studies • MCA 2025-27");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.setForeground(new Color(100, 116, 139));
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(footer, gbc);

        return panel;
    }

    private JPanel createModernCard(String icon, String title, String description, Color accentColor, Color bgTint) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw rounded background
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

                // Draw gradient overlay
                GradientPaint gp = new GradientPaint(0, 0, bgTint, getWidth(), getHeight(), new Color(0, 0, 0, 0));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, BORDER_COLOR),
                BorderFactory.createEmptyBorder(30, 35, 30, 35)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(340, 220));
        card.setOpaque(false);

        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        // Description
        JLabel descLabel = new JLabel("<html><center>" + description.replace("\n", "<br>") + "</center></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(descLabel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        // Launch button hint
        JLabel launchHint = new JLabel("Click to launch →");
        launchHint.setFont(new Font("Segoe UI", Font.BOLD, 12));
        launchHint.setForeground(accentColor);
        launchHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(launchHint);

        // Hover effects
        Color originalBg = BG_CARD;
        Color hoverBg = new Color(38, 38, 52);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(hoverBg);
                card.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(20, accentColor),
                        BorderFactory.createEmptyBorder(30, 35, 30, 35)));
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(originalBg);
                card.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(20, BORDER_COLOR),
                        BorderFactory.createEmptyBorder(30, 35, 30, 35)));
                card.repaint();
            }
        });

        return card;
    }

    private void setupNavigationPanel() {
        navigationPanel.setBackground(BG_NAV);
        navigationPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Logo/Brand
        JLabel brandLabel = new JLabel("⚡ VizHub");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(ACCENT_PURPLE);
        navigationPanel.add(brandLabel);
        navigationPanel.add(Box.createHorizontalStrut(30));

        // Buttons
        JButton homeBtn = createModernNavButton("🏠 Home", ACCENT_BLUE);
        homeBtn.addActionListener(e -> {
            cardLayout.show(mainPanel, "welcome");
            titleLabel.setText("Visualizer Hub");
        });

        JButton sortingBtn = createModernNavButton("📊 Sorting", ACCENT_BLUE);
        sortingBtn.addActionListener(e -> openSortingVisualizer());

        JButton dsBtn = createModernNavButton("🔗 Data Structures", ACCENT_GREEN);
        dsBtn.addActionListener(e -> openDataStructureVisualizer());

        JButton exitBtn = createModernNavButton("❌ Exit", ACCENT_RED);
        exitBtn.addActionListener(e -> System.exit(0));

        navigationPanel.add(homeBtn);
        navigationPanel.add(sortingBtn);
        navigationPanel.add(dsBtn);
        navigationPanel.add(Box.createHorizontalStrut(20));
        navigationPanel.add(exitBtn);
    }

    private JButton createModernNavButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(color.brighter());
                } else {
                    g2.setColor(color);
                }
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(150, 38));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private void openSortingVisualizer() {
        if (sortingVisualizerWindow == null || !sortingVisualizerWindow.isDisplayable()) {
            sortingVisualizerWindow = new VisualFrame();
            sortingVisualizerWindow.setTitle("Sorting Visualizer");
            sortingVisualizerWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            sortingVisualizerWindow.setLocationRelativeTo(null); // Center on screen
            sortingVisualizerWindow.setVisible(true);
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
            dataStructureVisualizerWindow.setLocationRelativeTo(null); // Center on screen
            dataStructureVisualizerWindow.setVisible(true);
        } else {
            dataStructureVisualizerWindow.toFront();
            dataStructureVisualizerWindow.requestFocus();
        }
        titleLabel.setText("Visualizer Hub - Data Structure Visualizer Active");
    }
}

// Gradient background panel
class GradientPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Dark gradient background
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(18, 18, 24),
                getWidth(), getHeight(), new Color(30, 27, 45));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Subtle decorative circles
        g2.setColor(new Color(99, 102, 241, 15));
        g2.fillOval(-100, -100, 400, 400);
        g2.setColor(new Color(16, 185, 129, 10));
        g2.fillOval(getWidth() - 200, getHeight() - 200, 400, 400);

        g2.dispose();
    }
}

// Custom rounded border
class RoundedBorder implements Border {
    private int radius;
    private Color color;

    public RoundedBorder(int radius, Color color) {
        this.radius = radius;
        this.color = color;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(2, 2, 2, 2);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2));
        g2.draw(new RoundRectangle2D.Double(x + 1, y + 1, width - 3, height - 3, radius, radius));
        g2.dispose();
    }
}
