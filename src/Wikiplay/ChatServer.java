package Wikiplay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServer extends Thread {

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }

    final int port = 12000;
    final List<ClientThread> clients = new LinkedList<>();

    public ChatServer() {       
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port);) {
            System.out.println("Started Chat server on port " + port);
            // repeatedly wait for connections
            while (!interrupted()) {
                Socket clientSocket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(clients, clientSocket, randomName());
                clientThread.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String randomName() {               
        return "user_"+clients.size();
    }   
    

    public class ClientThread extends Thread {

        final List<ClientThread> clients;
        final Socket socket;
        String name;
        DataOutputStream out;

        public ClientThread(List<ClientThread> clients, Socket socket, String name) {
            this.clients = clients;
            this.socket = socket;
            this.name = name;
        }

        //only one thread at the time can send messages through the socket
        synchronized public void sendMsg(String msg) {
            try {
                out.writeUTF(msg);
            } catch (IOException ex) {
                Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            try {
                System.out.println("Connection to ChatServer from "
                        + socket.getInetAddress() + ":" + socket.getPort());

                DataInputStream in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                //now that we have managed to stablish proper connection, we add ourselve into the list
                synchronized (clients) { //we must sync because other clients may be iterating over it
                    clients.add(this);
                }
                this.sendMsg("Type /help to show the available commands !!!");
                for (String line; (line = in.readUTF()) != null;) {
                    String[] lineParts = line.split(" ");
                    if (lineParts[0].equals("/help")) {
                        this.sendMsg("-------------HELP--------------");
                        this.sendMsg("COMANDS:");                        
                        this.sendMsg("/list  ----> Lists all online players");
                        this.sendMsg("/nickname xXxXx ----> Allows you to change nickname");
                        this.sendMsg("/msg playername message_text ----> Allows you to send a private message to a player");
                        this.sendMsg("/quit ----> Quit");
                    }
                    else if (lineParts[0].equals("/list")) {
                        clients.forEach(c -> this.sendMsg(c.name));
                    }
                    else if(line.contains("/msg")){                        
                        for (ClientThread client : clients) {
                            if (client.name.equals(lineParts[1])){
                                client.sendMsg(this.name+" sends: "+lineParts[2]);
                            }
                        }
                    }
                    else if(lineParts[0].equals("/quit")){
                        this.socket.close();
                    }
                    else if(lineParts[0].equals("/nickname")){                        
                        this.name = lineParts[1];
                    }                    
                    else {
                        String mayus = this.name + ": " + line.toUpperCase();
                        //when we read a line we send it to the rest of the clients in mayus
                        synchronized (clients) { //other clients may be trying to add to the list
                            clients.forEach(c -> c.sendMsg(mayus));
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally { //we have finished or failed so let's close the socket and remove ourselves from the list
                try {
                    socket.close();
                } catch (Exception ex) {
                } //this will make sure that the socket closes
                synchronized (clients) {
                    clients.remove(this);
                }
            }
        }

    }

}
