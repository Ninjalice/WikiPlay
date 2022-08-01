/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package Wikiplay;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 *
 * @author Endika
 */
public class Join extends javax.swing.JPanel {

    public MainFrame mainFrame;
    public static String ip;
    public Match match ;
    public Player player;
    public Server myserver;
    
    private DataOutputStream ServerListOut;
    private Chrono chrono;
    private DataOutputStream roomOut;
    private JEditorPane jEditorPane ;
    
    public Join(MainFrame mainFrame,String ip ,String name) {       
        this.ip = ip;
        this.mainFrame = mainFrame;
        
        Random rand = new Random();        
        this.player = new Player(name,ip);
        
        initComponents();
        chrono = new Chrono(chronoLabel);
        startServerListClient();
        getServers();
        
        try {
            sleep(2000);
        } catch (Exception e) {
        }        
        
        joinServer(myserver.getIp());
        
        connectToRoom();
                
        chatPlacerPanel.setLayout(new BorderLayout());
        chatPlacerPanel.add(new ChatPanel(ip,name));
        
        loadweb();
    }
    
    public void connectToRoom(){
        try {                
        
            final String hostAddr = ip;
            final int port = 12001;
            final Scanner scanner = new Scanner(System.in);
            final Socket socket = new Socket(hostAddr, port);

            System.out.println("Room Socket connected to " + socket.getInetAddress() + ":" + socket.getPort());

            final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            roomOut = new DataOutputStream(socket.getOutputStream());        
            
            roomOut.writeUTF("/connect "+player.name + " " + player.ip);
            
            Thread recvThread = new Thread() { //we read on a different thread to avoid getting blocked by the keyboard reading
                public void run() {

                    while (!this.isInterrupted()) {
                        try {
                            
                            Response res = (Response) in.readObject();
                            System.out.println("We get response");
                           
                            if(res.isMatch )
                            {
                                System.out.println("MATCH");
                                match = res.match;
                                System.out.println(match.Word1);
                                System.out.println("MATCH CREATED From " + match.Word1 + " to " + match.Word2);
                                repaintWeb();
                                chrono.restartTimer();
                                chrono.startTimer();                                
                            }
                            else if(res.type.equals("track")){
                                System.out.println("TRACK: " + res.msg);
                                playerWordTrackArea.setText(res.msg);
                            } 
                            else if(res.type.equals("win")){
                                jEditorPane.setContentType("text/html");
                                jEditorPane.setText("<html>"+res.msg+"</html>");
                            } 
                           
                        } catch (ClassNotFoundException ex) {
                            System.out.println("Error conecting to RoomClass" + ex);
                        } catch (IOException ex) {
                            //System.out.println("Error conecting to RoomIO" + ex);
                        }

                    }

                }
            };
            recvThread.start();
        } catch (IOException ex) {
            System.out.println("Error conecting to Room" + ex);
        }
    }
    
