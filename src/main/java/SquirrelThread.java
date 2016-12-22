import java.io.*;
import java.net.Socket;
import java.util.Date;

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

    //run the thread
    public void run(){
        InputStream in = null;
        BufferedReader reader = null;
        DataOutputStream out = null;
        try {
            request();
        } catch (IOException e){
            return;
        }
    }

    //parse the http request from the client
    public void request() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //start to read request
        String line;
        line = in.readLine();
        boolean isPost = line.startsWith("POST");
        if(line.startsWith("POST")){
            handle_post_request(in, line);
        } else if(line.startsWith("OPTION")){
            handle_option_request(in, line);
        } else {
            //tell the browser that the resource does not exist

        }


    }

    //for browsers making cross domain requests, an option request will be first sent
    public void handle_option_request(BufferedReader in, String line) throws IOException {
        //send back allow cross domain request information
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        // send response
        out.write("HTTP/1.1 200 OK\r\n");
        out.write("Content-Type: text/html\r\n");
        out.write("Access-Control-Allow-Origin: *\r\n");
        out.write("Access-Control-Allow-Headers: accept, content-type\r\n");
        out.write("Access-Control-Allow-Methods: POST\r\n");
        out.write(new Date().toString());
        out.write("\r\n");
        out.flush();
        out.close();
        socket.close();
    }

    public void handle_post_request(BufferedReader in, String line) throws IOException {
        StringBuilder raw = new StringBuilder();
        raw.append("" + line);
        int contentLength = 0;

        while (!(line = in.readLine()).equals("")) {
            raw.append('\n' + line);
            final String contentHeader = "Content-Length: ";
            if (line.startsWith(contentHeader)) {
                contentLength = Integer.parseInt(line.substring(contentHeader.length()));
            }
        }
        StringBuilder body = new StringBuilder();
        int c = 0;
        for (int i = 0; i < contentLength; i++) {
            c = in.read();
            body.append((char) c);
            System.out.println(c);
        }
        raw.append(body.toString());
        System.out.print(body.toString());
    }

    //send the http response back to the client
    public void response(String text) throws IOException {

    }

}
