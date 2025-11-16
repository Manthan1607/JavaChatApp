import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ServerModernChatGUI extends JFrame {
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader br;
    private PrintWriter out;

    private JPanel chatPanel;
    private JTextField messageField;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private JLabel statusLabel;

    private Point initialClick;

    private final Color ACCENT = new Color(0, 180, 120);
    private final Color BG = new Color(245, 245, 245);

    public ServerModernChatGUI() {
        // Frame setup
        setUndecorated(true);
        setSize(400, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Movable header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT);
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("💬 Server Chat");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        statusLabel = new JLabel("Waiting for client...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(230, 230, 230));

        JPanel leftHeader = new JPanel();
        leftHeader.setOpaque(false);
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.add(title);
        leftHeader.add(statusLabel);

        JButton closeBtn = new JButton("×");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(ACCENT);
        closeBtn.setBorder(null);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> System.exit(0));

        header.add(leftHeader, BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Drag window using header
        header.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });
        header.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                setLocation(thisX + xMoved, thisY + yMoved);
            }
        });

        // Chat panel
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(BG);

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        messageField = new JTextField();
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageField.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));
        messageField.setPreferredSize(new Dimension(0, 35));

        sendButton = new JButton("Send");
        sendButton.setBackground(ACCENT);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setFocusPainted(false);
        sendButton.setBorder(new EmptyBorder(8, 20, 8, 20));
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        // Listeners
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeConnection();
            }
        });

        // Start server
        new Thread(this::startServer).start();
        setVisible(true);
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(7777);
            addMessage("Server started. Waiting for client...", false);

            socket = serverSocket.accept();
            addMessage("Client connected ✅", false);
            statusLabel.setText("Connected");

            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            startReading();
        } catch (IOException e) {
            addMessage("Error: " + e.getMessage(), false);
            statusLabel.setText("Error");
        }
    }

    private void startReading() {
        Thread reader = new Thread(() -> {
            try {
                String msg;
                while ((msg = br.readLine()) != null) {
                    if (msg.equalsIgnoreCase("exit")) {
                        addMessage("Client disconnected ❌", false);
                        closeConnection();
                        break;
                    }
                    addMessage(msg, false);
                }
            } catch (IOException e) {
                addMessage("Connection lost ⚠️", false);
            }
        });
        reader.start();
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

    private void addMessage(String message, boolean isServer) {
        JPanel msgBubble = new JPanel(new BorderLayout());
        msgBubble.setOpaque(false);

        JTextArea msgText = new JTextArea(message);
        msgText.setLineWrap(true);
        msgText.setWrapStyleWord(true);
        msgText.setEditable(false);
        msgText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        msgText.setBorder(new EmptyBorder(8, 12, 8, 12));
        msgText.setMaximumSize(new Dimension(250, Integer.MAX_VALUE));

        if (isServer) {
            msgText.setBackground(ACCENT);
            msgText.setForeground(Color.WHITE);
            msgText.setBorder(new EmptyBorder(8, 12, 8, 12));
            msgText.setBorder(BorderFactory.createLineBorder(new Color(0, 140, 90), 1, true));
            msgBubble.add(msgText, BorderLayout.EAST);
        } else {
            msgText.setBackground(Color.WHITE);
            msgText.setForeground(Color.BLACK);
            msgText.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));
            msgBubble.add(msgText, BorderLayout.WEST);
        }

        chatPanel.add(msgBubble);
        chatPanel.add(Box.createVerticalStrut(5));

        SwingUtilities.invokeLater(() -> {
            chatPanel.revalidate();
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private void closeConnection() {
        try {
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
        statusLabel.setText("Disconnected");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerModernChatGUI::new);
    }
}
