import java.util.concurrent.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MainMethod {

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new RWGui());
  }
}

// ============================================================
// GUI CLASS
// ============================================================
class RWGui extends JFrame implements Synch.GUICallback {

  // --- Thread counts ---
  private static final int NUM_THREADS = 8;

  // --- Color palette ---
  private static final Color BG = new Color(13, 17, 23);
  private static final Color PANEL_BG = new Color(22, 27, 34);
  private static final Color IDLE_COL = new Color(48, 54, 61);
  private static final Color WAIT_COL = new Color(210, 153, 34);
  private static final Color READ_COL = new Color(35, 134, 54);
  private static final Color WRITE_COL = new Color(201, 48, 44);
  private static final Color TEXT_COL = new Color(230, 237, 243);
  private static final Color DIM_COL = new Color(110, 118, 129);
  private static final Color ACCENT = new Color(88, 166, 255);

  // --- State labels for each thread ---
  private JLabel[] readerLabels = new JLabel[NUM_THREADS + 1];
  private JLabel[] writerLabels = new JLabel[NUM_THREADS + 1];
  private JPanel[] readerPanels = new JPanel[NUM_THREADS + 1];
  private JPanel[] writerPanels = new JPanel[NUM_THREADS + 1];

  // --- Shared resource panel ---
  private JLabel resourceLabel;
  private JPanel resourcePanel;

  // --- Log area ---
  private JTextArea logArea;

  // --- Counters ---
  private JLabel readcountLabel;
  private JLabel writecountLabel;

  // --- Control ---
  private JButton startBtn;
  private JComboBox<Synch.Strategy> strategyBox;
  private boolean started = false;

  public RWGui() {
    setTitle("Readers / Writers Synchronization");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(980, 780);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout(10, 10));
    getContentPane().setBackground(BG);

    // Register this GUI as the callback
    Synch.guiCallback = this;

