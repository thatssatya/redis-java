package com.samsepiol.application;

import com.samsepiol.server.TcpServer;
import com.samsepiol.server.exception.ServerRuntimeException;
import com.samsepiol.server.impl.redis.RedisServer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class Application {
    private static Application application;
    private static List<TcpServer> tcpServers = List.of();

    private static void startServers() {
        tcpServers = List.of(RedisServer.start());
    }

    private static void closeServers() {
        tcpServers.forEach(tcpServer -> {
            try {
                tcpServer.close();
            } catch (IOException e) {
                log.error("Could not close server: ", e);
                throw new RuntimeException(e);
            }
        });
        log.info("Servers stopped!");
    }

    public static void start() {
        if (notStarted()) {
            application = newApplication();
            application.initialize();
        }
    }

    private static Application newApplication() {
        return new Application();
    }

    private static boolean notStarted() {
        return !started();
    }

    private static boolean started() {
        return Objects.nonNull(application);
    }

    private void initialize() {
        log.info("Starting Application...");
        startServers();
        log.info("Application started!");
        while (isHealthy()) {
            Thread.onSpinWait();
            try {
                Thread.sleep(5000);
                log.debug("Main thead wake up log: {}", Thread.currentThread());
            } catch (InterruptedException e) {
                log.error("Error while sleeping: ", e);
                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                }
                throw ServerRuntimeException.wrap(e);
            }
        }
        closeServers();
        log.info("Application stopped.");
    }

    public boolean isHealthy() {
        return !tcpServers.isEmpty() && tcpServers.stream().allMatch(TcpServer::isRunning);
    }
}