/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Wikiplay;

import java.io.Serializable;

/**
 *
 * @author Endika
 */
public class Response  implements Serializable{
    public Match match;
    public boolean isMatch;
    public String msg;
    public String type;

    public Response(Match match, boolean isMatch, String msg , String type) {
        this.match = match;
        this.isMatch = isMatch;
        this.msg = msg;
        this.type = type;
    }
    
}
