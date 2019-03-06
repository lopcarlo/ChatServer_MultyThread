package org.academiadecodigo.bootcamp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by codecadet on 06/03/2019.
 */
public class ChatServer {
    private ServerSocket serverSocket;
    private CopyOnWriteArrayList<ClientConnection> arr = new CopyOnWriteArrayList();
    private Socket socket;


    public void start() throws IOException {
       init();
       run();

    }


    public void init() throws IOException{
        serverSocket = new ServerSocket(5555);
    }

    public synchronized void run() throws IOException {


        while(true) {
            socket = serverSocket.accept();
            ClientConnection clientConnection = new ClientConnection(socket);

            System.out.println(socket);
            arr.add(clientConnection);
            ExecutorService fixedPool = Executors.newFixedThreadPool(200);
            fixedPool.submit(clientConnection);

        }

    }

    public void sendAll (String msg){
        for (ClientConnection clientConnection:arr) {
            System.out.println("sending all");
            System.out.println(clientConnection);
            clientConnection.send(msg);

        }
    }

    private class ClientConnection implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientConnection(Socket socket) {
            this.socket= socket;
        }

        public synchronized void init() throws IOException {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

        }

        @Override
        public synchronized void run() {
            try {

                init();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while(true){
                try {
                    String msg = in.readLine();
                    System.out.println(msg);
                    sendAll(msg);


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        public void send(String msg){
            System.out.println("Try to Send");
            out.println(msg);


        }

        @Override
        public String toString() {
            return socket.toString();
        }
    }


    public static void main(String[] args) throws IOException {

        ChatServer server = new ChatServer();
        server.start();

    }





}
