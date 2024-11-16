import com.samsepiol.redis.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) {

//        try (var redisServer = RedisServer.start()) {
//            redisServer.ping();
//        } catch (IOException e) {
//            System.out.println("IOException: " + e.getMessage());
//        } finally {
//            System.out.println("Connection closed");
//        }

        int port = 6379;
        try (var serverSocket = new ServerSocket(port)) {
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);
            // Wait for connection from client.
            try (var clientSocket = serverSocket.accept()) {
                clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