    public void startServerListClient() {
        try {

            final String hostAddr = ip;
            final int port = 12002;
            final Socket socket = new Socket(hostAddr, port);
            System.out.println("Client connected to server list server  " + socket.getInetAddress() + ":" + socket.getPort());
            final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ServerListOut = new DataOutputStream(socket.getOutputStream());

            Thread recvThread = new Thread() { //we read on a different thread to avoid getting blocked by the keyboard reading
                public void run() {
                    while (!this.isInterrupted()) {
                        try {
                            ArrayList<Server> servers = (ArrayList<Server>) in.readObject();
                            for (Server s : servers) {
                                if (s.getIp().equals(ip)) {
                                    setMyserver(s);
                                }
                            }
                        } catch (ClassNotFoundException ex) {
                            System.out.println(ex);
                        } catch (IOException ex) {
                            System.out.println(ex);
                        }
                    }
                }
            };
            recvThread.start();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    public void getServers() {
        try {
            ServerListOut.writeUTF("/get " + ip);
        } catch (Exception e) {
        }
    }
    
    public void closeServer() {
        try {
            ServerListOut.writeUTF("/close " + myserver.getIp());
        } catch (Exception e) {
        }
    }
    
    public void joinServer(String serverip) {
        try {
            ServerListOut.writeUTF("/join " + player.name + " " + ip  + " "+ serverip);
        } catch (Exception e) {
        }
    }
    
    public void exitServer(String serverip) {
        try {
            ServerListOut.writeUTF("/join " + player.name + " " + ip  + " "+ serverip);
        } catch (Exception e) {
        }
    }
    
    
    public void setMyserver(Server server) {
        myserver = server;
    }
    
    
    public void changeOfURL(String url)
    {
        String parts[] = url.split("/");                    
        String item = parts[4];
        currentLabel.setText("CURRENT WORD: "+item);
        if (item.equals(match.Word2))
        {
            winLabel.setText("YOU WON !!!!!");
            try {
                sleep(800);
                roomOut.writeUTF("/win "+this.player.name);
            } catch (Exception e) {
            }
        }
    }
    public void repaintWeb() {
        JScrollPane auxPaneScroll = (JScrollPane) gamePlacerPanel.getComponent(0);
        JViewport viewport = auxPaneScroll.getViewport(); 
        JEditorPane auxPaneEditor = (JEditorPane) viewport.getView();
        try {
            auxPaneEditor.setPage("https://en.wikipedia.org/wiki/" + match.Word1);
            fromToLabel.setText("FROM: " + match.Word1 + " TO: " + match.Word2);
        } catch (Exception e) {
        }

    }
    public void loadweb() {
        System.out.println("LOADING WEB URL");
        jEditorPane = new JEditorPane();
        jEditorPane.setEditable(false);
        
        HTMLEditorKit kit = new HTMLEditorKit();
        jEditorPane.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("h1 {color: blue;}");
        Document doc = kit.createDefaultDocument();
        jEditorPane.setDocument(doc);

        
        jEditorPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent hle) {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                    System.out.println(hle.getURL()); 
                    try {
                        jEditorPane.setPage(hle.getURL());
                        String url = hle.getURL().toString();
                        String parts[] = url.split("/");
                        String word = parts[4];
                        roomOut.writeUTF("/setCurrentWord "+word);
                        changeOfURL(hle.getURL().toString());
                    } catch (Exception ex) {
                        System.out.println("Error in creating jEditorPane" + ex);
                    }
                }
            }
        });

        try {            
            if (match != null) {
                System.out.println("URL: " + "https://en.wikipedia.org/wiki/" + match.Word1);
                fromToLabel.setText("FROM: " + match.Word1 + " TO: " + match.Word2);
                jEditorPane.setPage("https://en.wikipedia.org/wiki/" + match.Word1);
            } else {
                jEditorPane.setContentType("text/html");
                jEditorPane.setText("<html>WATING FOR THE MATCH TO START BE PATIENT.</html>");
            }
        } catch (IOException e) {
            jEditorPane.setContentType("text/html");
            jEditorPane.setText("<html>Page not found.</html>");
        }

        JScrollPane jScrollPane = new JScrollPane(jEditorPane);
        jScrollPane.setPreferredSize(new Dimension(540, 400));
        
        gamePlacerPanel.setLayout(new BorderLayout());
        gamePlacerPanel.add(jScrollPane);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        buttonMenu = new javax.swing.JButton();
        chatPlacerPanel = new javax.swing.JPanel();
        fromToLabel = new javax.swing.JLabel();
        gamePlacerPanel = new javax.swing.JPanel();
        currentLabel = new javax.swing.JLabel();
        winLabel = new javax.swing.JLabel();
        chronoLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        playerWordTrackArea = new javax.swing.JTextArea();
        label1 = new java.awt.Label();

        jLabel1.setText("JOIN");

        buttonMenu.setText("MENU");
        buttonMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenuActionPerformed(evt);
            }
        });

        chatPlacerPanel.setBackground(new java.awt.Color(153, 255, 153));

        javax.swing.GroupLayout chatPlacerPanelLayout = new javax.swing.GroupLayout(chatPlacerPanel);
        chatPlacerPanel.setLayout(chatPlacerPanelLayout);
        chatPlacerPanelLayout.setHorizontalGroup(
            chatPlacerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 260, Short.MAX_VALUE)
        );
        chatPlacerPanelLayout.setVerticalGroup(
            chatPlacerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 151, Short.MAX_VALUE)
        );

        fromToLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        fromToLabel.setText("jLabel2");

        gamePlacerPanel.setBackground(new java.awt.Color(153, 153, 153));

        javax.swing.GroupLayout gamePlacerPanelLayout = new javax.swing.GroupLayout(gamePlacerPanel);
        gamePlacerPanel.setLayout(gamePlacerPanelLayout);
        gamePlacerPanelLayout.setHorizontalGroup(
            gamePlacerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        gamePlacerPanelLayout.setVerticalGroup(
            gamePlacerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 453, Short.MAX_VALUE)
        );

        currentLabel.setText("CURRENT");

        chronoLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        chronoLabel.setText("00:00:00");

        playerWordTrackArea.setEditable(false);
        playerWordTrackArea.setColumns(20);
        playerWordTrackArea.setRows(5);
        jScrollPane2.setViewportView(playerWordTrackArea);

        label1.setText("PLAYER LOCATIONS");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gamePlacerPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonMenu)
                        .addGap(82, 82, 82)
                        .addComponent(chronoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chatPlacerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(winLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fromToLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(currentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(buttonMenu))
                    .addComponent(chronoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(gamePlacerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(chatPlacerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fromToLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(currentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(winLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenuActionPerformed
        mainFrame.changeToMenu();
    }//GEN-LAST:event_buttonMenuActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonMenu;
    private javax.swing.JPanel chatPlacerPanel;
    private javax.swing.JLabel chronoLabel;
    private javax.swing.JLabel currentLabel;
    private javax.swing.JLabel fromToLabel;
    private javax.swing.JPanel gamePlacerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private java.awt.Label label1;
    private javax.swing.JTextArea playerWordTrackArea;
    private javax.swing.JLabel winLabel;
    // End of variables declaration//GEN-END:variables
}
