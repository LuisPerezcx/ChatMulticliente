import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import java.util.Base64;

public class ChatClientGUI extends JFrame {
    private JTextField txtMessage;
    private JTextPane chatPane; // Cambiado de JTextArea a JTextPane
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenThread;
    private boolean connected = false;
    private String ip;
    private int port;
    private String username;
    private JButton btnSend;
    private JButton btnImage;

    public ChatClientGUI(String ip, int port, String username) {
        this.ip = ip;
        this.port = port;
        this.username = username;

        setTitle("Chat Cliente - " + username);
        setSize(520, 430);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        // Área de chat con JTextPane
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        chatPane.setBackground(new Color(245, 245, 245));
        chatPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatPane);
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

        btnSend = new JButton("Enviar");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSend.setBackground(new Color(0, 120, 215));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);

        btnImage = new JButton("Imagen");
        btnImage.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnImage.setBackground(new Color(0, 180, 120));
        btnImage.setForeground(Color.WHITE);
        btnImage.setFocusPainted(false);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.add(btnImage);
        buttonPanel.add(btnSend);

        bottomPanel.add(txtMessage, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Eventos
        btnSend.addActionListener(e -> sendMessage());
        txtMessage.addActionListener(e -> sendMessage());
        btnImage.addActionListener(e -> sendImage());

        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            if (username.isEmpty()) username = "Anónimo";
            out.println(username);

            // Crear hilo para escuchar mensajes entrantes
            listenThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        if (msg.startsWith("IMG:")) {
                            // Si es una imagen, procesarla
                            displayImage(msg.substring(4), false);
                        } else {
                            // Si es texto, mostrarlo
                            appendToChat(msg + "\n", false);
                        }
                    }
                } catch (IOException e) {
                    appendToChat("Conexión cerrada.\n", false);
                }
            });
            listenThread.start();

            appendToChat("Conectado al servidor como " + username + ".\n", false);
            connected = true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void sendMessage() {
        if (connected && !txtMessage.getText().trim().isEmpty()) {
            String msg = txtMessage.getText();
            out.println(msg);
            appendToChat("Tú: " + msg + "\n", true);
            txtMessage.setText("");
        }
    }

    private void sendImage() {
        if (!connected) return;

        // Selector de archivos configurado para imágenes
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar imagen");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // Leer la imagen seleccionada
                BufferedImage image = ImageIO.read(selectedFile);

                // Convertir imagen a Base64 para enviarla como texto
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                // Enviar imagen codificada al servidor
                out.println("IMG:" + base64Image);
                // Mostrar la imagen en el chat
                displayImage(base64Image, true);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar la imagen: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Método modificado para mostrar la imagen en el chat
    private void displayImage(String base64Image, boolean isMyMessage) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Decodificar imagen desde Base64
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

                // Escalar imagen si es muy grande
                int maxWidth = 200;
                int maxHeight = 200;

                if (bufferedImage.getWidth() > maxWidth || bufferedImage.getHeight() > maxHeight) {
                    double scale = Math.min((double)maxWidth / bufferedImage.getWidth(),
                                          (double)maxHeight / bufferedImage.getHeight());
                    int scaledWidth = (int)(bufferedImage.getWidth() * scale);
                    int scaledHeight = (int)(bufferedImage.getHeight() * scale);

                    Image scaledImage = bufferedImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                    BufferedImage resized = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
                    resized.getGraphics().drawImage(scaledImage, 0, 0, null);
                    bufferedImage = resized;
                }

                // Insertar mensaje de quién envió la imagen
                StyledDocument doc = chatPane.getStyledDocument();
                String sender = isMyMessage ? "Tú: " : username + ": ";

                // Estilo para el remitente
                Style style = chatPane.addStyle("senderStyle", null);
                StyleConstants.setBold(style, true);
                doc.insertString(doc.getLength(), sender, style);

                // Insertar salto de línea
                doc.insertString(doc.getLength(), "\n", null);

                // Insertar imagen
                Style imgStyle = chatPane.addStyle("ImageStyle", null);
                ImageIcon icon = new ImageIcon(bufferedImage);
                StyleConstants.setIcon(imgStyle, icon);
                doc.insertString(doc.getLength(), ".", imgStyle);

                // Salto de línea después de la imagen
                doc.insertString(doc.getLength(), "\n\n", null);

                // Desplazar hacia abajo
                chatPane.setCaretPosition(chatPane.getDocument().getLength());

            } catch (Exception ex) {
                try {
                    chatPane.getStyledDocument().insertString(
                        chatPane.getDocument().getLength(),
                        "Error al mostrar la imagen\n",
                        null
                    );
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Método para añadir texto al chat
    private void appendToChat(String text, boolean isMyMessage) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = chatPane.getStyledDocument();
                Style style = chatPane.addStyle("Style", null);

                if (isMyMessage && text.startsWith("Tú: ")) {
                    // Dividir el mensaje para aplicar estilo solo al "Tú:"
                    String[] parts = text.split(":", 2);
                    if (parts.length > 1) {
                        StyleConstants.setBold(style, true);
                        doc.insertString(doc.getLength(), parts[0] + ":", style);

                        StyleConstants.setBold(style, false);
                        doc.insertString(doc.getLength(), parts[1], style);
                    } else {
                        doc.insertString(doc.getLength(), text, null);
                    }
                } else {
                    doc.insertString(doc.getLength(), text, null);
                }

                chatPane.setCaretPosition(chatPane.getDocument().getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
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