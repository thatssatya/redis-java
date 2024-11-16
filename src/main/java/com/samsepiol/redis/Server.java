package com.samsepiol.redis;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class Server implements AutoCloseable, Closeable {
    private volatile Boolean running = Boolean.FALSE;
    private final ServerSocket serverSocket;
    private final Integer port;
    private final Map<ServerSocket, List<ExecutorService>> clientExecutorServiceMap;

    public Server(Integer port) throws IOException {
        this(port, 1);
    }

    public Server(Integer port, Integer maxClients) throws IOException {
        this.port = port;
        serverSocket = getServersocket();
        this.clientExecutorServiceMap = Map.of(serverSocket,
                IntStream.range(0, maxClients).mapToObj(i -> connectionManager(serverSocket)).toList());
    }

    private ExecutorService connectionManager(ServerSocket socket) {
        var executorService = Executors.newSingleThreadExecutor();
        handleMessageFromSocketAsync(socket, executorService);
        return executorService;
    }

    private void handleMessageFromSocketAsync(ServerSocket socket, ExecutorService executorService) {
        CompletableFuture.runAsync(() -> {
            try (var clientSocket = socket.accept()) {
                try (var reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    while (true) {
                        var input = reader.readLine();
                        log.info("Received message: {}", input);
                        handleClientMessage(input, clientSocket);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    protected void handleClientMessage(String input, Socket clientSocket) throws IOException {
        log.info("Default message handler: {}", input);
    }

    protected void signalRunning() {
        running = Boolean.TRUE;
    }

    public boolean isRunning() {
        return running;
    }

    protected void write(String message, Socket clientSocket) throws IOException {
        clientSocket.getOutputStream().write(message.getBytes());
    }

    private static synchronized ServerSocket createServerSocket(Integer port) throws IOException {
        var socket = new ServerSocket(port);
        socket.setReuseAddress(true);
        return socket;
    }

    private ServerSocket getServersocket() throws IOException {
        if (Objects.nonNull(serverSocket)) {
            return serverSocket;
        }
        return createServerSocket(port);
    }

    @Override
    public void close() throws IOException {
        closeServerSocket();
        clientExecutorServiceMap.values().forEach(executorServices -> executorServices.forEach(ExecutorService::shutdown));
        running = Boolean.FALSE;
        log.info("Server terminated");
    }

    private void closeServerSocket() throws IOException {
        if (Objects.nonNull(serverSocket)) {
            getServersocket().close();
            log.info("Server socket closed");
        }
    }

}
