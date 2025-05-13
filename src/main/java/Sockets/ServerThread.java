package Sockets;

import java.net.*;
import java.io.*;
import java.util.*;

public class ServerThread extends Thread {
    private Socket socket;  // Instancia para el socket de la conexión con el cliente
    private List<ServerThread> clients;  // Lista para almacenar los hilos de los clientes conectados
    private PrintWriter out;  // Objeto para enviar mensajes al cliente
    private String username = "Anónimo";  // Nombre de usuario del cliente, por defecto "Anónimo"

    public ServerThread(Socket socket, List<ServerThread> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    public void run() {
        try {
            // Inicialización de los flujos de entrada y salida para el socket
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);// Habilita la salida automática del flujo

            // Lee el nombre de usuario del cliente
            String message;
            username = in.readLine();  // El primer mensaje enviado es el nombre del usuario
            broadcast(username + " se ha unido al chat.");  // Notifica que el usuario se ha conectado

            // Bucle para leer mensajes del cliente y retransmitirlos a los demás
            while ((message = in.readLine()) != null) {
                System.out.println(username + ": " + message);  // Muestra el mensaje del cliente en consola
                broadcast(username + ": " + message);  // Envía el mensaje a todos los clientes conectados
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + username);// Notifica que un cliente se ha desconectado
        } finally {
            clients.remove(this);
            broadcast(username + " ha salido del chat.");// Notifica que el cliente ha dejado el chat
            try {
                socket.close(); // Cierra el socket de la conexión
            } catch (IOException e) {
                //
            }
        }
    }
    // Métod0 para enviar un mensaje a todos los clientes conectados, excepto a este hilo
    private void broadcast(String message) {
        for (ServerThread client : clients) {
            if (client != this) {  // Asegura que no se envíe el mensaje al propio cliente
                client.out.println(message);  // Envía el mensaje al cliente
            }
        }
    }
}