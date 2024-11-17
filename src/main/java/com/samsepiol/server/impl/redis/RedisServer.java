package com.samsepiol.server.impl.redis;

import com.samsepiol.server.impl.AbstractTcpServer;
import com.samsepiol.server.impl.redis.thread.factory.RedisServerThreadFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisServer extends AbstractTcpServer {
    private static final String SERVER_NAME = "Redis";
    private static final Integer PORT = 6379;
    private static final String PING = "PING";
    private static final String PONG = "+PONG\r\n";
    private static final RedisServer INSTANCE = createInstance();

    private RedisServer() {
        super(PORT, 3, SERVER_NAME, RedisServerThreadFactory.getInstance());
    }

    @Override
    protected String outputMessage(String input) {
        if (PING.equals(input)) {
            return pong();
        }
        return null;
    }

    private static RedisServer createInstance() {
        return new RedisServer();
    }

    public static RedisServer start() {
        return INSTANCE;
    }

    private static String pong() {
        return PONG;
    }
}
