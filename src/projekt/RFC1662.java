/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projekt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jarek
 */
public class RFC1662 {

    public RFC1662() {

    }

    public static final byte FLG = 0x7E;
    private static final byte ESC = 0x7D;
    private static final byte[] ESC_FLG = {0x7D, 0x5E};
    private static final byte[] ESC_ESC = {0x7D, 0x5D};

    private IOnFrameReceived onFrameReceived;
    private IOnFrameError onFrameError;
    
    public void setOnFrameReceived(IOnFrameReceived onFrameReceived) {
        this.onFrameReceived = onFrameReceived;
    }

    public void setOnFrameError(IOnFrameError onFrameError) {
        this.onFrameError = onFrameError;
    }
    
    ByteArrayOutputStream writerMachine = new ByteArrayOutputStream();

    public void putData(byte[] data, int len) {
        for (int i = 0; i < len; i++) {
            byte b = data[i];
            if (b == FLG) {
                byte[] buffer = writerMachine.toByteArray();
                if (buffer.length > 0) {
                    boolean bufferOK = false;
                    try {
                        buffer = getBytes(buffer);
                        bufferOK = true;
                    } catch (Exception ex) {
                        if (onFrameError != null)
                            onFrameError.frameError(buffer, ex);
                    }
                    if (onFrameReceived != null && bufferOK) {
                        onFrameReceived.frameReceived(buffer);
                    }
                }
                writerMachine = new ByteArrayOutputStream();
            } else {
                writerMachine.write(b);
            }
        }

    }

    // Decode message from RFC1662 standard
    public byte[] getBytes(byte[] frame) throws Exception {
        ByteArrayOutputStream writer = new ByteArrayOutputStream();

        int length = frame.length;
        for (int i = 0; i < length; i++) {
            byte b = frame[i];
            switch (b) {
                case ESC:
                    i++;
                    if (frame[i] == ESC_FLG[1]) {
                        writer.write(FLG);
                    } else if (frame[i] == ESC_ESC[1]) {
                        writer.write(ESC);
                    } else {
                        throw new Exception("RFC1662 Corupted");
                    }
                    break;
                default:
                    writer.write(b);
                    break;
            }
        }
        return writer.toByteArray();
    }

    // Code buffer to RFC1662
    public byte[] getFrame(byte[] buffer) {

        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        try {
            int length = buffer.length;
            for (int i = 0; i < length; i++) {
                byte b = buffer[i];
                switch (b) {
                    case FLG:
                        writer.write(ESC_FLG);
                        break;
                    case ESC:
                        writer.write(ESC_ESC);
                        break;
                    default:
                        writer.write(b);
                        break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(RFC1662.class.getName()).log(Level.SEVERE, null, ex);
        }
        return writer.toByteArray();
    }

    public interface IOnFrameReceived {

        void frameReceived(byte[] frame);
    }

    public interface IOnFrameError {

        void frameError(byte[] buffer, Exception ex);
    }
}
