package Sockets;

import java.net.*;
import java.io.*;
import java.util.*;

public class ServerThread extends Thread {
    private Socket socket;
    private List<ServerThread> clients;
    private PrintWriter out;
    private String username = "Anónimo";

    public ServerThread(Socket socket, List<ServerThread> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Lee el nombre de usuario del cliente
            String message;
            username = in.readLine();
            broadcast(username + " se ha unido al chat.");

            // Bucle para leer mensajes del cliente y retransmitirlos a los demás
            while ((message = in.readLine()) != null) {
                System.out.println(username + " envió un mensaje");

                if (message.startsWith("IMG:")) {
                    // Es una imagen
                    System.out.println(username + " envió una imagen");
                    broadcast(username + ": [Imagen]"); // Para mostrar en consola del servidor
                    broadcastImage(message); // Enviar la imagen codificada
                } else {
                    // Es un mensaje de texto normal
                    System.out.println(username + ": " + message);
                    broadcast(username + ": " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + username);
        } finally {
            clients.remove(this);
            broadcast(username + " ha salido del chat.");
            try {
                socket.close();
            } catch (IOException e) {
                // Ignorar error al cerrar socket
            }
        }
    }

    // Método para enviar un mensaje de texto a todos los clientes conectados, excepto a este hilo
    private void broadcast(String message) {
        for (ServerThread client : clients) {
            if (client != this) {
                client.out.println(message);
            }
        }
    }

    // Método para enviar una imagen a todos los clientes conectados, excepto a este hilo
    private void broadcastImage(String imageData) {
        for (ServerThread client : clients) {
            if (client != this) {
                client.out.println(imageData); // Enviamos la imagen codificada en base64
            }
        }
    }
}