package Wikiplay;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author am14010
 */
public class ListFrameTest extends javax.swing.JFrame {
    GenericListModel<Server> serversModel;

    public ListFrameTest() {
        serversModel = new GenericListModel<>();
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField3 = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        serverList = new javax.swing.JList();
        createButton = new javax.swing.JButton();
        nStudentsSpinner = new javax.swing.JSpinner();
        sortButon = new javax.swing.JButton();
        sortMethodCombo = new javax.swing.JComboBox();
        nameField = new javax.swing.JTextField();
        ipField = new javax.swing.JTextField();

        jTextField3.setText("jTextField3");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        serverList.setModel(serversModel);
        serverList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                serverListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(serverList);

        createButton.setText("Create");
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        nStudentsSpinner.setModel(new javax.swing.SpinnerNumberModel(10, 1, 20, 1));

        sortButon.setText("Sort");
        sortButon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortButonActionPerformed(evt);
            }
        });

        sortMethodCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "score descending", "random shuffle" }));
        sortMethodCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortMethodComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(createButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nStudentsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(40, 40, 40)
                                .addComponent(sortButon)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(sortMethodCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(139, 139, 139)
                                .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(16, 16, 16)
                    .addComponent(ipField, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(362, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createButton)
                    .addComponent(nStudentsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sortButon)
                    .addComponent(sortMethodCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(60, 60, 60)
                .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(234, Short.MAX_VALUE)
                    .addComponent(ipField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(20, 20, 20)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        String ip = "192.168.1.19";        
        try {
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();        
        
            final String hostAddr = ip;
            final int port = 12002;
            final Scanner scanner = new Scanner(System.in);
            final Socket socket = new Socket(hostAddr, port);

            System.out.println("Socket connected to " + socket.getInetAddress() + ":" + socket.getPort());

            final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            System.out.println("LIST FRAME");
            System.out.println(ip);
            
            out.writeUTF("/set endikaroom1 el_mejor1 192.168.1.12 12000 10");            
            out.writeUTF("/set endikaroom2 el_mejor2 192.168.1.13 12000 10");
            out.writeUTF("/set endikaroom3 el_mejor3 192.168.1.14 12000 10");
            out.writeUTF("/set endikaroom4 el_mejor4 192.168.1.16 12000 10");
            out.writeUTF("/set endikaroom5 el_mejor5 192.168.1.17 12000 10");
            out.writeUTF("/set endikaroom6 el_mejor6 192.168.1.19 12000 10");
            out.writeUTF("/get "+ip);
            Thread recvThread = new Thread() { //we read on a different thread to avoid getting blocked by the keyboard reading
                public void run() {

                    while (!this.isInterrupted()) {
                        try {
                            ArrayList<Server> servers = (ArrayList<Server>) in.readObject();
                            servers.forEach( s -> serversModel.add(s) );                            
                            System.out.println("Servers listframestest: ");
                            System.out.println(servers.size());
                            System.out.println(serversModel.getSize());
                            System.out.println("!");
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
    }//GEN-LAST:event_createButtonActionPerformed

    private void sortButonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortButonActionPerformed
        final int option = sortMethodCombo.getSelectedIndex();
        if (option == 0){ //descending score
            serversModel.getElements().sort( Comparator.comparing( Server:: getNumPlayers).reversed() );
        }else if(option == 1){ //shuffle
            Collections.shuffle( serversModel.getElements() );
        }
        serversModel.updateAll();
    }//GEN-LAST:event_sortButonActionPerformed

    private void serverListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_serverListValueChanged
       if ( evt.getValueIsAdjusting() )
           return;
       
       final int index = serverList.getSelectedIndex();
       final Server s = serversModel.getAt(index);
       
       serverToGUI(s);
    }//GEN-LAST:event_serverListValueChanged

    private void sortMethodComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortMethodComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sortMethodComboActionPerformed

       
    protected void serverToGUI(final Server s) {
        nameField.setText( s.getName() );
        ipField.setText( s.getIp() + "");        
    }

    public static void main(String args[]) {
        ListFrameTest mf = new ListFrameTest();
        mf.setLocationRelativeTo(null);
        mf.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createButton;
    private javax.swing.JTextField ipField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JSpinner nStudentsSpinner;
    private javax.swing.JTextField nameField;
    private javax.swing.JList serverList;
    private javax.swing.JButton sortButon;
    private javax.swing.JComboBox sortMethodCombo;
    // End of variables declaration//GEN-END:variables

    
}