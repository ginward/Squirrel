import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * The server for squirrel notepad
 * Author: Jinhua Wang
 */
public class SquirrelServer {

    static final int PORT = 8080;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e){
            e.printStackTrace();
        }
        while (true){
            try {
                socket = serverSocket.accept();
                System.out.println(socket);
                //start processing the socket
                new SquirrelThread(socket).start();
            } catch (IOException e){
                System.out.println("I/O error: "+e);
            }

        }
    }
}