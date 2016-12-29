import javax.sound.sampled.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * The thread to handle upcoming socket connections and transcribe audio into text
 * Author: Jinhua Wang
 * Dec. 20, 2016
 */

@SuppressWarnings("Since15")
public class SquirrelThread extends Thread {

    protected Socket socket;

    public static final String HOST_NAME = "localhost";

    public static final int PORT = 8081;

    public SquirrelThread (Socket clientSocket){
        this.socket = clientSocket;
    }

    //run the thread
    public void run(){
        try {
            request();
        } catch (IOException e){
            e.printStackTrace();
            return;
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    //parse the http request from the client
    public void request() throws IOException, UnsupportedAudioFileException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        //start to read request
        String line;
        line = in.readLine();
        if(line!=null) {
            System.out.println(line);
            if (line.startsWith("POST")) {
                handle_post_request(in, line);
            } else if (line.startsWith("OPTION")) {
                handle_option_request(in, line);
            } else {
                //todo:tell the browser that the resource does not exist
                System.out.print("cannot parse"+line);
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
    }

    public void handle_post_request(BufferedReader in, String line) throws IOException, UnsupportedAudioFileException {

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
        System.out.println("starting to convert...");
        String converted_str = convert_service(body.toString());
        InputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(converted_str));

        SquirrelTranscriber transcriber = new SquirrelTranscriber();
        String text = transcriber.transcribe(inputStream);
        response(text);
    }

    //send the http response back to the client
    public void response(String text) throws IOException {
        System.out.println(text);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        out.write("HTTP/1.0 200 OK\r\n");
        out.write(getServerTime()+"\r\n");
        out.write("Server: JAVA\r\n");
        out.write("Content-Type: text/html\r\n");
        out.write("Access-Control-Allow-Origin: *\r\n");
        out.write("Access-Control-Allow-Headers: accept, content-type\r\n");
        out.write("Access-Control-Allow-Methods: POST\r\n");
        out.write("Content-Length:"+text.length()+"\r\n");
        out.write("\r\n");
        out.write(text);
        out.flush();
        out.close();
    }

    //get the time of the server for HTTP response
    String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    //the service to convert the audio file with python script
    //@return base64 string
    public String convert_service(String base64_str) throws IOException {
        String result = "";
        InetAddress address = InetAddress.getByName(HOST_NAME);
        Socket service_socket = new Socket(address, PORT);
        OutputStream os = service_socket.getOutputStream();
        InputStream is = service_socket.getInputStream();
        String str_to_send = "SEND\n"+base64_str.length()+"\n"+"44100\n"+"1\n"+base64_str + "\n";
        os.write(str_to_send.getBytes());
        os.flush();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String cmd = br.readLine();
        if (new String(cmd).equals("RECV")){
            System.out.println("Begin receiving data ...");
            int contentSize = Integer.parseInt(br.readLine());
            StringBuilder sb = new StringBuilder();
            int c = 0;
            for (int i = 0; i < contentSize; i++) {
                c = br.read();
                sb.append((char) c);
            }
            String goodbye = "END\n";
            os.write(goodbye.getBytes());
            os.flush();
            return sb.toString();
        } else {
            System.out.println("Command Not Found: "+cmd);
        }
        os.close();
        return result;
    }

}