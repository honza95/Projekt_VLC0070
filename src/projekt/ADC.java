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
public class ADC {

    private static final String ADC_PATH = "/sys/devices/ocp.3/helper.15";    
    private static final String AIN = "AIN";
    private final int channel;

    public ADC(int channel) {
        this.channel = channel;
    }

    public void adcInit() {
        File f = new File(ADC_PATH + "/" + AIN + "0");
        if((f.exists() && !f.isDirectory()) == false) {
            writeToFile("/sys/devices/bone_capemgr.9/slots", "cape-bone-iio");
        }
        
    }
    
    private void writeToFile(String file, String value) {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(value);
            bw.close();
        } catch (Exception ex) {
            Logger.getLogger(ADC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public int getVoltage() {
        try {
            BufferedReader br;
            br = new BufferedReader(new FileReader(ADC_PATH + "/" + AIN + Integer.toString(channel)));
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
            Logger.getLogger(ADC.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }

    }

}
