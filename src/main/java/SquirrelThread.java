import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.System.out;

/**
 * The thread to handle upcoming socket connections and transcribe audio into text
 * Author: Jinhua Wang
 * Dec. 20, 2016
 */

@SuppressWarnings("Since15")
public class SquirrelThread extends Thread {

    protected Socket socket;

    public SquirrelThread (Socket clientSocket){
        this.socket = clientSocket;
    }

    //run the thread
    public void run(){
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
        if(line!=null) {
            if (line.startsWith("POST")) {
                handle_post_request(in, line);
            } else if (line.startsWith("OPTION")) {
                handle_option_request(in, line);
            } else {
                //todo:tell the browser that the resource does not exist
            }
        } else {
            System.out.print("ERROR: LINE EMPTY");
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
        in.close();
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
        }
        //convert the input to stream
        //InputStream inputStream = new ByteArrayInputStream(String.valueOf(body).getBytes());
        FileOutputStream fos = new FileOutputStream("/Users/jinhuawang/Desktop/test.wav");
        byte[] decoded = Base64.getDecoder().decode(body.toString());
        fos.write(decoded);
        fos.close();
        SquirrelTranscriber transcriber = new SquirrelTranscriber();
        //String text = transcriber.transcribe(inputStream);
        in.close();
        //response(text);
    }

    //send the http response back to the client
    public void response(String text) throws IOException {
        System.out.println(text);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        out.write("HTTP/1.0 200 OK\r\n");
        out.write(getServerTime()+"\r\n");
        out.write("Server: JAVA\r\n");
        out.write("Content-Type: text/html\r\n");
        out.write("Content-Length:"+text.length()+"\r\n");
        out.write("\r\n");
        out.write(text);
        out.close();
        socket.close();
    }

    //get the time of the server for HTTP response
    String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

}