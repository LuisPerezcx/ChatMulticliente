import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ChatClientGUI extends JFrame {
    private JTextField txtMessage;
    private JTextArea chatArea;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenThread;
    private boolean connected = false;
    private String ip;
    private int port;
    private String username;

    public ChatClientGUI(String ip, int port, String username) {
        this.ip = ip;
        this.port = port;
        this.username = username;

        setTitle("Chat Cliente - " + username);
        setSize(520, 430);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        // Área de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        chatArea.setBackground(new Color(245, 245, 245));
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 0));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        txtMessage = new JTextField();
        txtMessage.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtMessage.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 8, 5, 8)
        ));

        JButton btnSend = new JButton("Enviar");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSend.setBackground(new Color(0, 120, 215));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);

        bottomPanel.add(txtMessage, BorderLayout.CENTER);
        bottomPanel.add(btnSend, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Enviar mensaje al presionar botón o Enter
        btnSend.addActionListener(e -> sendMessage());
        txtMessage.addActionListener(e -> sendMessage());

        connectToServer();
    }

    // Conexión al servidor y escucha de mensajes
    private void connectToServer() {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            if (username.isEmpty()) username = "Anónimo";
            out.println(username);

            // Hilo para recibir mensajes del servidor
            listenThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        chatArea.append(msg + "\n");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    }
                } catch (IOException e) {
                    chatArea.append("Conexión cerrada.\n");
                }
            });
            listenThread.start();

            chatArea.append("Conectado al servidor como " + username + ".\n");
            connected = true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    // Envía el mensaje al servidor y lo muestra en el área de chat
    private void sendMessage() {
        if (connected && !txtMessage.getText().trim().isEmpty()) {
            String msg = txtMessage.getText();
            out.println(msg);
            chatArea.append("Tú: " + msg + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
            txtMessage.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);
            if (login.isConfirmed()) {
                String ip = login.getIP();
                int port = Integer.parseInt(login.getPort());
                String username = login.getUsername();
                new ChatClientGUI(ip, port, username).setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}