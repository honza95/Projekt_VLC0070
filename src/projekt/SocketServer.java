/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projekt;

import java.io.IOException;
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

    private static ADC adc0;

    /**
     * @param args the command line arguments
     */
    static SocketServer ss;

    public static void main(String[] args) {
        // TODO code application logic here                       
        ss = new SocketServer();
        ss.setPort(1000);

        adc0 = new ADC(0);
        adc0.adcInit();
        ss.runServer();

    }

    class Task extends TimerTask {

        // run is a abstract method that defines task performed at scheduled time.
        @Override
        public void run() {
            int value = adc0.getVoltage();
            byte[] bytes = intToByteArray(value);

            ArrayList<Client> clients = (ArrayList<Client>) ss.getClients().clone();
            for (Client c : clients) {
                c.sendData(bytes);
            }
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

}
