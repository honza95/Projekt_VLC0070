/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projekt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jarek
 */
public class PWM {

    public PWM(PWM_PIN pin) {
        this.pin = pin;
    }

    private static final String ENABLE_PWM = "/sys/devices/bone_capemgr.9/slots";
    private static final String ENABLE_PIN_P9_14 = "bone_pwm_P9_14";
    private static final String ENABLE_PIN_P9_21 = "bone_pwm_P9_21";
    private static final String ENABLE_PIN_P9_42 = "bone_pwm_P9_42";
    private static final String ENABLE_PIN_P8_13 = "bone_pwm_P8_13";
    private static final String ENABLE_PIN_PATH = "/sys/devices/bone_capemgr.9/slots";
    private static final int TS = 100;
    private final PWM_PIN pin;
    private String pinDirectory;

    private int duty;
    private int period;
    private int run;
    private int polarity;

    public void enablePWM() {
        pinDirectory = getPinDirectory(pin);
        if (pinDirectory.contains("null")) {
            writeToFile(ENABLE_PWM, "am33xx_pwm");
            try {
                Thread.sleep(TS);
            } catch (InterruptedException ex) {
                Logger.getLogger(PWM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setDuty(int duty) {
        this.duty = duty;
        writeToFile(pinDirectory + "/duty", duty);
    }

    public void setPeriod(int period) {
        this.period = period;
        writeToFile(pinDirectory + "/period", period);
    }

    public void setPolarity(int polarity) {
        this.polarity = polarity;
        writeToFile(pinDirectory + "/polarity", polarity);
    }

    public void setRun(int run) {
        this.run = run;
        writeToFile(pinDirectory + "/run", run);
    }

    public int getDuty() {
        return duty;
    }

    public int getPeriod() {
        return period;
    }

    public int getPolarity() {
        return polarity;
    }

    public int getRun() {
        return run;
    }

    public void enablePin() {
        enablePin(pin);
    }

    private void enablePin(PWM_PIN pin) {
        Process p;
        String args = "";
        switch (pin) {
            case P9_14:
                args = ENABLE_PIN_P9_14;
                break;
            case P9_21:
                args = ENABLE_PIN_P9_21;
                break;
            case P9_42:
                args = ENABLE_PIN_P9_42;
                break;
            case P8_13:
                args = ENABLE_PIN_P8_13;
                break;
            default:
                return;
        }
        pinDirectory = getPinDirectory(pin);
        if (pinDirectory.contains("null")) {
            writeToFile(ENABLE_PIN_PATH, args);
            try {
                Thread.sleep(TS);
            } catch (InterruptedException ex) {
                Logger.getLogger(PWM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        pinDirectory = getPinDirectory(pin);
    }

    private String getPinDirectory(PWM_PIN pin) {
        String ls = "";
        String grep = "";
        switch (pin) {
            case P9_14:
                ls = "ls /sys/devices/ocp.3";
                grep = "pwm_test_P9_14";
                break;
            case P9_21:
                ls = "ls /sys/devices/ocp.3";
                grep = "pwm_test_P9_21";
                break;
            case P9_42:
                ls = "ls /sys/devices/ocp.3";
                grep = "pwm_test_P9_42";
                break;
            case P8_13:
                ls = "ls /sys/devices/ocp.3";
                grep = "pwm_test_P8_13";
                break;
        }

        Process p;
        String s = "";
        try {
            p = Runtime.getRuntime().exec(ls);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null) {
                if (s.contains(grep)) {
                    break;
                }
            }
            p.waitFor();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(PWM.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "/sys/devices/ocp.3/" + s;
    }

    private void writeToFile(String file, int value) {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(Integer.toString(value));
            bw.close();
        } catch (Exception ex) {
            Logger.getLogger(PWM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeToFile(String file, String value) {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(value);
            bw.close();
        } catch (Exception ex) {
            Logger.getLogger(PWM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public enum PWM_PIN {
        P9_14,
        P9_21,
        P9_42,
        P8_13
    }
}
