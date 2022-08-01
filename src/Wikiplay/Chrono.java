/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Wikiplay;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.Timer;

/**
 *
 * @author Endika
 */
public class Chrono implements ActionListener {

    private int seconds, minutes, hours;
    private Timer chronometer;
    public JLabel chornoLabel;
    public Chrono(JLabel inputLabel) {
        seconds = 0;
        minutes = 0;
        hours = 0;
        this.chornoLabel = inputLabel;
        chronometer = new Timer(1000, this);
        chornoLabel.setText("00:00:00");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof Timer) {
            seconds++;
            if (seconds == 60) {
                minutes++;
                seconds = 0;
                if (minutes == 60) {
                    hours++;
                    minutes = 0;
                }
            }
            chornoLabel.setText(hours + ":" + minutes + ":" + seconds);
        }
    }

    public void startTimer() {
        chronometer.start();
    }

    public void stopTimer() {
        chronometer.stop();
    }

    public void restartTimer() {
        seconds = 0;
        minutes = 0;
        hours = 0;
        chronometer.stop();
    }

    public int getSeconds() {
        return seconds;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getHours() {
        return hours;
    }
    
}
