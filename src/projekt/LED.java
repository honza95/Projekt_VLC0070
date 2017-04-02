/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projekt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jarek
 */
public class LED {

    public static final String LED0_PATH = "/sys/class/leds/beaglebone:green:usr0";
    public static final String LED1_PATH = "/sys/class/leds/beaglebone:green:usr1";
    public static final String LED2_PATH = "/sys/class/leds/beaglebone:green:usr2";
    public static final String LED3_PATH = "/sys/class/leds/beaglebone:green:usr3";

    private final String ledPath;
    private int delayOn;
    private int delayOff;

    public LED(String ledPath){
        this.ledPath = ledPath;
    }
    
    public void setDelayOff(int delayOff) {
        this.delayOff = delayOff;
    }

    public void setDelayOn(int delayOn) {
        this.delayOn = delayOn;
    }

    public void setLedMode(LED_MODE mode) {
        try {
            BufferedWriter bw;
            switch (mode) {
                case LED_OFF:
                    bw = new BufferedWriter(new FileWriter(ledPath + "/trigger"));
                    bw.write("none");
                    bw.close();

                    bw = new BufferedWriter(new FileWriter(ledPath + "/brightness"));
                    bw.write("0");
                    bw.close();
                    break;
                case LED_ON:
                    bw = new BufferedWriter(new FileWriter(ledPath + "/trigger"));
                    bw.write("none");
                    bw.close();

                    bw = new BufferedWriter(new FileWriter(ledPath + "/brightness"));
                    bw.write("1");
                    bw.close();
                    break;
                case LED_BLINK:
                    bw = new BufferedWriter(new FileWriter(ledPath + "/trigger"));
                    bw.write("timer");
                    bw.close();

                    bw = new BufferedWriter(new FileWriter(ledPath + "/delay_on"));
                    bw.write(Integer.toString(delayOn));
                    bw.close();

                    bw = new BufferedWriter(new FileWriter(ledPath + "/delay_off"));
                    bw.write(Integer.toString(delayOff));
                    bw.close();
                    break;
            }
        } catch (IOException ex) {
           Logger.getLogger(LED.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public enum LED_MODE {
        LED_OFF,
        LED_ON,
        LED_BLINK,
    }
}
