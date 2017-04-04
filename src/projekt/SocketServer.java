/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projekt;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jarek
 */
public class SocketServer {
 
    private int port;
    private final ArrayList<Client> clients;
    private ServerSocket serverSocket;

    public SocketServer() {
        clients = new ArrayList<>();
    }

    public int getPort() {
        return port;
    }

    public synchronized ArrayList<Client> getClients() {
        return clients;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void runServer() {        
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new Task(), 0, 10);

        Thread serverThread = new Thread() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(port);
                    System.out.println("Server started at port " + Integer.toString(port));
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Client connected: " + clientSocket.getInetAddress().toString());
                        Client client = new Client(clientSocket);
                        client.setOnClientDisconect(new Client.IClientDisconect() {

                            @Override
                            public void SocketClosed(Client sender) {
                                getClients().remove(sender);
                                System.out.println("Client disconected: " + sender.getClientSocket().getInetAddress().toString());
                            }
                        });
                        client.setOnClientReceiveData(dataReceived);
                        client.runClient();
                        getClients().add(client);
                    }
                } catch (SocketException ex) {
                    Logger.getLogger(SocketServer.class.getName()).log(Level.SEVERE, null, ex);
                    //System.out.println("Socket closed.");
                } catch (IOException ex) {
                    Logger.getLogger(SocketServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        serverThread.start();
    }

    private final Client.IClientReceiveData dataReceived = new Client.IClientReceiveData() {

        @Override
        public void DataReceived(Client sender, byte[] buffer, int len) {

        }
    };

    public void stopServer() {
        try {
            serverSocket.close();

        } catch (IOException ex) {
            Logger.getLogger(SocketServer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    

    /**
     * @param args the command line arguments
     */
    static SocketServer ss;
    private static Projekt rm;

    public static void main(String[] args) {
        // TODO code application logic here                       
        ss = new SocketServer();
        ss.setPort(1000);

        rm = new Projekt();
        rm.inicializace()
        
        ss.runServer();

    }

    class Task extends TimerTask {

        // run is a abstract method that defines task performed at scheduled time.
        @Override
        public void run() {
            
            byte[] bytes = null;
            
            try {
            	bytes = rm.data();
            } catch (IOException ex) {
            	Logger.getLogger(SocketServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            ArrayList<Client> clients = (ArrayList<Client>) ss.getClients().clone();
            for (Client c : clients) {
                c.sendData(bytes);
            }
        }
    }

    class Task extends TimerTask {
    	// run is a abstract method that defines task performed at scheduled time.
        @Override
        public void run() {
            rm.rizeni();
        }
    }
    
    public static int byteArrayToInt(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    public static byte[] intToByteArray(int i) {
        final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }

    public static class Projekt{
    	public Projekt (){}
    	
    	/Konstanty
    	private GPIO onOffButton;
    	private GPIO startStopButton;
    	private GPIO ledOnOff;
    	
    	private LED zapinaniLed;
    	
    	private ADC potenciometr;
    	
    	private PWM pwmPot;
    	
    	private static final boolean stavOnOff = false;
    	private static final boolean stavBeh = false;
    	private static final boolean valueOnOff;
    	private static final boolean valueBeh;
    	
    	private static final int pwmPeriod = 10000000;
    	private int pwmPart = pwmPeriod/10;
    	private static final int pwmDutyDefault = 1500000;
    	private long pwmDuty;
    	
    	private void inicializace(){
    		onOffButton = new GPIO(51);
    		onOffButton.exportPin();
    		onOffButton.setDirection(GPIO.setDirection(GPIO.GPIO_DIRECTION.IN);
    		
    		startStopButton = new GPIO (22);
    		startStopButton.exportPin();
    		startStopButton.setDirection(GPIO.setDirection(GPIO.GPIO_DIRECTION.IN);
    		
    		ledOnOff = new GPIO(66);           
            ledOnOff.exportPin();
            ledOnOff.setDirection(GPIO.GPIO_DIRECTION.OUT);
            
            zapinaniLed = new LED(LED.LED0_PATH);
            zapinaniLed.setDelayOff(1000);
            zapinaniLed.setDelayOn(1000);
            zapinaniLed.setLedMode(LED.LED_MODE.LED_OFF);
            
            potenciometr = new ADC(0); 
            potenciometr.adcInit();
            
            pwmPot = new PWM(PWM.PWM_PIN.P9_14);
            pwmPot.enablePWM();
            pwmPot.enablePin();
            pwmPot.setRun(0);
            pwmPot.setPeriod(pwmPeriod)
            pwmPot.setPolarity(0);
    	}
    	    	
    	private void rizeni (){
    		while (stavOnOff == false){
    			onOffButton.waitForValue(1, 50); // Cekani na nabeznou hranu Zapnutí vypnutí
                onOffButton.waitForValue(0, 50); // cekani na pusteni tlacitka
                
    			zapinani();
    			while (stavOnOff == true){            
                    valueOnOff = toBolean(onOffButton.getValue()); 
                
                    if (valueOnOff == true){           // kontrola jestli uživatel nevypnul pristroj   
                        onOffButton.waitForValue(0, 50); 
                        stavOnOff = false;            //když je tlačitko zmacknute dojde ke zmene stavu
                        
                        vypinani();                        
                    }            
                    else {
                        valueBeh = toBoolean(startStopButton.getValue());
                    
                        if (stavBeh == true){
                            if (valueBeh == true){
                                startStopButton.waitForValue(0, 50);   //cekani na jeho pusteni aby se neprovadelo
                                stavBeh = false;                       //stop
                                pwmPot.setRun(0);
                                System.out.println("Stop");
                             }
                             else{                                      //beh pwm
                                 if (valuePot < 199){
                                    pwmDuty = pwmPart
                                 } else if (valuePot < 399){
                                    pwmDuty = pwmPart*2;
                                 } else if (valuePot < 599){
                                	 pwmDuty = pwmPart*3;
                                 } else if (valuePot < 799){
                                	 pwmDuty = pwmPart*4;
                                 } else if (valuePot < 999){
                                	 pwmDuty = pwmPart*5;
                                 } else if (valuePot < 1199){
                                	 pwmDuty = pwmPart*6;
                                 } else if (valuePot < 1399){
                                	 pwmDuty = pwmPart*7;
                                 } else if (valuePot < 1599){
                                	 pwmDuty = pwmPart*8;
                                 } else {
                                	 pwmDuty = pwmPart*9;
                                 } 
                                 pwmPot.setDuty((int)pwmDuty);
                             }
                         }
                         else{           //stavBeh == false
                             if (valueBeh == true){
                                 startStopButton.waitForValue(0, 50);  //cekani na jeho pusteni aby se neprovadelo
                                 stavBeh = true;                        //start
                                 pwmPot.setDuty(pwmDutyDefault);
                                 pwmPot.setRun(1);
                                 System.out.println("Start");   
                             }
                                            
                         }
                    
                    }
    			}
    		}
    	}
    	
    	private void zapinani (){
                      
            System.out.println("Zapínaní");                               
            zapinaniLed.setLedMode(LED.LED_MODE.LED_BLINK);
            
            try {
                Thread.sleep(6000);                 //5000 ms 
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            
            zapinaniLed.setLedMode(LED.LED_MODE.LED_OFF);
        
            System.out.println("Zapnuto");
            stavOnOff = true;
            ledOnOff.setValue(1);  //sviti led 1    		
    	}
    	
    	private void vypinani (){
    		System.out.println("Vypinani");
            zapinaniLed.setLedMode(LED.LED_MODE.LED_BLINK);
            
            try {
                Thread.sleep(6000);                 //5000 ms 
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
    
            zapinaniLed.setLedMode(LED.LED_MODE.LED_OFF);
            System.out.println("Vypnuto");
    	}
    	
    	private void data (){
    		ByteArrayOutputStream bytestr = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(bytestr);
            
            dataOut.writeBoolean(stavOnOff);
            dataOut.writeBoolean(stavBeh);
            
            dataOut.writeInt((int)pwmDuty)
            
            return bytestr.toByteArray();
    	}
    	
    	private boolean toBoolean(int number)
        {
            boolean bool = false;
            if (number > 0)
                bool = true;
            return bool;
        }
    	
    }
}
