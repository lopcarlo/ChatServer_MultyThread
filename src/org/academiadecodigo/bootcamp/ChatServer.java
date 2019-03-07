package org.academiadecodigo.bootcamp;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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
    private ExecutorService fixedPool;

    public void start() throws IOException {
        init();
        run();

    }


    public void init() throws IOException {
        serverSocket = new ServerSocket(6666);
        System.out.println(serverSocket.getInetAddress());
    }

    public synchronized void run() throws IOException {

        while (serverSocket.isBound()) {
            fixedPool = Executors.newFixedThreadPool(50);
            socket = serverSocket.accept();
            ClientConnection clientConnection = new ClientConnection(socket);
            connections.add(clientConnection);
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

    public void kick(String nick) throws IOException {
        for (ClientConnection clientConnection : connections) {
            if (clientConnection.getNick().equals(nick))
                clientConnection.close();

        }
    }

    public void whipe(String nick) {
        for(ClientConnection clientConnection : connections){
            if(clientConnection.getNick().equals(nick))
            connections.remove(clientConnection);
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
                System.out.println(getNick() + " Connected");
                welcome();
                list();
                out.println("Tell Me your Nickname: ");
                nick = in.readLine();
                if (nick.equals("")) {
                    out.println("Nick name invalid, changed to jackass");
                    nick = "jackass";
                }
                System.out.println(nick + " Connected");


            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!socket.isClosed()) {
                try {

                    String msg = in.readLine();

                    if (msg.startsWith("/")) {
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
            String message = "*PM*" + getNick() + ": " + pmMessage;

            System.out.println("entrou nos commandos");
            switch (command[0]) {
                case "/quit":
                    sendAll(nick + " Disconnected");
                    whipe(getNick());
                    close();
                    Thread.currentThread().stop();
                    break;
                case "/nickserv":
                    nick = command[1];
                    setNick(nick);
                    out.println("Nick Changed");
                    break;
                case "/pm":
                    sendPm(command[1], message);
                    break;
                case "/list":
                    list();
                    break;
                case "/users":
                    out.println("----------Users Online----------------");
                    usersOnline();
                    break;
                case "/kick":
                    kick(command[1]);
                    whipe(command[1]);
                    out.println(getNick() + " kicked " + command[1]);
                    break;
                default:
                    out.println("Command not found");
                    list();

            }
        }

        public void send(String msg) {
            out.println(msg);

        }


        public void usersOnline() {
            for (ClientConnection clientConnection : connections) {
                out.println(clientConnection.getNick());
            }

        }


        public void list() {

            out.println("##############################################################################################");
            out.println("# List of available commands                                                                 #");
            out.println("# /quit                   => exist the program                                               #");
            out.println("# /nickserv <newnick>     => changes your nick                                               #");
            out.println("# /pm <nick> <message>    => sends private message to user                                   #");
            out.println("# /list                   => list all commands                                               #");
            out.println("# /users                  => list users Online                                               #");
            out.println("##############################################################################################");

        }

        public void welcome() throws IOException {
            BufferedImage image = new BufferedImage(144, 32, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            g.setFont(new Font("Dialog", Font.PLAIN, 24));
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.drawString("Welcome!", 5, 20);
            ImageIO.write(image, "png", new File("text.png"));

            for (int y = 0; y < 32; y++) {
                StringBuilder sb = new StringBuilder();
                for (int x = 0; x < 144; x++)
                    sb.append(image.getRGB(x, y) == -16777216 ? " " : image.getRGB(x, y) == -1 ? "#" : "*");
                if (sb.toString().trim().isEmpty()) continue;
                out.println(sb);
            }

        }


        public void close() throws IOException {
            socket.close();
            in.close();
            out.close();


        }


    }


    public static void main(String[] args) throws IOException {

        ChatServer server = new ChatServer();
        server.start();

    }


}
