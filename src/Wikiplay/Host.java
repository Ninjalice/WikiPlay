package Wikiplay;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import static java.lang.Thread.sleep;
import java.net.InetAddress;
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

public class Host extends javax.swing.JPanel {

    public static String ip;
    public Room room;
    public Match match;
    public Server myserver;
    public MainFrame mainFrame;
    public static ChatServer chatServer = new ChatServer();

    private DataOutputStream ServerListOut;
    public DataOutputStream roomOut;
    private Player player;
    private Chrono chrono;
    private JEditorPane jEditorPane;

    public Host(MainFrame mainFrame, String name) {
        this.mainFrame = mainFrame;

        try {
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();
        } catch (IOException ex) {
            System.out.println("Error geting ip: " + ex);
        }

        Random rand = new Random();
        this.player = new Player(name, ip);

        initComponents();
        chrono = new Chrono(chronoLabel);
        System.out.println("INITATION COMPLETE!");
        startServerListClient();
        getServers();
        createRoom();
        joinServer(room.server.getIp());
        connectToRoom();

        try {
            startChatServer();
        } catch (Exception e) {
            System.out.println("Error in creating chatServer :" + e);
        }

        chatPlacerPanel.setLayout(new BorderLayout());
        chatPlacerPanel.add(new ChatPanel(ip,name));

        loadweb();

    }

    public void startChatServer() {
        chatServer.start();
    }

    public void stopChatServer() {
        chatServer.stop();
    }

    public void changeOfURL(String url) {
        String parts[] = url.split("/");
        String item = parts[4];
        currentLabel.setText("CURRENT WORD: " + item);
        if (item.equals(match.Word2)) 
        {
            winLabel.setText("WORD FOUND");
            try {
                sleep(800);
                roomOut.writeUTF("/win " + this.player.name);
            } catch (Exception e) {
            }

        }
    }

    public void connectToRoom() {

        try {

            final String hostAddr = ip;
            final int port = 12001;
            final Scanner scanner = new Scanner(System.in);
            final Socket socket = new Socket(hostAddr, port);

            System.out.println("Room Socket connected to " + socket.getInetAddress() + ":" + socket.getPort());

            final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            roomOut = new DataOutputStream(socket.getOutputStream());

            roomOut.writeUTF("/connect " + player.name + " " + player.ip);

            Thread recvThread = new Thread() { //we read on a different thread to avoid getting blocked by the keyboard reading
                public void run() {

                    while (!this.isInterrupted()) {
                        try {
                            Response res = (Response) in.readObject();

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
            currentLabel.setText("CURRENT WORD: ");
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
            System.out.println("SERVER LIST SOCKET OPEN LISTENING TO REQUESTS");
            Thread recvThread = new Thread() {
                public void run() {
                    while (!this.isInterrupted()) {
                        try {
                            ArrayList<Server> servers = (ArrayList<Server>) in.readObject();
                            for (Server s : servers) {
                                if (s.getIp().equals(ip)) {
                                    System.out.println("YOUR SERVER WAS FOUND");
                                    setMyserver(s);
                                }
                            }
                        } catch (ClassNotFoundException ex) {
                            System.out.println("SSLC class" + ex);
                        } catch (IOException ex) {
                            System.out.println("SSLC IO" + ex);
                        }
                    }
                }
            };
            recvThread.start();
        } catch (IOException ex) {
            System.out.println("erro se astarserverlistclient" + ex);
        }
    }

    public void getServers() {
        try {
            ServerListOut.writeUTF("/get " + ip);
        } catch (Exception e) {
            System.out.println("error getserver" + e);
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
            System.out.println("TRY TO JOIN");
            ServerListOut.writeUTF("/join " + player.name + " " + ip + " " + serverip);
        } catch (Exception e) {
            System.out.println("ERROR JOINING TO SERVER");
        }
    }

    public void exitServer(String serverip) {
        try {
            ServerListOut.writeUTF("/join " + player.name + " " + ip + " " + serverip);
        } catch (Exception e) {
        }
    }

    public void setMyserver(Server server) {
        myserver = server;
    }

    public void createRoom() {
        try {
            sleep(1500);
        } catch (Exception e) {
        }

        System.out.println("myserver" + myserver.getIp() + myserver.getPort());
        room = new Room(myserver);
        room.start();
        System.out.println("Room created. IP: " + room.server.getIp());
    }

    public void repaintWeb() {
        JScrollPane auxPaneScroll = (JScrollPane) gameHolder.getComponent(0);
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

        // add a HTMLEditorKit to the editor pane
        HTMLEditorKit kit = new HTMLEditorKit();
        jEditorPane.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("h1 {color: blue;}");
        Document doc = kit.createDefaultDocument();
        jEditorPane.setDocument(doc);

        //URL url = SwingTester.class.getResource("test.htm");
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
                        roomOut.writeUTF("/setCurrentWord " + word);                        
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

        gameHolder.setLayout(new BorderLayout());
        gameHolder.add(jScrollPane);

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
        gameHolder = new javax.swing.JPanel();
        chatPlacerPanel = new javax.swing.JPanel();
        fromToLabel = new javax.swing.JLabel();
        currentLabel = new javax.swing.JLabel();
        winLabel = new javax.swing.JLabel();
        chronoLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        startMatchButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        playerWordTrackArea = new javax.swing.JTextArea();
        label1 = new java.awt.Label();

        setPreferredSize(new java.awt.Dimension(1920, 1080));

        jLabel1.setText("HOST");

        buttonMenu.setText("MENU");
        buttonMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenuActionPerformed(evt);
            }
        });

        gameHolder.setBackground(new java.awt.Color(153, 153, 153));

        javax.swing.GroupLayout gameHolderLayout = new javax.swing.GroupLayout(gameHolder);
        gameHolder.setLayout(gameHolderLayout);
        gameHolderLayout.setHorizontalGroup(
            gameHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        gameHolderLayout.setVerticalGroup(
            gameHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 313, Short.MAX_VALUE)
        );

        chatPlacerPanel.setBackground(new java.awt.Color(101, 255, 152));

        javax.swing.GroupLayout chatPlacerPanelLayout = new javax.swing.GroupLayout(chatPlacerPanel);
        chatPlacerPanel.setLayout(chatPlacerPanelLayout);
        chatPlacerPanelLayout.setHorizontalGroup(
            chatPlacerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 360, Short.MAX_VALUE)
        );
        chatPlacerPanelLayout.setVerticalGroup(
            chatPlacerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 155, Short.MAX_VALUE)
        );

