/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Wikiplay;


import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.WindowConstants;


public class App {    
    
    static GraphicsDevice device = GraphicsEnvironment
        .getLocalGraphicsEnvironment().getScreenDevices()[0];
    
    public static void main(String[] args) {        
        
        MainFrame mainmenu = new MainFrame();        
        mainmenu.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);        
        mainmenu.pack();
        mainmenu.setVisible(true);
       
    }
    
  
}
