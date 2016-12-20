import java.io.*;
import java.net.Socket;

import static java.lang.System.out;

/**
 * The thread to handle upcoming socket connections and transcribe audio into text
 * Author: Jinhua Wang
 * Dec. 20, 2016
 */

public class SquirrelThread extends Thread {
    protected Socket socket;

    public SquirrelThread (Socket clientSocket){
        this.socket = clientSocket;
    }

    public void run(){
        InputStream in = null;
        BufferedReader reader = null;
        DataOutputStream out = null;
        try {
            in = socket.getInputStream();
            SquirrelTranscriber transcriber = new SquirrelTranscriber();
            //transcribe the audio into text
            String text = transcriber.transcribe(in);
            out = new DataOutputStream(socket.getOutputStream());
            //return the response to client
            out.writeBytes(text);
            out.flush();
        } catch (IOException e){
            return;
        }
    }

}
