package com.samsepiol.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class Application {
    private static List<Server> servers = List.of();

    private static void startServers() {
        try {
            servers = List.of(RedisServer.start());
        } catch (IOException e) {
            log.error("Could not start servers: ", e);
        }
    }

    private static void closeServers() {
        servers.forEach(server -> {
            try {
                server.close();
            } catch (IOException e) {
                log.error("Could not close server: ", e);
                throw new RuntimeException(e);
            }
        });
        log.info("Servers stopped!");
    }

    public static void start() {
        new Application().initApplication();
    }

    public void initApplication() {
        log.info("Starting Application...");
        startServers();
        log.info("Application started!");
        while (true) {
            if (isHealthy()) {
                continue;
            }
            closeServers();
            log.info("Application stopped.");
        }
    }

    public boolean isHealthy() {
        return !servers.isEmpty() && servers.stream().allMatch(Server::isRunning);
    }
}
