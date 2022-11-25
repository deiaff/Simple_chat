package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private static int portNumber = 4444;
    private ServerSocket serverSocket = null;
    private ExecutorService executor;
    protected CopyOnWriteArrayList<ClientThread> clients = new CopyOnWriteArrayList();


    public static void main(String[] args) {

        ChatServer chatServer = new ChatServer();
        chatServer.listen(portNumber);

    }

    private void listen(int portNumber) {

        try {
            serverSocket = new ServerSocket(portNumber);
            acceptClients();

        } catch (IOException e) {
            System.out.println("Could not listen on port: " + portNumber);
            System.exit(1);
        }

    }

    private void acceptClients() {

        executor = Executors.newCachedThreadPool();

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                ClientThread client = new ClientThread(socket);
                clients.add(client);
                executor.submit(client);

            } catch (IOException e) {
                System.out.println("Accept failed on: " + portNumber);
            }
        }
    }
    private class ClientThread implements Runnable{

        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String name;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {


            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("Please input username");
                name = in.readLine();
                out.println("Welcome " + name + ", to deiaff chatRoom.");

                while (!socket.isClosed()) {
                    String input = in.readLine();

                    if(input != null) {
                        for (ClientThread client : clients) {
                            if (client != this) {
                                client.getWriter().println("< " + getName() + " > : " + input);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public PrintWriter getWriter() {
            return out;
        }

        public String getName() {
            return name;
        }
    }
}
