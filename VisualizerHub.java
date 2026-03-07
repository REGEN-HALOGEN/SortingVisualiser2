import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
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
    private final JPanel navigationPanel = new JPanel(new BorderLayout());
    private final List<ModernCardPanel> cards = new ArrayList<>();

    private ThemePalette theme = ThemePalette.light();
    private boolean darkMode = false;

    private GradientPanel welcomePanel;
    private JLabel eyebrowLabel;
    private JLabel mainTitleLabel;
    private JLabel subtitleLabel;
    private JLabel footerLabel;
    private JLabel builtByLabel;
    private JLabel githubLinkLabel;
    private JButton themeToggleBtn;
    private JButton exitBtn;
    private boolean githubLinkHovered;

    private JFrame sortingVisualizerWindow;
    private JFrame dataStructureVisualizerWindow;

    public HubFrame() {
        super("Visualizer Hub - Sorting & Data Structures");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        mainPanel.setBackground(theme.background);
        setupMainPanel();
        setupNavigationPanel();

        add(navigationPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        cardLayout.show(mainPanel, "welcome");
        applyTheme();
    }

    private void setupMainPanel() {
        welcomePanel = createWelcomePanel();
        mainPanel.add(welcomePanel, "welcome");
    }

    private GradientPanel createWelcomePanel() {
        GradientPanel panel = new GradientPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalStrut(1), gbc);

        eyebrowLabel = new JLabel("SORTING • DATA STRUCTURES");
        eyebrowLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        eyebrowLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        panel.add(eyebrowLabel, gbc);

        mainTitleLabel = new JLabel("Visualizer Hub");
        mainTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        mainTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 20, 8, 20);
        panel.add(mainTitleLabel, gbc);

        subtitleLabel = new JLabel("Simple, focused visual tools for learning algorithms and data structures.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 3;
        gbc.insets = new Insets(6, 20, 28, 20);
        panel.add(subtitleLabel, gbc);

        ModernCardPanel sortingCard = new ModernCardPanel(
                "Sorting Visualizer",
                "8 algorithms with step playback, code view, stats, and speed control.",
                "Bubble • Selection • Insertion • Merge • Quick • Heap • Shell • Radix",
                new Color(52, 52, 52),
                new Color(0, 0, 0, 10));
        sortingCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openSortingVisualizer();
            }
        });
        cards.add(sortingCard);

        ModernCardPanel dsCard = new ModernCardPanel(
                "Data Structure Visualizer",
                "Interactive operations, node inspection, smooth animations, and operation code view.",
                "Stack • Queue • Deque • Linked List • Circular • Doubly Linked List",
                new Color(72, 72, 72),
                new Color(0, 0, 0, 12));
        dsCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openDataStructureVisualizer();
            }
        });
        cards.add(dsCard);

        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.add(sortingCard);
        cardsPanel.add(dsCard);

        gbc.gridy = 4;
        gbc.insets = new Insets(10, 20, 0, 20);
        panel.add(cardsPanel, gbc);

        footerLabel = new JLabel("Built with Java Swing");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        footerLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, footerLabel.getPreferredSize().height));

        builtByLabel = new JLabel("Built by REGEN-HALOGEN");
        builtByLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        builtByLabel.setHorizontalAlignment(SwingConstants.CENTER);
        builtByLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        builtByLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, builtByLabel.getPreferredSize().height));

        githubLinkLabel = new JLabel("https://github.com/REGEN-HALOGEN");
        githubLinkLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        githubLinkLabel.setHorizontalAlignment(SwingConstants.CENTER);
        githubLinkLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        githubLinkLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, githubLinkLabel.getPreferredSize().height));
        githubLinkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        githubLinkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openGithubProfile();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                githubLinkHovered = true;
                updateGithubLinkLabel();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                githubLinkHovered = false;
                updateGithubLinkLabel();
            }
        });

        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.add(footerLabel);
        footerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        footerPanel.add(builtByLabel);
        footerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        footerPanel.add(githubLinkLabel);

        gbc.gridy = 5;
        gbc.insets = new Insets(30, 20, 16, 20);
        panel.add(footerPanel, gbc);

        gbc.gridy = 6;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 20, 0, 20);
        panel.add(Box.createVerticalStrut(1), gbc);

        updateGithubLinkLabel();

        return panel;
    }

    private void updateGithubLinkLabel() {
        if (githubLinkLabel == null) {
            return;
        }
        String decoration = githubLinkHovered ? "text-decoration:underline;" : "text-decoration:none;";
        githubLinkLabel.setText("<html><div style='text-align:center; color:" + toHex(theme.link) + "; " + decoration
                + "'>https://github.com/REGEN-HALOGEN</div></html>");
    }

    private void setupNavigationPanel() {
        navigationPanel.setPreferredSize(new Dimension(0, 70));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
        leftPanel.setOpaque(false);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        rightPanel.setOpaque(false);

        themeToggleBtn = createThemeToggleButton();
        leftPanel.add(themeToggleBtn);

        exitBtn = createModernNavButton("Exit", theme.exitAccent, false);
        exitBtn.addActionListener(e -> System.exit(0));
        rightPanel.add(exitBtn);

        navigationPanel.add(leftPanel, BorderLayout.WEST);
        navigationPanel.add(rightPanel, BorderLayout.EAST);
    }

    private JButton createThemeToggleButton() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color fill = getModel().isPressed()
                        ? theme.toggleFill.darker()
                        : (getModel().isRollover() ? theme.toggleHover : theme.toggleFill);

                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 18, 18));

                g2.setColor(theme.toggleBorder);
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(new RoundRectangle2D.Double(0.7, 0.7, getWidth() - 1.4, getHeight() - 1.4, 18, 18));

                String icon = darkMode ? "☀" : "☾";
                g2.setColor(theme.textPrimary);
                g2.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(icon)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1;
                g2.drawString(icon, x, y);
                g2.dispose();
            }
        };
        btn.setToolTipText("Toggle light and dark mode");
        btn.setPreferredSize(new Dimension(48, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> toggleTheme());
        return btn;
    }

    private JButton createModernNavButton(String text, Color color, boolean filled) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color bgColor;
                if (getModel().isPressed()) {
                    bgColor = color.darker();
                } else if (getModel().isRollover()) {
                    bgColor = filled ? color.brighter() : new Color(color.getRed(), color.getGreen(), color.getBlue(), 70);
                } else {
                    bgColor = filled ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), 28);
                }

                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));

                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), filled ? 0 : 110));
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(new RoundRectangle2D.Double(0.7, 0.7, getWidth() - 1.4, getHeight() - 1.4, 14, 14));

                g2.setColor(theme.textPrimary);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(110, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void openSortingVisualizer() {
        if (sortingVisualizerWindow == null || !sortingVisualizerWindow.isDisplayable()) {
            sortingVisualizerWindow = SortingVisualiser.createFrame();
            sortingVisualizerWindow.setTitle("Sorting Visualizer");
            sortingVisualizerWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            sortingVisualizerWindow.setLocationRelativeTo(null);
            sortingVisualizerWindow.setVisible(true);
        } else {
            sortingVisualizerWindow.toFront();
            sortingVisualizerWindow.requestFocus();
        }
    }

    private void openDataStructureVisualizer() {
        if (dataStructureVisualizerWindow == null || !dataStructureVisualizerWindow.isDisplayable()) {
            dataStructureVisualizerWindow = DataStructureVisualizer.createFrame();
            dataStructureVisualizerWindow.setTitle("Data Structure Visualizer");
            dataStructureVisualizerWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            dataStructureVisualizerWindow.setLocationRelativeTo(null);
            dataStructureVisualizerWindow.setVisible(true);
        } else {
            dataStructureVisualizerWindow.toFront();
            dataStructureVisualizerWindow.requestFocus();
        }
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        theme = darkMode ? ThemePalette.dark() : ThemePalette.light();
        applyTheme();
    }

    private void applyTheme() {
        getContentPane().setBackground(theme.background);
        mainPanel.setBackground(theme.background);
        navigationPanel.setBackground(theme.navBackground);
        navigationPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, theme.border));

        if (welcomePanel != null) {
            welcomePanel.setPalette(theme);
        }
        if (eyebrowLabel != null) {
            eyebrowLabel.setForeground(theme.eyebrow);
        }
        if (mainTitleLabel != null) {
            mainTitleLabel.setForeground(theme.textPrimary);
        }
        if (subtitleLabel != null) {
            subtitleLabel.setForeground(theme.textSecondary);
        }
        if (footerLabel != null) {
            footerLabel.setForeground(theme.footer);
        }
        if (builtByLabel != null) {
            builtByLabel.setForeground(theme.footer);
        }
        if (githubLinkLabel != null) {
            updateGithubLinkLabel();
        }
        if (themeToggleBtn != null) {
            themeToggleBtn.repaint();
        }
        if (exitBtn != null) {
            exitBtn.repaint();
        }
        for (ModernCardPanel card : cards) {
            card.applyTheme(theme);
        }

        repaint();
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private void openGithubProfile() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new java.net.URI("https://github.com/REGEN-HALOGEN"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to open the GitHub link.",
                    "Link Error",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    static final class ThemePalette {
        final Color background;
        final Color navBackground;
        final Color panelOuter;
        final Color panelInner;
        final Color cardBackground;
        final Color cardHover;
        final Color border;
        final Color textPrimary;
        final Color textSecondary;
        final Color eyebrow;
        final Color footer;
        final Color link;
        final Color toggleFill;
        final Color toggleHover;
        final Color toggleBorder;
        final Color exitAccent;

        ThemePalette(Color background,
                Color navBackground,
                Color panelOuter,
                Color panelInner,
                Color cardBackground,
                Color cardHover,
                Color border,
                Color textPrimary,
                Color textSecondary,
                Color eyebrow,
                Color footer,
                Color link,
                Color toggleFill,
                Color toggleHover,
                Color toggleBorder,
                Color exitAccent) {
            this.background = background;
            this.navBackground = navBackground;
            this.panelOuter = panelOuter;
            this.panelInner = panelInner;
            this.cardBackground = cardBackground;
            this.cardHover = cardHover;
            this.border = border;
            this.textPrimary = textPrimary;
            this.textSecondary = textSecondary;
            this.eyebrow = eyebrow;
            this.footer = footer;
            this.link = link;
            this.toggleFill = toggleFill;
            this.toggleHover = toggleHover;
            this.toggleBorder = toggleBorder;
            this.exitAccent = exitAccent;
        }

        static ThemePalette light() {
            return new ThemePalette(
                    new Color(245, 245, 245),
                    Color.WHITE,
                    new Color(0, 0, 0, 10),
                    new Color(255, 255, 255, 235),
                    Color.WHITE,
                    new Color(248, 248, 248),
                    new Color(220, 220, 220),
                    new Color(16, 16, 16),
                    new Color(110, 110, 110),
                    new Color(90, 90, 90),
                    new Color(120, 120, 120),
                    new Color(37, 99, 235),
                    Color.WHITE,
                    new Color(245, 245, 245),
                    new Color(220, 220, 220),
                    new Color(239, 68, 68));
        }

        static ThemePalette dark() {
            return new ThemePalette(
                    new Color(15, 15, 17),
                    new Color(18, 18, 20),
                    new Color(255, 255, 255, 14),
                    new Color(19, 19, 23, 238),
                    new Color(24, 24, 29),
                    new Color(30, 30, 36),
                    new Color(64, 64, 74),
                    new Color(244, 244, 246),
                    new Color(170, 170, 178),
                    new Color(186, 186, 194),
                    new Color(142, 142, 150),
                    new Color(125, 170, 255),
                    new Color(24, 24, 30),
                    new Color(35, 35, 42),
                    new Color(78, 78, 88),
                    new Color(239, 68, 68));
        }
    }

    private final class ModernCardPanel extends JPanel {
        private final String description;
        private final String meta;
        private final Color accentColor;
        private final Color bgTint;
        private final JPanel accentBar = new JPanel();
        private final JLabel titleLabel = new JLabel();
        private final JLabel descLabel = new JLabel();
        private final JLabel metaLabel = new JLabel();
        private final JLabel launchHint = new JLabel("Open →");
        private boolean hovered;

        private ModernCardPanel(String title, String description, String meta, Color accentColor, Color bgTint) {
            this.description = description;
            this.meta = meta;
            this.accentColor = accentColor;
            this.bgTint = bgTint;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(380, 250));
            setMinimumSize(new Dimension(380, 250));
            setMaximumSize(new Dimension(380, 250));
            setOpaque(false);

            accentBar.setMaximumSize(new Dimension(52, 6));
            accentBar.setPreferredSize(new Dimension(52, 6));
            accentBar.setOpaque(true);
            accentBar.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(accentBar);
            add(Box.createRigidArea(new Dimension(0, 22)));

            titleLabel.setText(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 23));
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(titleLabel);
            add(Box.createRigidArea(new Dimension(0, 10)));

            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(descLabel);
            add(Box.createRigidArea(new Dimension(0, 16)));

            metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(metaLabel);
            add(Box.createVerticalGlue());

            launchHint.setFont(new Font("Segoe UI", Font.BOLD, 13));
            launchHint.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(Box.createRigidArea(new Dimension(0, 16)));
            add(launchHint);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    applyTheme(theme);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    applyTheme(theme);
                }
            });

            applyTheme(theme);
        }

        private void applyTheme(ThemePalette palette) {
            setBackground(hovered ? palette.cardHover : palette.cardBackground);
            setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(24, hovered ? palette.toggleBorder : palette.border),
                    BorderFactory.createEmptyBorder(28, 30, 28, 30)));
            accentBar.setBackground(accentColor);
            titleLabel.setForeground(palette.textPrimary);
            descLabel.setForeground(palette.textSecondary);
            metaLabel.setForeground(palette.textSecondary);
            launchHint.setForeground(accentColor);
            descLabel.setText("<html><div style='width:300px; line-height:1.45;'>" + description + "</div></html>");
            metaLabel.setText("<html><div style='width:300px; color:" + toHex(palette.textSecondary)
                    + "; line-height:1.4;'>" + meta + "</div></html>");
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));

            GradientPaint gp = new GradientPaint(0, 0, bgTint, getWidth(), getHeight(), new Color(0, 0, 0, 0));
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));

            g2.setColor(darkMode ? new Color(255, 255, 255, 18) : new Color(0, 0, 0, 18));
            g2.fill(new RoundRectangle2D.Double(18, 18, 72, 5, 5, 5));
            g2.dispose();
        }
    }
}

class GradientPanel extends JPanel {
    private HubFrame.ThemePalette palette = HubFrame.ThemePalette.light();

    public void setPalette(HubFrame.ThemePalette palette) {
        this.palette = palette;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        GradientPaint gp = new GradientPaint(
                0, 0, palette.background,
                getWidth(), getHeight(), palette.navBackground);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(palette.panelOuter);
        g2.fillRoundRect(60, 50, getWidth() - 120, Math.max(320, getHeight() - 120), 36, 36);

        g2.setColor(palette.panelInner);
        g2.fillRoundRect(61, 51, getWidth() - 122, Math.max(318, getHeight() - 122), 36, 36);

        g2.dispose();
    }
}

class RoundedBorder implements Border {
    private final int radius;
    private final Color color;

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
