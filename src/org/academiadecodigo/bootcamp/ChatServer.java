package org.academiadecodigo.bootcamp;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

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
    private CopyOnWriteArrayList<ClientConnection> connections = new CopyOnWriteArrayList();
    private Socket socket;


    public void start() throws IOException {
        init();
        run();

    }


    public void init() throws IOException {
        serverSocket = new ServerSocket(5555);
    }

    public synchronized void run() throws IOException {

        while (serverSocket.isBound()) {

            socket = serverSocket.accept();

            ClientConnection clientConnection = new ClientConnection(socket);

            connections.add(clientConnection);
            ExecutorService fixedPool = Executors.newFixedThreadPool(10);
            fixedPool.submit(clientConnection);

        }

    }

    public void sendAll(String msg) {
        for (ClientConnection clientConnection : connections) {
            clientConnection.send(msg);

        }
    }

    public void sendPm(String nick, String msg) {
        for (ClientConnection clientConnection : connections) {
            if (clientConnection.getNick().equals(nick))
                clientConnection.send(msg);
        }

    }


    private class ClientConnection implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String nick = "No Nick";


        public ClientConnection(Socket socket) {
            this.socket = socket;
        }

        public void setNick(String nick) {

            this.nick = nick;
        }

        public String getNick() {
            return nick;
        }


        public void init() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public synchronized void run() {
            try {
                init();
                out.println("Welcome to the chat: Tell Me your Nickname: ");
                nick = in.readLine();


            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                try {
                    String msg = in.readLine();

                    if (msg.startsWith("/")) {
                        System.out.println("no if");
                        commandsAction(msg);
                        continue;

                    }

                    sendAll(nick + ": " + msg);


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        public void commandsAction(String msg) throws IOException {

            String[] command = msg.split(" ");
            String pmMessage = "";

            for (int i = 2; i < command.length; i++) {
                pmMessage += command[i] + " ";
            }

            System.out.println("entrou nos commandos");
            switch (command[0]) {
                case "/quit":
                    sendAll(nick + " Disconnected");
                    socket.close();
                    in.close();
                    out.close();
                    Thread.currentThread().stop();
                    break;
                case "/nickserv":
                    nick = command[1];
                    setNick(nick);
                    out.println("Nick Changed");
                    break;
                case "/pm":
                    sendPm(command[1], pmMessage);
                    break;
                case "/list":

            }
        }

        public void send(String msg) {
            out.println(msg);

        }


    }


    public static void main(String[] args) throws IOException {

        ChatServer server = new ChatServer();
        server.start();

    }


}
