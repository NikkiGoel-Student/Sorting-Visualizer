import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SortingAlgorithmVisualizer extends JFrame {
    private int[] array;
    private JPanel drawPanel;
    private JSlider speedSlider, sizeSlider;
    private JComboBox<String> algorithmComboBox;
    private JButton startButton, pauseButton, stopButton, generateButton;
    private AtomicBoolean isPaused, isStopped;
    private Thread sortingThread;
    private JLabel statusLabel, comparisonsLabel, swapsLabel;
    private int comparisons = 0, swaps = 0;
    private int[] highlightedIndices = new int[2]; // For highlighting compared elements
    private boolean showNumbers = false;
    private JCheckBox showNumbersCheckbox;
    private long startTime;

    // Enhanced color scheme
    private static final Color BACKGROUND_COLOR = new Color(20, 25, 35);
    private static final Color PANEL_COLOR = new Color(35, 40, 55);
    private static final Color ACCENT_COLOR = new Color(100, 200, 255);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color TEXT_COLOR = new Color(240, 245, 255);
    private static final Color HIGHLIGHT_COLOR = new Color(255, 193, 7);

    public SortingAlgorithmVisualizer() {
        initializeUI();
        setupEventListeners();
        generateArray();
        Arrays.fill(highlightedIndices, -1);
    }

    private void initializeUI() {
        setTitle("Sorting Algorithms Visualizer");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Create main drawing panel with enhanced styling
        drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawArray(g2d);
            }
        };
        drawPanel.setBackground(BACKGROUND_COLOR);
        drawPanel.setPreferredSize(new Dimension(1400, 600));
        drawPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(drawPanel, BorderLayout.CENTER);

        // Create enhanced control panel
        JPanel mainControlPanel = createControlPanel();
        add(mainControlPanel, BorderLayout.SOUTH);

        // Create status panel
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.NORTH);
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(PANEL_COLOR);
        statusPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Title label
        JLabel titleLabel = new JLabel("Sorting Algorithms Visualizer", SwingConstants.CENTER);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        statusPanel.add(titleLabel, BorderLayout.CENTER);

        // Stats panel
        JPanel statsPanel = new JPanel(new FlowLayout());
        statsPanel.setBackground(PANEL_COLOR);

        statusLabel = createStyledLabel("Ready to sort", SUCCESS_COLOR);
        comparisonsLabel = createStyledLabel("Comparisons: 0", ACCENT_COLOR);
        swapsLabel = createStyledLabel("Swaps: 0", ACCENT_COLOR);

        statsPanel.add(statusLabel);
        statsPanel.add(Box.createHorizontalStrut(30));
        statsPanel.add(comparisonsLabel);
        statsPanel.add(Box.createHorizontalStrut(30));
        statsPanel.add(swapsLabel);

        statusPanel.add(statsPanel, BorderLayout.EAST);
        return statusPanel;
    }

    private JPanel createControlPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(PANEL_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top controls
        JPanel topControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        topControls.setBackground(PANEL_COLOR);

        // Array size controls
        JPanel sizePanel = createControlGroup("Array Size");
        sizeSlider = new JSlider(10, 200, 50);
        styleSlider(sizeSlider);
        sizePanel.add(sizeSlider);
        topControls.add(sizePanel);

        // Speed controls
        JPanel speedPanel = createControlGroup("Animation Speed");
        speedSlider = new JSlider(1, 100, 50);
        styleSlider(speedSlider);
        speedPanel.add(speedSlider);
        topControls.add(speedPanel);

        // Algorithm selection
        JPanel algorithmPanel = createControlGroup("Algorithm");
        String[] algorithms = {"Bubble Sort", "Selection Sort", "Insertion Sort", 
                              "Merge Sort", "Quick Sort", "Heap Sort"};
        algorithmComboBox = new JComboBox<>(algorithms);
        styleComboBox(algorithmComboBox);
        algorithmPanel.add(algorithmComboBox);
        topControls.add(algorithmPanel);

        mainPanel.add(topControls, BorderLayout.NORTH);

        // Bottom controls
        JPanel bottomControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomControls.setBackground(PANEL_COLOR);

        generateButton = createStyledButton("Generate New Array", SUCCESS_COLOR);
        startButton = createStyledButton("Start Sorting", ACCENT_COLOR);
        pauseButton = createStyledButton("Pause", HIGHLIGHT_COLOR);
        stopButton = createStyledButton("Stop", DANGER_COLOR);

        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        // Show numbers checkbox
        showNumbersCheckbox = new JCheckBox("Show Numbers");
        showNumbersCheckbox.setForeground(TEXT_COLOR);
        showNumbersCheckbox.setBackground(PANEL_COLOR);
        showNumbersCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        bottomControls.add(generateButton);
        bottomControls.add(startButton);
        bottomControls.add(pauseButton);
        bottomControls.add(stopButton);
        bottomControls.add(Box.createHorizontalStrut(20));
        bottomControls.add(showNumbersCheckbox);

        mainPanel.add(bottomControls, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createControlGroup(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 1), 
            title, 
            0, 0, 
            new Font("Segoe UI", Font.BOLD, 12), 
            TEXT_COLOR
        ));
        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isEnabled()) {
                    g2d.setColor(getModel().isPressed() ? color.darker() : color);
                } else {
                    g2d.setColor(color.darker().darker());
                }
                
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setPreferredSize(new Dimension(130, 35));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }

    private void styleSlider(JSlider slider) {
        slider.setBackground(PANEL_COLOR);
        slider.setForeground(TEXT_COLOR);
        slider.setPreferredSize(new Dimension(200, 40));
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(slider.getMaximum() / 4);
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(PANEL_COLOR);
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setPreferredSize(new Dimension(150, 30));
    }

    private JLabel createStyledLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    private void setupEventListeners() {
        isPaused = new AtomicBoolean(false);
        isStopped = new AtomicBoolean(false);

        generateButton.addActionListener(e -> {
            generateArray();
            resetStats();
        });

        sizeSlider.addChangeListener(e -> {
            if (!sizeSlider.getValueIsAdjusting()) {
                generateArray();
                resetStats();
            }
        });

        showNumbersCheckbox.addActionListener(e -> {
            showNumbers = showNumbersCheckbox.isSelected();
            drawPanel.repaint();
        });

        startButton.addActionListener(e -> startSorting());
        pauseButton.addActionListener(e -> pauseSorting());
        stopButton.addActionListener(e -> stopSorting());
    }

    private void generateArray() {
        int size = sizeSlider.getValue();
        array = new int[size];
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = rand.nextInt(500) + 1;
        }
        Arrays.fill(highlightedIndices, -1);
        drawPanel.repaint();
        statusLabel.setText("Array generated with " + size + " elements");
        statusLabel.setForeground(SUCCESS_COLOR);
    }

    private void resetStats() {
        comparisons = 0;
        swaps = 0;
        updateStats();
    }

    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            comparisonsLabel.setText("Comparisons: " + comparisons);
            swapsLabel.setText("Swaps: " + swaps);
        });
    }

    private void drawArray(Graphics2D g2d) {
        if (array == null) return;

        int panelWidth = drawPanel.getWidth() - 40;
        int panelHeight = drawPanel.getHeight() - 40;
        double barWidth = (double) panelWidth / array.length;
        int maxValue = Arrays.stream(array).max().orElse(1);

        // Draw background grid
        g2d.setColor(new Color(50, 55, 70));
        g2d.setStroke(new BasicStroke(0.5f));
        for (int i = 0; i <= 10; i++) {
            int y = 20 + (panelHeight * i / 10);
            g2d.drawLine(20, y, panelWidth + 20, y);
        }

        // Draw bars with enhanced styling
        for (int i = 0; i < array.length; i++) {
            int barHeight = (int) (((double) array[i] / maxValue) * panelHeight);
            int x = (int) (20 + i * barWidth);
            int y = panelHeight + 20 - barHeight;

            // Color selection with gradient effect
            Color barColor;
            if (i == highlightedIndices[0] || i == highlightedIndices[1]) {
                barColor = HIGHLIGHT_COLOR;
            } else {
                float hue = (float) array[i] / maxValue * 0.8f;
                barColor = Color.getHSBColor(hue, 0.8f, 0.9f);
            }

            // Create gradient
            GradientPaint gradient = new GradientPaint(
                x, y, barColor.brighter(),
                x, y + barHeight, barColor.darker()
            );
            g2d.setPaint(gradient);

            // Draw rounded rectangle
            RoundRectangle2D.Float rect = new RoundRectangle2D.Float(
                x + 1, y, Math.max(1, (float) barWidth - 2), barHeight, 3, 3
            );
            g2d.fill(rect);

            // Draw border
            g2d.setColor(barColor.darker());
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(rect);

            // Draw numbers if enabled
            if (showNumbers && barWidth > 15) {
                g2d.setColor(TEXT_COLOR);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, Math.max(8, (int)(barWidth/4))));
                String value = String.valueOf(array[i]);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = x + (int)(barWidth - fm.stringWidth(value)) / 2;
                int textY = y - 5;
                if (textY < 15) textY = y + 15;
                g2d.drawString(value, textX, textY);
            }
        }
    }

    private void startSorting() {
        if (sortingThread != null && sortingThread.isAlive()) return;

        resetStats();
        isPaused.set(false);
        isStopped.set(false);
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        stopButton.setEnabled(true);
        generateButton.setEnabled(false);
        
        startTime = System.currentTimeMillis();
        statusLabel.setText("Sorting in progress...");
        statusLabel.setForeground(HIGHLIGHT_COLOR);

        sortingThread = new Thread(() -> {
            try {
                String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
                switch (selectedAlgorithm) {
                    case "Bubble Sort": bubbleSort(); break;
                    case "Selection Sort": selectionSort(); break;
                    case "Insertion Sort": insertionSort(); break;
                    case "Merge Sort": mergeSort(0, array.length - 1); break;
                    case "Quick Sort": quickSort(0, array.length - 1); break;
                    case "Heap Sort": heapSort(); break;
                }
                
                if (!isStopped.get()) {
                    long endTime = System.currentTimeMillis();
                    double duration = (endTime - startTime) / 1000.0;
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Sorting completed in " + String.format("%.2f", duration) + " seconds");
                        statusLabel.setForeground(SUCCESS_COLOR);
                        showComplexityDialog();
                    });
                }
            } catch (InterruptedException e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Sorting interrupted");
                    statusLabel.setForeground(DANGER_COLOR);
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                    stopButton.setEnabled(false);
                    generateButton.setEnabled(true);
                    pauseButton.setText("Pause");
                    Arrays.fill(highlightedIndices, -1);
                    drawPanel.repaint();
                });
            }
        });

        sortingThread.start();
    }

    private void pauseSorting() {
        isPaused.set(!isPaused.get());
        pauseButton.setText(isPaused.get() ? "Resume" : "Pause");
        statusLabel.setText(isPaused.get() ? "Sorting paused" : "Sorting resumed");
        statusLabel.setForeground(isPaused.get() ? HIGHLIGHT_COLOR : SUCCESS_COLOR);
    }

    private void stopSorting() {
        isStopped.set(true);
        if (sortingThread != null) {
            sortingThread.interrupt();
        }
        statusLabel.setText("Sorting stopped");
        statusLabel.setForeground(DANGER_COLOR);
    }

    private int calculateSleepTime() {
        return Math.max(1, 101 - speedSlider.getValue());
    }

    private void highlight(int index1, int index2) {
        highlightedIndices[0] = index1;
        highlightedIndices[1] = index2;
        SwingUtilities.invokeLater(() -> drawPanel.repaint());
    }

    // Enhanced sorting algorithms with highlighting
    private void bubbleSort() throws InterruptedException {
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = 0; j < array.length - i - 1; j++) {
                if (isStopped.get()) return;
                while (isPaused.get()) Thread.sleep(10);
                
                highlight(j, j + 1);
                comparisons++;
                updateStats();
                Thread.sleep(calculateSleepTime());
                
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                    swaps++;
                    updateStats();
                }
            }
        }
    }

    private void selectionSort() throws InterruptedException {
        for (int i = 0; i < array.length - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < array.length; j++) {
                if (isStopped.get()) return;
                while (isPaused.get()) Thread.sleep(10);
                
                highlight(minIndex, j);
                comparisons++;
                updateStats();
                Thread.sleep(calculateSleepTime());
                
                if (array[j] < array[minIndex]) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                int temp = array[minIndex];
                array[minIndex] = array[i];
                array[i] = temp;
                swaps++;
                updateStats();
            }
        }
    }

    private void insertionSort() throws InterruptedException {
        for (int i = 1; i < array.length; i++) {
            int key = array[i];
            int j = i - 1;
            while (j >= 0 && array[j] > key) {
                if (isStopped.get()) return;
                while (isPaused.get()) Thread.sleep(10);
                
                highlight(j, j + 1);
                comparisons++;
                array[j + 1] = array[j];
                j--;
                swaps++;
                updateStats();
                Thread.sleep(calculateSleepTime());
            }
            array[j + 1] = key;
        }
    }

    private void mergeSort(int left, int right) throws InterruptedException {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(left, mid);
            mergeSort(mid + 1, right);
            merge(left, mid, right);
        }
    }

    private void merge(int left, int mid, int right) throws InterruptedException {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        int[] L = new int[n1];
        int[] R = new int[n2];
        System.arraycopy(array, left, L, 0, n1);
        System.arraycopy(array, mid + 1, R, 0, n2);

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (isStopped.get()) return;
            while (isPaused.get()) Thread.sleep(10);
            
            highlight(left + i, mid + 1 + j);
            comparisons++;
            updateStats();
            Thread.sleep(calculateSleepTime());
            
            if (L[i] <= R[j]) {
                array[k++] = L[i++];
            } else {
                array[k++] = R[j++];
            }
            swaps++;
            updateStats();
        }
        while (i < n1) array[k++] = L[i++];
        while (j < n2) array[k++] = R[j++];
    }

    private void quickSort(int low, int high) throws InterruptedException {
        if (low < high) {
            int pi = partition(low, high);
            quickSort(low, pi - 1);
            quickSort(pi + 1, high);
        }
    }

    private int partition(int low, int high) throws InterruptedException {
        int pivot = array[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (isStopped.get()) return high;
            while (isPaused.get()) Thread.sleep(10);
            
            highlight(j, high);
            comparisons++;
            updateStats();
            Thread.sleep(calculateSleepTime());
            
            if (array[j] <= pivot) {
                i++;
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
                swaps++;
                updateStats();
            }
        }
        int temp = array[i + 1];
        array[i + 1] = array[high];
        array[high] = temp;
        swaps++;
        updateStats();
        return i + 1;
    }

    private void heapSort() throws InterruptedException {
        int n = array.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(n, i);
        }
        for (int i = n - 1; i >= 0; i--) {
            if (isStopped.get()) return;
            while (isPaused.get()) Thread.sleep(10);
            
            int temp = array[0];
            array[0] = array[i];
            array[i] = temp;
            swaps++;
            updateStats();
            heapify(i, 0);
            Thread.sleep(calculateSleepTime());
        }
    }

    private void heapify(int n, int i) throws InterruptedException {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n && array[left] > array[largest]) {
            largest = left;
        }
        if (right < n && array[right] > array[largest]) {
            largest = right;
        }
        if (largest != i) {
            if (isStopped.get()) return;
            while (isPaused.get()) Thread.sleep(10);
            
            highlight(i, largest);
            comparisons++;
            int swap = array[i];
            array[i] = array[largest];
            array[largest] = swap;
            swaps++;
            updateStats();
            Thread.sleep(calculateSleepTime());
            heapify(n, largest);
        }
    }

    private void showComplexityDialog() {
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        String complexityInfo = getComplexityInfo(selectedAlgorithm);
        
        JDialog dialog = new JDialog(this, "Algorithm Analysis", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
        
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setText(complexityInfo);
        textPane.setBackground(PANEL_COLOR);
        textPane.setForeground(TEXT_COLOR);
        textPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textPane.setEditable(false);
        textPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(null);
        dialog.add(scrollPane);
        
        dialog.setVisible(true);
    }

    private String getComplexityInfo(String algorithm) {
        StringBuilder info = new StringBuilder();
        info.append("<html><body style='background-color: #232937; color: #f0f5ff; font-family: Segoe UI;'>");
        info.append("<h2 style='color: #64c8ff; text-align: center;'>").append(algorithm).append(" Analysis</h2>");
        info.append("<div style='margin: 20px;'>");
        
        switch (algorithm) {
            case "Bubble Sort":
                info.append("<h3 style='color: #2ecc71;'>Time Complexity:</h3>");
                info.append("<ul>");
                info.append("<li><b>Best Case:</b> <span class='complexity'>O(n&sup2;)</span> - Always same performance</li>");
                info.append("<li><b>Average Case:</b> <span class='complexity'>O(n&sup2;)</span> - Random order</li>");
                info.append("<li><b>Worst Case:</b> <span class='complexity'>O(n&sup2;)</span> - Always same performance</li>");
                info.append("</ul>");
                info.append("<h3 style='color: #2ecc71;'>Space Complexity:</h3><p>O(1) - In-place sorting</p>");
                info.append("<h3 style='color: #2ecc71;'>Characteristics:</h3>");
                info.append("<ul><li>Stable sorting algorithm</li><li>Simple but inefficient for large datasets</li><li>Good for educational purposes</li></ul>");
                break;
            case "Selection Sort":
                info.append("<h3 style='color: #2ecc71;'>Time Complexity:</h3>");
                info.append("<ul>");
                info.append("<li><b>Best Case:</b> <span class='complexity'>O(n&sup2;)</span> - Always same performance</li>");
                info.append("<li><b>Average Case:</b> <span class='complexity'>O(n&sup2;)</span> - Random order</li>");
                info.append("<li><b>Worst Case:</b> <span class='complexity'>O(n&sup2;)</span> - Always same performance</li>");
                info.append("</ul>");
                info.append("<h3 style='color: #2ecc71;'>Space Complexity:</h3><p>O(1) - In-place sorting</p>");
                info.append("<h3 style='color: #2ecc71;'>Characteristics:</h3>");
                info.append("<ul><li>Not stable</li><li>Minimum number of swaps</li><li>Simple implementation</li></ul>");
                break;
            case "Insertion Sort":
                info.append("<h3 style='color: #2ecc71;'>Time Complexity:</h3>");
                info.append("<ul>");
                info.append("<li><b>Best Case:</b> <span class='complexity'>O(n)</span> - Already sorted array</li>");
                info.append("<li><b>Average Case:</b> <span class='complexity'>O(n&sup2;)</span> - Random order</li>");
                info.append("<li><b>Worst Case:</b> <span class='complexity'>O(n&sup2;)</span> - Reverse sorted array</li>");
                info.append("</ul>");
                info.append("<h3 style='color: #2ecc71;'>Space Complexity:</h3><p>O(1) - In-place sorting</p>");
                info.append("<h3 style='color: #2ecc71;'>Characteristics:</h3>");
                info.append("<ul><li>Stable sorting algorithm</li><li>Efficient for small datasets</li><li>Adaptive - performs well on nearly sorted data</li></ul>");
                break;
            case "Merge Sort":
                info.append("<h3 style='color: #2ecc71;'>Time Complexity:</h3>");
                info.append("<ul><li><b>Best Case:</b> O(n log n)</li>");
                info.append("<li><b>Average Case:</b> O(n log n)</li>");
                info.append("<li><b>Worst Case:</b> O(n log n)</li></ul>");
                info.append("<h3 style='color: #2ecc71;'>Space Complexity:</h3><p>O(n) - Requires additional space</p>");
                info.append("<h3 style='color: #2ecc71;'>Characteristics:</h3>");
                info.append("<ul><li>Stable sorting algorithm</li><li>Divide and conquer approach</li><li>Consistent performance across all cases</li></ul>");
                break;
            case "Quick Sort":
                info.append("<h3 style='color: #2ecc71;'>Time Complexity:</h3>");
                info.append("<ul><li><b>Best Case:</b> O(n log n) - Good pivot selection</li>");
                info.append("<li><b>Average Case:</b> O(n log n)</li>");
                info.append("<li><b>Worst Case:</b> <span class='complexity'>O(n&sup2;)</span> - Poor pivot selection</li></ul>");
                info.append("<h3 style='color: #2ecc71;'>Space Complexity:</h3><p>O(log n) - Recursive calls</p>");
                info.append("<h3 style='color: #2ecc71;'>Characteristics:</h3>");
                info.append("<ul><li>Not stable</li><li>In-place sorting</li><li>Generally fastest practical sorting algorithm</li></ul>");
                break;
            case "Heap Sort":
                info.append("<h3 style='color: #2ecc71;'>Time Complexity:</h3>");
                info.append("<ul><li><b>Best Case:</b> O(n log n)</li>");
                info.append("<li><b>Average Case:</b> O(n log n)</li>");
                info.append("<li><b>Worst Case:</b> O(n log n)</li></ul>");
                info.append("<h3 style='color: #2ecc71;'>Space Complexity:</h3><p>O(1) - In-place sorting</p>");
                info.append("<h3 style='color: #2ecc71;'>Characteristics:</h3>");
                info.append("<ul><li>Not stable</li><li>Consistent O(n log n) performance</li><li>Uses heap data structure</li></ul>");
                break;
        }
        
        // Add performance statistics
        info.append("<h3 style='color: #f39c12;'>Current Execution Statistics:</h3>");
        info.append("<p><b>Array Size:</b> ").append(array.length).append(" elements<br>");
        info.append("<b>Total Comparisons:</b> ").append(comparisons).append("<br>");
        info.append("<b>Total Swaps:</b> ").append(swaps).append("<br>");
        long endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) / 1000.0;
        info.append("<b>Execution Time:</b> ").append(String.format("%.3f", duration)).append(" seconds</p>");
        
        // Add efficiency analysis
        double theoreticalComparisons = getTheoreticalComparisons(algorithm, array.length);
        if (theoreticalComparisons > 0) {
            double efficiency = (comparisons / theoreticalComparisons) * 100;
            info.append("<h3 style='color: #e74c3c;'>Efficiency Analysis:</h3>");
            info.append("<p><b>Theoretical Comparisons:</b> ").append(String.format("%.0f", theoreticalComparisons)).append("<br>");
            info.append("<b>Actual vs Theoretical:</b> ").append(String.format("%.1f%%", efficiency)).append("</p>");
        }
        
        info.append("</div></body></html>");
        return info.toString();
    }
    
    private double getTheoreticalComparisons(String algorithm, int n) {
        switch (algorithm) {
            case "Bubble Sort":
                return (n * (n - 1)) / 2.0; // Worst case
            case "Selection Sort":
                return (n * (n - 1)) / 2.0;
            case "Insertion Sort":
                return (n * (n - 1)) / 2.0; // Worst case
            case "Merge Sort":
                return n * Math.log(n) / Math.log(2);
            case "Quick Sort":
                return n * Math.log(n) / Math.log(2); // Average case
            case "Heap Sort":
                return n * Math.log(n) / Math.log(2);
            default:
                return -1;
        }
    }
    
    // Main method to run the application
    public static void main(String[] args) {
        // Set system look and feel for better appearance
        
        
        // Enable anti-aliasing for better text rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Create and show the application
        SwingUtilities.invokeLater(() -> {
            try {
                SortingAlgorithmVisualizer visualizer = new SortingAlgorithmVisualizer();
                visualizer.setLocationRelativeTo(null); // Center the window
                visualizer.setVisible(true);
                
                // Show welcome message
                showWelcomeDialog(visualizer);
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error starting the application: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // Fix for Timer ambiguity - use javax.swing.Timer explicitly
    private static void showWelcomeDialog(JFrame parent) {
        JDialog welcomeDialog = new JDialog(parent, "Welcome to Sorting Algorithm Visualizer", true);
        welcomeDialog.setSize(500, 350);
        welcomeDialog.setLocationRelativeTo(parent);
        welcomeDialog.getContentPane().setBackground(new Color(20, 25, 35));
        welcomeDialog.setLayout(new BorderLayout());
        
        // Create welcome content
        JTextPane welcomeText = new JTextPane();
        welcomeText.setContentType("text/html");
        welcomeText.setText(
            "<html><body style='background-color: #141923; color: #f0f5ff; font-family: Segoe UI; padding: 20px;'>" +
            "<h1 style='color: #64c8ff; text-align: center; margin-bottom: 20px;'>Sorting Visualizer</h1>" +
            "<h3 style='color: #2ecc71;'>Features:</h3>" +
            "<ul style='margin: 15px 0;'>" +
            "<li>Visual representation of 6 different sorting algorithms</li>" +
            "<li>Adjustable animation speed and array size</li>" +
            "<li>Real-time statistics (comparisons, swaps, time)</li>" +
            "<li>Pause/Resume functionality during sorting</li>" +
            "<li>Optional number display on bars</li>" +
            "<li>Detailed algorithm complexity analysis</li>" +
            "</ul>" +
            "<h3 style='color: #f39c12;'>How to Use:</h3>" +
            "<ol style='margin: 15px 0;'>" +
            "<li>Adjust array size and animation speed using sliders</li>" +
            "<li>Select a sorting algorithm from the dropdown</li>" +
            "<li>Click 'Generate New Array' to create random data</li>" +
            "<li>Click 'Start Sorting' to begin visualization</li>" +
            "<li>Use Pause/Resume/Stop controls as needed</li>" +
            "</ol>" +
            "<p style='text-align: center; margin-top: 20px; font-style: italic; color: #95a5a6;'>" +
            "Perfect for learning and understanding sorting algorithms!</p>" +
            "</body></html>"
        );
        welcomeText.setBackground(new Color(20, 25, 35));
        welcomeText.setEditable(false);
        welcomeText.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(welcomeText);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Create close button
        JButton closeButton = new JButton("Get Started!");
        closeButton.setBackground(new Color(46, 204, 113));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.setPreferredSize(new Dimension(120, 35));
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> welcomeDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(20, 25, 35));
        buttonPanel.add(closeButton);
        
        welcomeDialog.add(scrollPane, BorderLayout.CENTER);
        welcomeDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Fix for Timer ambiguity - use javax.swing.Timer explicitly
        javax.swing.Timer timer = new javax.swing.Timer(500, e -> welcomeDialog.setVisible(true));
        timer.setRepeats(false);
        timer.start();
    }
    
    // Cleanup method
    private void cleanup() {
        if (sortingThread != null && sortingThread.isAlive()) {
            isStopped.set(true);
            sortingThread.interrupt();
        }
    }
    
    @Override
    public void dispose() {
        cleanup();
        super.dispose();
    }
}
                