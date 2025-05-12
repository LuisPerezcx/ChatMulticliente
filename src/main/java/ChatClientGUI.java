import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ChatClientGUI extends JFrame {

    private JTextField txtIP, txtPort, txtMessage;
    private JButton btnConnect;
    private JTextArea chatArea;
    private JTextField txtUsername;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenThread;

    private boolean connected = false;

    public ChatClientGUI() {
        setTitle("Chat Cliente");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior
        JPanel topPanel = new JPanel();
        txtIP = new JTextField("127.0.0.1", 10);
        txtPort = new JTextField("9090", 5);
        txtUsername = new JTextField("Usuario", 8); // nuevo campo
        btnConnect = new JButton("Conectar");
        topPanel.add(new JLabel("IP:"));
        topPanel.add(txtIP);
        topPanel.add(new JLabel("Puerto:"));
        topPanel.add(txtPort);
        topPanel.add(new JLabel("Nombre:"));
        topPanel.add(txtUsername);
        topPanel.add(btnConnect);

        topPanel.setPreferredSize(new Dimension(0, 60)); // ancho 0 = ajustable
        add(topPanel, BorderLayout.NORTH);

        // Área de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Panel inferior
        JPanel bottomPanel = new JPanel();
        txtMessage = new JTextField(30);
        JButton btnSend = new JButton("Enviar");
        bottomPanel.add(txtMessage);
        bottomPanel.add(btnSend);

        add(bottomPanel, BorderLayout.SOUTH);

        // Eventos
        btnConnect.addActionListener(e -> toggleConnection());
        btnSend.addActionListener(e -> sendMessage());
        txtMessage.addActionListener(e -> sendMessage());
    }

    private void toggleConnection() {
        if (!connected) {
            try {
                socket = new Socket(txtIP.getText(), Integer.parseInt(txtPort.getText()));
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String username = txtUsername.getText().trim();
                if (username.isEmpty()) username = "Anónimo";
                out.println(username);

                listenThread = new Thread(() -> {
                    try {
                        String msg;
                        while ((msg = in.readLine()) != null) {
                            chatArea.append(msg + "\n");
                        }
                    } catch (IOException e) {
                        chatArea.append("Conexión cerrada.\n");
                    }
                });
                listenThread.start();

                chatArea.append("Conectado al servidor como " + username + ".\n");
                btnConnect.setText("Desconectar");
                connected = true;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
            }
        } else {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
                if (listenThread != null) listenThread.interrupt();
                chatArea.append("Desconectado del servidor.\n");
                btnConnect.setText("Conectar");
                connected = false;
            } catch (IOException e) {
                //
            }
        }
    }

    private void sendMessage() {
        if (connected && !txtMessage.getText().trim().isEmpty()) {
            String msg = txtMessage.getText();
            out.println(msg);
            chatArea.append("Tú: " + msg + "\n");
            txtMessage.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientGUI().setVisible(true));
    }
}