    buildUI();
    setVisible(true);
  }

  // ---- BUILD UI ------------------------------------------------
  private void buildUI() {
    // Title bar and strategy selector
    JPanel header = new JPanel(new BorderLayout(0, 6));
    header.setBackground(BG);
    JLabel title = new JLabel("Readers / Writers Problem", SwingConstants.CENTER);
    title.setFont(new Font("Monospaced", Font.BOLD, 18));
    title.setForeground(ACCENT);
    title.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
    header.add(title, BorderLayout.NORTH);

    JPanel strategyControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    strategyControls.setBackground(BG);
    JLabel strategyLabel = new JLabel("Synchronization strategy:");
    strategyLabel.setForeground(TEXT_COL);
    strategyLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
    strategyBox = new JComboBox<>(Synch.Strategy.values());
    strategyBox.setSelectedItem(Synch.strategy);
    strategyBox.setFont(new Font("Monospaced", Font.PLAIN, 12));
    strategyBox.addActionListener(e -> {
      if (!started) {
        Synch.strategy = (Synch.Strategy) strategyBox.getSelectedItem();
      }
    });
    strategyControls.add(strategyLabel);
    strategyControls.add(strategyBox);
    header.add(strategyControls, BorderLayout.SOUTH);
    add(header, BorderLayout.NORTH);

    // Center panel
    JPanel center = new JPanel(new GridLayout(1, 3, 10, 0));
    center.setBackground(BG);
    center.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

    center.add(buildThreadPanel("READERS", readerLabels, readerPanels, READ_COL));
    center.add(buildResourcePanel());
    center.add(buildThreadPanel("WRITERS", writerLabels, writerPanels, WRITE_COL));

    add(center, BorderLayout.CENTER);

    // Bottom: log + button
    JPanel bottom = new JPanel(new BorderLayout(6, 6));
    bottom.setBackground(BG);
    bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

    logArea = new JTextArea(8, 60);
    logArea.setBackground(new Color(13, 17, 23));
    logArea.setForeground(new Color(163, 184, 204));
    logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
    logArea.setEditable(false);
    logArea.setBorder(BorderFactory.createLineBorder(IDLE_COL));
    JScrollPane scroll = new JScrollPane(logArea);
    scroll.setBorder(BorderFactory.createLineBorder(IDLE_COL));

    startBtn = new JButton("▶  Start Simulation");
    startBtn.setFont(new Font("Monospaced", Font.BOLD, 13));
    startBtn.setBackground(new Color(35, 134, 54));
    startBtn.setForeground(Color.WHITE);
    startBtn.setFocusPainted(false);
    startBtn.setBorderPainted(false);
    startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    startBtn.addActionListener(e -> startSimulation());

    bottom.add(scroll, BorderLayout.CENTER);
    bottom.add(startBtn, BorderLayout.SOUTH);
    add(bottom, BorderLayout.SOUTH);
  }

  private JPanel buildThreadPanel(String title, JLabel[] labels, JPanel[] panels, Color activeColor) {
    JPanel outer = new JPanel(new BorderLayout(0, 8));
    outer.setBackground(PANEL_BG);
    outer.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(IDLE_COL),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)));

    JLabel hdr = new JLabel(title, SwingConstants.CENTER);
    hdr.setFont(new Font("Monospaced", Font.BOLD, 14));
    hdr.setForeground(activeColor);
    outer.add(hdr, BorderLayout.NORTH);

    JPanel grid = new JPanel(new GridLayout(NUM_THREADS, 1, 0, 6));
    grid.setBackground(PANEL_BG);

    for (int i = 1; i <= NUM_THREADS; i++) {
      JPanel card = new JPanel(new BorderLayout());
      card.setBackground(IDLE_COL);
      card.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

      JLabel lbl = new JLabel(title.charAt(0) + "" + i + "  —  IDLE", SwingConstants.LEFT);
      lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
      lbl.setForeground(DIM_COL);
      card.add(lbl, BorderLayout.CENTER);

      labels[i] = lbl;
      panels[i] = card;
      grid.add(card);
    }

    outer.add(grid, BorderLayout.CENTER);
    return outer;
  }

  private JPanel buildResourcePanel() {
    JPanel outer = new JPanel(new BorderLayout(0, 10));
    outer.setBackground(PANEL_BG);
    outer.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(IDLE_COL),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)));

    JLabel hdr = new JLabel("SHARED RESOURCE", SwingConstants.CENTER);
    hdr.setFont(new Font("Monospaced", Font.BOLD, 13));
    hdr.setForeground(ACCENT);
    outer.add(hdr, BorderLayout.NORTH);

    resourcePanel = new JPanel(new BorderLayout());
    resourcePanel.setBackground(IDLE_COL);
    resourcePanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
    resourceLabel = new JLabel("FREE", SwingConstants.CENTER);
    resourceLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
    resourceLabel.setForeground(DIM_COL);
    resourcePanel.add(resourceLabel, BorderLayout.CENTER);
    outer.add(resourcePanel, BorderLayout.CENTER);

    // Counter panel
    JPanel counters = new JPanel(new GridLayout(2, 1, 0, 4));
    counters.setBackground(PANEL_BG);

    readcountLabel = makeCounterLabel("Active Readers: 0", READ_COL);
    writecountLabel = makeCounterLabel("Active Writers: 0", WRITE_COL);
    counters.add(readcountLabel);
    counters.add(writecountLabel);

    // Legend
    JPanel legend = new JPanel(new GridLayout(3, 1, 0, 2));
    legend.setBackground(PANEL_BG);
    legend.add(makeLegend("■ WAITING", WAIT_COL));
    legend.add(makeLegend("■ ACTIVE", READ_COL));
    legend.add(makeLegend("■ WRITING", WRITE_COL));

    JPanel south = new JPanel(new BorderLayout(0, 6));
    south.setBackground(PANEL_BG);
    south.add(counters, BorderLayout.NORTH);
    south.add(legend, BorderLayout.SOUTH);
    outer.add(south, BorderLayout.SOUTH);

    return outer;
  }

  private JLabel makeCounterLabel(String text, Color color) {
    JLabel lbl = new JLabel(text, SwingConstants.CENTER);
    lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
    lbl.setForeground(color);
    return lbl;
  }

  private JLabel makeLegend(String text, Color color) {
    JLabel lbl = new JLabel(text, SwingConstants.CENTER);
    lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
    lbl.setForeground(color);
    return lbl;
  }

  // ---- START SIMULATION ----------------------------------------
  private void startSimulation() {
    if (started)
      return;
    started = true;
    startBtn.setEnabled(false);
    strategyBox.setEnabled(false);
    startBtn.setText("Simulation Running...");
    startBtn.setBackground(IDLE_COL);

    // Initialize semaphores for the selected strategy
    Synch.readMutex = new java.util.concurrent.Semaphore(1, true);
    Synch.writeMutex = new java.util.concurrent.Semaphore(1, true);
    Synch.readTry = new java.util.concurrent.Semaphore(1, true);
    Synch.resource = new java.util.concurrent.Semaphore(1, true);
    Synch.serviceQueue = new java.util.concurrent.Semaphore(1, true);
    Synch.readcount = 0;
    Synch.writecount = 0;

    for (int i = 1; i <= NUM_THREADS; i++) {
      new Reader(i).start();
      new Writer(i).start();
    }

    appendLog("=== Simulation started (" + Synch.strategy + ") ===");
  }

  // ---- GUI CALLBACKS (called from Reader/Writer threads) -------

  @Override
  public void onReaderWaiting(int id) {
    SwingUtilities.invokeLater(() -> setThreadState(readerLabels[id], readerPanels[id],
        "R" + id + "  —  WAITING", WAIT_COL, Color.BLACK));
  }

  @Override
  public void onReaderReading(int id) {
    SwingUtilities.invokeLater(() -> {
      setThreadState(readerLabels[id], readerPanels[id], "R" + id + "  —  READING", READ_COL, Color.WHITE);
      updateResource();
      updateCounters();
    });
  }

  @Override
  public void onReaderDone(int id) {
    SwingUtilities.invokeLater(() -> {
      setThreadState(readerLabels[id], readerPanels[id], "R" + id + "  —  IDLE", IDLE_COL, DIM_COL);
      updateResource();
      updateCounters();
    });
  }

  @Override
  public void onWriterWaiting(int id) {
    SwingUtilities.invokeLater(() -> setThreadState(writerLabels[id], writerPanels[id],
        "W" + id + "  —  WAITING", WAIT_COL, Color.BLACK));
  }

  @Override
  public void onWriterWriting(int id) {
    SwingUtilities.invokeLater(() -> {
      setThreadState(writerLabels[id], writerPanels[id], "W" + id + "  —  WRITING", WRITE_COL, Color.WHITE);
      updateResource();
      updateCounters();
    });
  }

  @Override
  public void onWriterDone(int id) {
    SwingUtilities.invokeLater(() -> {
      setThreadState(writerLabels[id], writerPanels[id], "W" + id + "  —  IDLE", IDLE_COL, DIM_COL);
      updateResource();
      updateCounters();
    });
  }

  @Override
  public void onLog(String message) {
    SwingUtilities.invokeLater(() -> appendLog(message));
  }

  // ---- HELPERS --------------------------------------------------

  private void setThreadState(JLabel lbl, JPanel panel, String text, Color bg, Color fg) {
    lbl.setText(text);
    lbl.setForeground(fg);
    panel.setBackground(bg);
    panel.repaint();
  }

  private void updateResource() {
    int rc = Synch.readcount;
    int wc = Synch.writecount;
    if (wc > 0) {
      resourceLabel.setText("WRITING");
      resourceLabel.setForeground(Color.WHITE);
      resourcePanel.setBackground(WRITE_COL);
    } else if (rc > 0) {
      resourceLabel.setText("READING (" + rc + ")");
      resourceLabel.setForeground(Color.WHITE);
      resourcePanel.setBackground(READ_COL);
    } else {
      resourceLabel.setText("FREE");
      resourceLabel.setForeground(DIM_COL);
      resourcePanel.setBackground(IDLE_COL);
    }
    resourcePanel.repaint();
  }

  private void updateCounters() {
    readcountLabel.setText("Active Readers: " + Synch.readcount);
    writecountLabel.setText("Active/Waiting Writers: " + Synch.writecount);
  }

  private void appendLog(String msg) {
    logArea.append(msg + "\n");
    logArea.setCaretPosition(logArea.getDocument().getLength());
  }
}