package Wikiplay;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import static java.lang.Thread.interrupted;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Endika
 */
public class Room extends Thread {

    final List<ClientThread> clients = new LinkedList<>();
    public Match match;
    public Server server;

    public void setMatch(Match match) {
        this.match = match;
    }

    public Room(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        try ( ServerSocket serverSocket = new ServerSocket(server.getPort());) {
            System.out.println("Started Room server on port " + server.getPort());
            // repeatedly wait for connections
            while (!interrupted()) {
                Socket clientSocket = serverSocket.accept();
                Player newPlayer = new Player(randomName(), clientSocket.getInetAddress().toString());
                server.addPlayer(newPlayer);

                ClientThread clientThread = new ClientThread(clients, clientSocket, newPlayer);
                clientThread.start();
            }
        } catch (Exception ex) {
            System.out.println("Error in run de Room" + ex);
        }
    }

    public String randomName() {
        return "user_" + server.getNumPlayers();
    }

    public class ClientThread extends Thread {

        final List<ClientThread> clients;
        public Player player;
        final Socket socket;        
        private ObjectOutputStream out;
        public List<String> wordTrack = new ArrayList<>();
        public String currentWord = "STARTING POINT";

        public ClientThread(List<ClientThread> clients, Socket socket, Player player) {
            this.clients = clients;
            this.socket = socket;
            this.player = player;
        }

        synchronized public void sendMsg(Response res) {
            try {
                out.writeObject(res);
            } catch (IOException ex) {
                Logger.getLogger(ServerList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            try {
                System.out.println("Connection from "
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

                    if (lineParts[0].equals("/createMatch")) {
                        Match newMatch = new Match("Pineapple", "Apple");
                        setMatch(newMatch);
                        Response res = new Response(newMatch,true,"createMatch" , "createMatch");
                        synchronized (clients) { //we must sync because other clients may be iterating over it
                            for (ClientThread c : clients) {
                                c.wordTrack.add(newMatch.Word1);
                                c.sendMsg(res);
                            }
                        }

                    } else if (lineParts[0].equals("/getMatch")) {
                        Response res = new Response(match,true, "get done" ,"getMatch");                                              
                        this.sendMsg(res);
                        
                    }else if (lineParts[0].equals("/win")) { //win playername
                        
                        String namesAndCurrentWords = "PLAYER: " + lineParts[1] + " WON WITH THE TRACE: ";
                        
                        System.out.println("TRACKS WIN");
                        for (String s : this.wordTrack) {                            
                            System.out.println(s);
                        } 
                        
                        for (String s : this.wordTrack) {                            
                            namesAndCurrentWords +=( s + " --> ");
                        } 
                        namesAndCurrentWords +=(" WIN ");
                        
                        Response res = new Response(null,false,namesAndCurrentWords , "win");
                        synchronized (clients) { 
                            for (ClientThread c : clients) {
                                c.sendMsg(res);
                            }
                        }
                        
                    } else if (lineParts[0].equals("/setCurrentWord")) {                     
                        this.currentWord = lineParts[1];                       
                        this.wordTrack.add(currentWord);
                        
                        String namesAndCurrentWords = "";
                        
                        for (ClientThread c : clients) {
                            System.out.println(namesAndCurrentWords);
                            namesAndCurrentWords +=( " PLAYER: "+c.player.name+" IN WORD: "+c.currentWord + " \n");
                        }                        
                        
                        Response res = new Response(null,false,namesAndCurrentWords , "track");
                        synchronized (clients) { 
                            for (ClientThread c : clients) {
                                c.sendMsg(res);
                            }
                        }
                                                
                    } else if (lineParts[0].equals("/connect")) { //connect name ip                       
                        this.player.setName(lineParts[1]);
                        this.player.setIp(lineParts[2]);               
                        
                    }
                }

            } catch (Exception ex) {
                System.out.println("Error in run clientThread room" + ex);
            } finally { //we have finished or failed so let's close the socket and remove ourselves from the list
                try {
                    socket.close();
                } catch (Exception ex) {
                } //this will make sure that the socket closes
                synchronized (clients) {
                    server.popPlayer(this.player);
                    clients.remove(this);
                }
            }
        }

    }

}
