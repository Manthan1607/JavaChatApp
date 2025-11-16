import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client extends JFrame {
    private Socket socket;
    private BufferedReader br;
    private PrintWriter out;

    private ChatPanel chatPanel;
    private JTextField messageField;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private JLabel timeLabel;

    // Colors
    private final Color GREEN = new Color(37, 211, 102);
    private final Color LIGHT_GREEN = new Color(217, 255, 208);

    public Client() {
        setTitle("WhatsApp Chat Client");
        setSize(400, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header panel (top bar)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(GREEN);
        headerPanel.setBorder(new EmptyBorder(8, 12, 8, 12));

        // Profile circle
        JLabel profilePic = new JLabel();
        profilePic.setPreferredSize(new Dimension(40, 40));
        profilePic.setOpaque(true);
        profilePic.setBackground(new Color(255, 255, 255, 60));
        profilePic.setBorder(new RoundBorder(20));

        // Name + status
        JLabel nameLabel = new JLabel("Manthan");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));

        JLabel statusLabel = new JLabel("Online");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBackground(GREEN);
        namePanel.add(nameLabel);
        namePanel.add(statusLabel);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftHeader.setBackground(GREEN);
        leftHeader.add(profilePic);
        leftHeader.add(namePanel);

        // Time label
        timeLabel = new JLabel(getCurrentTime());
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        new Timer(60000, e -> timeLabel.setText(getCurrentTime())).start();

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(timeLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Chat area
        chatPanel = new ChatPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Input area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        inputPanel.setBackground(Color.WHITE);

        messageField = new JTextField();
        messageField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        messageField.setBorder(new LineBorder(GREEN, 1, true));

        sendButton = new JButton("Send");
        sendButton.setBackground(GREEN);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(new EmptyBorder(5, 10, 5, 10));

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Listeners
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        // Connect to server
        startConnection();

        setVisible(true);
    }

    // Get current time
    private String getCurrentTime() {
        return new SimpleDateFormat("hh:mm a").format(new Date());
    }

    private void startConnection() {
        Thread connectThread = new Thread(() -> {
            try {
                socket = new Socket("127.0.0.1", 7777);
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());

                addMessage("Connected to server", false);

                String msg;
                while ((msg = br.readLine()) != null) {
                    addMessage(msg, false);
                }
            } catch (IOException e) {
                addMessage("Disconnected from server", false);
            }
        });
        connectThread.start();
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty() && out != null) {
            addMessage(msg, true);
            out.println(msg);
            out.flush();
            messageField.setText("");
        }
    }

    // Message bubbles with rounded corners
    private void addMessage(String message, boolean isUser) {
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.X_AXIS));
        bubble.setOpaque(false);

        JTextArea messageLabel = new JTextArea(message);
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        messageLabel.setLineWrap(true);
        messageLabel.setWrapStyleWord(true);
        messageLabel.setEditable(false);
        messageLabel.setOpaque(true);
        messageLabel.setMaximumSize(new Dimension(160, Integer.MAX_VALUE));

        // Rounded bubble borders
        if (!isUser && (message.toLowerCase().contains("connected") || message.toLowerCase().contains("disconnected"))) {
            messageLabel.setBackground(new Color(235, 235, 235));
            messageLabel.setForeground(Color.DARK_GRAY);
            messageLabel.setBorder(new CompoundBorder(new RoundBorder(10), new EmptyBorder(5, 10, 5, 10)));
            bubble.setAlignmentX(Component.CENTER_ALIGNMENT);
        } 
        else if (isUser) {
            messageLabel.setBackground(LIGHT_GREEN);
            messageLabel.setBorder(new CompoundBorder(new RoundBorder(15), new EmptyBorder(6, 10, 6, 10)));
            bubble.setAlignmentX(Component.RIGHT_ALIGNMENT);
        } 
        else {
            messageLabel.setBackground(Color.WHITE);
            messageLabel.setBorder(new CompoundBorder(new RoundBorder(15), new EmptyBorder(6, 10, 6, 10)));
            bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        bubble.add(messageLabel);
        bubble.setBorder(new EmptyBorder(2, 8, 2, 8));
        chatPanel.add(bubble);
        chatPanel.add(Box.createVerticalStrut(3));

        chatPanel.revalidate();
        chatPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    // Rounded border helper
    static class RoundBorder extends AbstractBorder {
        private final int radius;
        RoundBorder(int radius) { this.radius = radius; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(new Color(180, 180, 180));
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    // Gradient background
    static class ChatPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(245, 255, 245),
                0, height, new Color(230, 240, 230)
            );
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
