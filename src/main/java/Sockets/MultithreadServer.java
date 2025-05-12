package Sockets;

import java.net.*;
import java.util.*;
import java.io.*;

public class MultithreadServer {
    public static void main(String[] args) {
        List<ServerThread> clients = new ArrayList<>();
        try (ServerSocket serverSocket = new ServerSocket(9090)) {
            System.out.println("Servidor iniciado en el puerto 9090...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress());
                ServerThread thread = new ServerThread(socket, clients);
                clients.add(thread);
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}