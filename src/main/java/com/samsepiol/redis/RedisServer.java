package com.samsepiol.redis;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

@Slf4j
public class RedisServer extends Server {
    private static final Integer PORT = 6379;
    public RedisServer() throws IOException {
        super(PORT, 3);
        signalRunning();
        log.info("Redis server listening on port: {}", PORT);
    }

    @Override
    protected void handleClientMessage(String input, Socket clientSocket) throws IOException {
        if ("PING".equals(input)) {
            pong(clientSocket);
        }
    }

    public static RedisServer start() throws IOException {
        return new RedisServer();
    }

    public void pong(Socket clientSocket) throws IOException {
        write("+PONG\r\n", clientSocket);
    }
}
