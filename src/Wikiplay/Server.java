/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Wikiplay;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Endika
 */
public class Server implements Serializable{    
    private String name;
    private String description;
    private String ip;
    private int port;    
    private ArrayList<Player> players = new ArrayList<Player>();
    private int maxPlayers;

    public Server(String name, String description, String ip, int port, int maxPlayers) {
        this.name = name;
        this.description = description;
        this.ip = ip;
        this.port = port;
        this.maxPlayers = maxPlayers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
    
    public void addPlayer( Player newplayer) {
        players.add(newplayer);
    }
    
    public void popPlayer( Player player) {
        players.remove(player);
    }
    
    public int getNumPlayers() {
        return players.size();        
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    
    @Override
    public String toString() {
        String aux1 = ip +":" + port + " ";
        String aux2 = getNumPlayers()+"/"+maxPlayers;
        return String.format("%-20s%-15s%-10s%-5s",name , description , aux1 , aux2);
    }
    
}
