package Wikiplay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerList extends Thread {

    public ArrayList<Server> servers = new ArrayList<Server>();

    public static void main(String[] args) {
        ServerList server = new ServerList(12002);
        server.start();
    }

    final int port;
    final List<ClientThread> clients = new LinkedList<>();

    public ServerList(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try ( ServerSocket serverSocket = new ServerSocket(port);) {
            System.out.println("Started serverlist server on port " + port);
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
        return "user_" + clients.size();
    }

    public class ClientThread extends Thread {

        final List<ClientThread> clients;
        final Socket socket;
        String name;
        ObjectOutputStream out;

        public ClientThread(List<ClientThread> clients, Socket socket, String name) {
            this.clients = clients;
            this.socket = socket;
            this.name = name;
        }

        //only one thread at the time can send messages through the socket
        synchronized public void sendMsg(ArrayList<Server> servers) {
            try {
                out.writeObject(servers);
            } catch (IOException ex) {
                Logger.getLogger(ServerList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            try {
                System.out.println("Connection to serverList server from "
                    + socket.getInetAddress() + ":" + socket.getPort());

                DataInputStream in = new DataInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());

                //now that we have managed to stablish proper connection, we add ourselve into the list
                synchronized (clients) { //we must sync because other clients may be iterating over it
                    clients.add(this);
                }
                for (String line; (line = in.readUTF()) != null;) {
                    System.out.println(line);
                    String[] lineParts = line.split(" ");
                    if (lineParts[0].equals("/get")) {
                        System.out.println(servers);
                        this.sendMsg(servers);
                    } else if (lineParts[0].equals("/set")) { ///set name descrp ip port maxplayers                       
                        boolean roomExist = false;
                        for (Server server : servers) {
                            if (server.getIp().equals(lineParts[3])) {
                                roomExist = true;
                                break;
                            }
                        }
                        if (roomExist == false) {
                            servers.add(new Server(lineParts[1], lineParts[2], lineParts[3], Integer.parseInt(lineParts[4]), Integer.parseInt(lineParts[5])));
                            System.out.println("Adding room to list, ROOM: " + lineParts[1]);
                        }
                    } else if (lineParts[0].equals("/join")) { ///join name ip_cli ip_server  
                        System.out.println("TRYING TO JOIN");
                        boolean roomExist = false;
                        for (Server server : servers) {
                            if (server.getIp().equals(lineParts[3])) {                                
                                Player player = new Player(lineParts[1] , lineParts[2]);
                                server.addPlayer(player);
                                System.out.println("Adding player "+lineParts[1]+" to  ROOM: " + lineParts[3]);                                
                                break;
                            }
                        }                        
                    } else if (lineParts[0].equals("/exit")) { ///exit name ip_cli ip_server                
                        
                        for (Server server : servers) {
                            if (server.getIp().equals(lineParts[2])) {                              
                                
                                for(Player p:server.getPlayers())
                                {
                                    if(p.ip == lineParts[2] && p.name == lineParts[1])
                                    {
                                        server.popPlayer(p);
                                        break;
                                    }                                    
                                }
                                
                                System.out.println("Removing player "+lineParts[1]+" from  ROOM: " + lineParts[3]);
                                
                                break;
                            }
                        }                        
                    }else if (lineParts[0].equals("/close")) { //close ip                    
                        boolean roomExist = false;
                        int index = 0;
                        for (Server server : servers) {
                            if (server.getIp().equals(lineParts[1])) {
                                roomExist = true;
                                break;
                            }
                            index++;
                        }
                        if (roomExist == true) {                          
                            servers.remove(index);                                                    
                            System.out.println("Removing room off list, ROOM: " + lineParts[1]);
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
