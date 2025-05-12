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
            String message;

            username = in.readLine();
            broadcast("🟢 " + username + " se ha unido al chat.");

            while ((message = in.readLine()) != null) {
                System.out.println(username + ": " + message);
                broadcast(username + ": " + message);
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + username);
        } finally {
            clients.remove(this);
            broadcast("🔴 " + username + " ha salido del chat.");
            try {
                socket.close();
            } catch (IOException e) {
                //
            }
        }
    }

    private void broadcast(String message) {
        for (ServerThread client : clients) {
            if (client != this) {
                client.out.println(message);
            }
        }
    }
}