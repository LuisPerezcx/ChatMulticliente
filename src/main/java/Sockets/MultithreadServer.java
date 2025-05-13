/* Chat grupal
Yenisleydi Benitez Martinez
Luis David Perez Cruz
*/

package Sockets;

import java.net.*;
import java.util.*;

public class MultithreadServer {
    public static void main(String[] args) {
        List<ServerThread> clients = new ArrayList<>();// Lista para almacenar las conexiones de los clientes (hilos)

        try (ServerSocket serverSocket = new ServerSocket(9090)) {  // Crea un ServerSocket en el puerto 9090
            System.out.println("Servidor iniciado en el puerto 9090...");
            while (true) {  // Bucle infinito para aceptar clientes constantemente
                Socket socket = serverSocket.accept();  // Espera y acepta la conexión de un cliente
                System.out.println("Cliente conectado: " + socket.getInetAddress());  // Muestra la dirección IP del cliente que se conecta
                ServerThread thread = new ServerThread(socket, clients);  // Crea un nuevo hilo (ServerThread) para manejar al cliente, pasándole el socket y la lista de clientes
                clients.add(thread);  // Agrega el nuevo hilo a la lista de clientes
                thread.start();  // Inicia el hilo para que comience a manejar la comunicación con el cliente
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}