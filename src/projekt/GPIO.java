/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projekt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jarek
 */
public class GPIO {

    private final String GPIO_PATH = "/sys/class/gpio";

    private final int pinNumber;

    public GPIO(int pinNumber) {
        this.pinNumber = pinNumber;
    }

    public void exportPin() {

        File f = new File(GPIO_PATH + "/gpio" + Integer.toString(pinNumber) + "/value");
        if ((f.exists() && !f.isDirectory()) == false) {
            BufferedWriter bw;
            try {
                bw = new BufferedWriter(new FileWriter(GPIO_PATH + "/export"));
                bw.write(Integer.toString(pinNumber));
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(GPIO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void unexportPin() {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(GPIO_PATH + "/unexport"));
            bw.write(Integer.toString(pinNumber));
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(GPIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setDirection(GPIO_DIRECTION direction) {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(GPIO_PATH + "/gpio" + Integer.toString(pinNumber) + "/direction"));
            switch (direction) {
                case IN:
                    bw.write("in");
                    break;
                case OUT:
                    bw.write("out");
                    break;
                default:
                    bw.write("in");
                    break;
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(GPIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setValue(int value) {
        value = value & 0x01;
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(GPIO_PATH + "/gpio" + Integer.toString(pinNumber) + "/value"));
            bw.write(Integer.toString(value));
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(GPIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void waitForValue(int value, int timeSlice) {
        try {
            while (getValue() != value) {
                Thread.sleep(timeSlice);
            }
        } catch (Exception ex) {
            Logger.getLogger(GPIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setEdge(GPIO_EDGE edge) {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(GPIO_PATH + "/gpio" + Integer.toString(pinNumber) + "/edge"));
            switch (edge) {
                case FALLING:
                    bw.write("falling");
                    break;
                case RISING:
                    bw.write("rising");
                    break;
                case BOTH:
                    bw.write("both");
                case NONE:
                default:
                    bw.write("none");
                    break;
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(GPIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getValue() {
        try {
            BufferedReader br;
            br = new BufferedReader(new FileReader(GPIO_PATH + "/gpio" + Integer.toString(pinNumber) + "/value"));
            int num = 0;
            char ch;
            String ret = "";
            while ((num = br.read()) != -1) {
                ch = (char) num;
                ret += ch;
            }
            br.close();
            return Integer.parseInt(ret.replace("\n", ""));
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(GPIO.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }

    }

    public enum GPIO_DIRECTION {

        IN,
        OUT
    }

    public enum GPIO_EDGE {

        FALLING,
        RISING,
        BOTH,
        NONE
    }

}
