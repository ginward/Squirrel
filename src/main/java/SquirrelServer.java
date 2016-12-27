import javax.net.ssl.SSLServerSocketFactory;
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
        System.setProperty("javax.net.ssl.keyStore","/etc/letsencrypt/live/www.duedue.xyz/keystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword","squirrel");
        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        try {
            serverSocket = ssf.createServerSocket(PORT);
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