        fromToLabel.setBackground(new java.awt.Color(153, 255, 153));
        fromToLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        fromToLabel.setText("FROM to");

        currentLabel.setText("jLabel2");

        chronoLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        chronoLabel.setText("00:00:00");

        startMatchButton.setText("START MATCH");
        startMatchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startMatchButtonActionPerformed(evt);
            }
        });

        playerWordTrackArea.setEditable(false);
        playerWordTrackArea.setColumns(20);
        playerWordTrackArea.setRows(5);
        jScrollPane1.setViewportView(playerWordTrackArea);

        label1.setText("PLAYER LOCATIONS");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gameHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonMenu)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(97, 97, 97)
                                .addComponent(jLabel3))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(31, 31, 31)
                                .addComponent(startMatchButton)))
                        .addGap(242, 242, 242)
                        .addComponent(chronoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                        .addGap(381, 381, 381))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chatPlacerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(winLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(currentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fromToLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(buttonMenu)
                    .addComponent(chronoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(startMatchButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(gameHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chatPlacerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fromToLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(currentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(winLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenuActionPerformed
        closeServer();
        room.stop();
        chatServer.stop();
        mainFrame.changeToMenu();
    }//GEN-LAST:event_buttonMenuActionPerformed

    private void startMatchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startMatchButtonActionPerformed
        try {
            roomOut.writeUTF("/createMatch");
        } catch (Exception e) {
            System.out.println("ERROR IN CREATING MATCH IN BUTTON START");
        }

    }//GEN-LAST:event_startMatchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonMenu;
    public static javax.swing.JPanel chatPlacerPanel;
    private javax.swing.JLabel chronoLabel;
    private javax.swing.JLabel currentLabel;
    private javax.swing.JLabel fromToLabel;
    private javax.swing.JPanel gameHolder;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private java.awt.Label label1;
    private javax.swing.JTextArea playerWordTrackArea;
    private javax.swing.JButton startMatchButton;
    private javax.swing.JLabel winLabel;
    // End of variables declaration//GEN-END:variables
}
