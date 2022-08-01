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
public class Match implements Serializable{
    public String Word1;
    public String Word2;  

    public Match(String Word1, String Word2) {
        this.Word1 = Word1;
        this.Word2 = Word2;            
    }

    
}
