package com.samsepiol.redis;

import java.io.IOException;

public class RedisServer extends Server {

    public RedisServer() throws IOException {
        super(6379);
    }

    public static RedisServer start() throws IOException {
        return new RedisServer();
    }

    public void ping() throws IOException {
        write("+PONG\r\n");
    }
}
