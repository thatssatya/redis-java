import com.samsepiol.redis.RedisServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        try (var redisServer = RedisServer.start()) {
            while (redisServer.isRunning());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            System.out.println("Connection closed");
        }
    }
}
