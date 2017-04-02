/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projekt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jarek
 */
public class Client {

    Socket clientSocket;
    InputStream in;
    OutputStream out;
    private IClientReceiveData onClientReceiveData;
    private IClientDisconect onClientDisconect;
    private static final int BUFFER_SIZE = 4096;

    private boolean connected = false;

    private RFC1662 rfc;

    public Client(Socket clientSocket) {
        this.clientSocket = clientSocket;
        rfc = new RFC1662();
        rfc.setOnFrameReceived(onFrameReceived);

    }

    RFC1662.IOnFrameReceived onFrameReceived = new RFC1662.IOnFrameReceived() {

        @Override
        public void frameReceived(byte[] frame) {
            if (onClientReceiveData != null) {
                onClientReceiveData.DataReceived(Client.this, frame, frame.length);
            }
        }
    };

    public void setOnClientDisconect(IClientDisconect onClientDisconect) {
        this.onClientDisconect = onClientDisconect;
    }

    public void setOnClientReceiveData(IClientReceiveData onClientReceiveData) {
        this.onClientReceiveData = onClientReceiveData;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void sendData(byte[] buffer) {
        try {
            if (connected) {
                out.write(RFC1662.FLG);
                out.write(rfc.getFrame(buffer));
                out.write(RFC1662.FLG);
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendData(byte[] buffer, int len) {
        try {
            out.write(buffer, 0, len);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void runClient() {
        Thread clientThread;
        clientThread = new Thread() {
            @Override
            public void run() {
                try {
                    out = clientSocket.getOutputStream();
                    in = clientSocket.getInputStream();

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int len = -1;
                    connected = true;
                    while ((len = in.read(buffer)) > -1) {
                        //System.out.print(new String(buffer, 0, len));

                        rfc.putData(buffer, len);
                    }

                } catch (IOException ex) {
                    connected = false;
                    if (onClientDisconect != null) {
                        onClientDisconect.SocketClosed(Client.this);
                    }
                }
            }
        };
        clientThread.start();
    }

    public interface IClientDisconect {

        void SocketClosed(Client sender);
    }

    public interface IClientReceiveData {

        void DataReceived(Client sender, byte[] buffer, int len);
    }
}
