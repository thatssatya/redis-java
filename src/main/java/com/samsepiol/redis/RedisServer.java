package com.samsepiol.redis;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisServer extends Server {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    public RedisServer() throws IOException {
        super(6379);
        respondToPings();
        signalRunning();
    }

    private void respondToPings() {
        CompletableFuture.runAsync(() -> {
            try {
                while (true) {
                    var input = readLine();
                    if (input.contains("PING")) {
                        pong();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, EXECUTOR_SERVICE);
    }

    public static RedisServer start() throws IOException {
        return new RedisServer();
    }

    public void pong() throws IOException {
        write("+PONG\r\n");
    }
}
