import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerGUI extends JFrame {
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader br;
    private PrintWriter out;

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JLabel statusLabel;

    private volatile boolean isRunning = true;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public ServerGUI() {
        setTitle(" Chat Server");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(false);

        //  Colors
        Color bgColor = new Color(30, 30, 30);
        Color textColor = new Color(230, 230, 230);
        Color accentColor = new Color(0, 153, 255);

        //  Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(bgColor);
        chatArea.setForeground(textColor);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(null);

        //  Input area
        messageField = new JTextField();
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.setBackground(new Color(45, 45, 45));
        messageField.setForeground(Color.WHITE);
        messageField.setCaretColor(Color.WHITE);
        messageField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        sendButton = new JButton("Send");
        sendButton.setBackground(accentColor);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(bgColor);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // Status bar
        statusLabel = new JLabel("Starting server...");
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusLabel.setBackground(new Color(25, 25, 25));
        statusLabel.setOpaque(true);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);

        // Event Listeners
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeConnection();
            }
        });

        // Start server in a background thread
        new Thread(this::startServer).start();

        setVisible(true);
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(7777);
            appendMessage("System", " Server started on port 7777. Waiting for client...");
            socket = serverSocket.accept();
            appendMessage("System", "Client connected.");

            statusLabel.setText(" Connected to client");

            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            startReading();
        } catch (IOException e) {
            appendMessage("System", " Server error: " + e.getMessage());
            statusLabel.setText(" Server failed");
            disableInput();
        }
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty() || !isRunning) return;

        appendMessage("You", msg);
        out.println(msg);
        messageField.setText("");

        if (msg.equalsIgnoreCase("exit")) {
            appendMessage("System", "Closing connection...");
            closeConnection();
        }
    }

    private void startReading() {
        Runnable reader = () -> {
            try {
                while (isRunning && !socket.isClosed()) {
                    String msg = br.readLine();
                    if (msg == null || msg.equalsIgnoreCase("exit")) {
                        appendMessage("System", " Client ended the chat.");
                        closeConnection();
                        break;
                    }
                    appendMessage("Client", msg);
                }
            } catch (IOException e) {
                if (isRunning) {
                    appendMessage("System", " Connection lost: " + e.getMessage());
                }
            }
        };
        new Thread(reader, "ReaderThread").start();
    }

    private void appendMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            chatArea.append("[" + timestamp + "] " + sender + ": " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void closeConnection() {
        isRunning = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            appendMessage("System", "🔌 Connection closed.");
        } catch (IOException e) {
            appendMessage("System", " Error while closing: " + e.getMessage());
        }
        disableInput();
        statusLabel.setText(" Disconnected");
    }

    private void disableInput() {
        messageField.setEditable(false);
        sendButton.setEnabled(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }
}
