import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ChatClientGUI extends JFrame {
    private JTextField txtIP, txtPort, txtMessage;  // Campos de texto para ingresar IP, puerto y mensajes
    private JButton btnConnect;  // Botón para conectar/desconectar
    private JTextArea chatArea;  // Área de texto para mostrar los mensajes del chat
    private JTextField txtUsername;  // Campo de texto para el nombre de usuario

    private Socket socket;  // Socket para la conexión con el servidor
    private PrintWriter out;  // Flujo de salida para enviar mensajes al servidor
    private BufferedReader in;  // Flujo de entrada para recibir mensajes del servidor
    private Thread listenThread;  // Hilo para escuchar los mensajes entrantes del servidor

    private boolean connected = false;  // Bandera para saber si el cliente está conectado

    public ChatClientGUI() {
        setTitle("Chat Cliente");  // Título de la ventana
        setSize(500, 400);  // Tamaño de la ventana
        setDefaultCloseOperation(EXIT_ON_CLOSE);  // Acción al cerrar la ventana
        setLayout(new BorderLayout());  // Layout de la ventana (distribución de los componentes)

        // Panel superior con campos de IP, puerto y nombre de usuario
        JPanel topPanel = new JPanel();
        txtIP = new JTextField("127.0.0.1", 10);  // IP por defecto (localhost)
        txtPort = new JTextField("9090", 5);  // Puerto por defecto
        txtUsername = new JTextField("Usuario", 8);  // Nombre de usuario por defecto
        btnConnect = new JButton("Conectar");  // Botón para conectar/desconectar
        topPanel.add(new JLabel("IP:"));
        topPanel.add(txtIP);
        topPanel.add(new JLabel("Puerto:"));
        topPanel.add(txtPort);
        topPanel.add(new JLabel("Nombre:"));
        topPanel.add(txtUsername);
        topPanel.add(btnConnect);
        topPanel.setPreferredSize(new Dimension(0, 60));
        add(topPanel, BorderLayout.NORTH);

        // Área de chat donde se mostrarán los mensajes
        chatArea = new JTextArea();
        chatArea.setEditable(false);  // No se puede editar directamente desde el área de chat
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Panel inferior con campo para el mensaje y botón para enviarlo
        JPanel bottomPanel = new JPanel();
        txtMessage = new JTextField(30);
        JButton btnSend = new JButton("Enviar");
        bottomPanel.add(txtMessage);
        bottomPanel.add(btnSend);
        add(bottomPanel, BorderLayout.SOUTH);

        // Eventos (Acciones de los botones)
        btnConnect.addActionListener(e -> toggleConnection());  // Conectar/desconectar
        btnSend.addActionListener(e -> sendMessage());  // Enviar mensaje
        txtMessage.addActionListener(e -> sendMessage());  // Enviar mensaje al presionar Enter
    }

    // Método para conectar o desconectar del servidor
    private void toggleConnection() {
        if (!connected) {  // Si no está conectado, intenta conectar
            try {
                // Crea el socket con la IP y el puerto proporcionados
                socket = new Socket(txtIP.getText(), Integer.parseInt(txtPort.getText()));
                out = new PrintWriter(socket.getOutputStream(), true);  // Flujo de salida
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // Flujo de entrada

                // Envía el nombre de usuario al servidor
                String username = txtUsername.getText().trim();
                if (username.isEmpty()) username = "Anónimo";  // Si no se proporciona un nombre, se usa "Anónimo"
                out.println(username);

                // Hilo para escuchar los mensajes del servidor
                listenThread = new Thread(() -> {
                    try {
                        String msg;
                        while ((msg = in.readLine()) != null) {  // Lee los mensajes del servidor
                            chatArea.append(msg + "\n");  // Muestra los mensajes en el área de chat
                        }
                    } catch (IOException e) {
                        chatArea.append("Conexión cerrada.\n");  // Muestra mensaje cuando la conexión se cierra
                    }
                });
                listenThread.start();  // Inicia el hilo para escuchar mensajes

                chatArea.append("Conectado al servidor como " + username + ".\n");  // Notifica que se ha conectado
                btnConnect.setText("Desconectar");  // Cambia el texto del botón
                connected = true;  // Establece la bandera de conexión
            } catch (Exception ex) {  // Si ocurre un error al conectar, muestra un mensaje
                JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
            }
        } else {  // Si ya está conectado, desconecta
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
                if (listenThread != null) listenThread.interrupt();
                chatArea.append("Desconectado del servidor.\n");
                btnConnect.setText("Conectar");
                connected = false;
            } catch (IOException e) {
                // Si ocurre un error al desconectar, no hace nada
            }
        }
    }

    // Método para enviar un mensaje al servidor
    private void sendMessage() {
        if (connected && !txtMessage.getText().trim().isEmpty()) {
            String msg = txtMessage.getText();  // Obtiene el mensaje
            out.println(msg);  // Envía el mensaje al servidor
            chatArea.append("Tú: " + msg + "\n");  // Muestra el mensaje en el área de chat
            txtMessage.setText("");  // Limpia el campo de texto para nuevos mensajes
        }
    }

    // Método principal para ejecutar la interfaz gráfica
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientGUI().setVisible(true));  // Ejecuta el cliente en el hilo de la interfaz gráfica
    }
}
