package com.samsepiol.server.impl;

import com.samsepiol.server.TcpServer;
import com.samsepiol.server.exception.ServerRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.IntStream;

@Slf4j
public abstract class AbstractTcpServer implements TcpServer {
    private final String serverName;
    private volatile Boolean running = Boolean.FALSE;
    private final ServerSocket serverSocket;
    private final Integer port;
    private final Integer maxClients;
    private final List<ExecutorService> clientConnectionManagers;

    protected AbstractTcpServer(Integer port) {
        this(port, 1, "TcpServer");
    }

    protected AbstractTcpServer(Integer port, String serverName) {
        this(port, 1, serverName);
    }

    protected AbstractTcpServer(Integer port, Integer maxClients, String serverName) {
        this(port, maxClients, serverName, null);
    }

    protected AbstractTcpServer(Integer port, Integer maxClients, String serverName, ThreadFactory threadFactory) {
        this.port = port;
        this.maxClients = maxClients;
        this.serverName = serverName;
        serverSocket = getServerSocket();
        this.clientConnectionManagers = clientConnectionManagers(serverSocket, threadFactory);
        signalRunning();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void close() throws IOException {
        closeServerSocket();
        clientConnectionManagers.forEach(ExecutorService::shutdown);
        running = Boolean.FALSE;
        log.info("Server terminated");
    }

    private static InputStream clientInputStream(Socket clientSocket) throws IOException {
        return clientSocket.getInputStream();
    }

    private static String readInputFromClient(BufferedReader reader) throws IOException {
        return reader.readLine();
    }

    private List<ExecutorService> clientConnectionManagers(ServerSocket socket,
                                                           ThreadFactory threadFactory) {
        return IntStream.range(0, maxClients)
                .mapToObj(i -> {
                    var executorService = Objects.nonNull(threadFactory)
                            ? Executors.newThreadPerTaskExecutor(threadFactory)
                            : Executors.newVirtualThreadPerTaskExecutor();
                    CompletableFuture.runAsync(() -> handleClientConnectionThread(socket), executorService);
                    log.info("{} connection manager {} started!", serverName, i + 1);
                    return executorService;
                })
                .toList();
    }

    private void handleClientConnectionThread(ServerSocket socket) {
        log.debug("Waiting for client connection on thread: {}", Thread.currentThread());

        try (var clientSocket = socket.accept()) {
            log.debug("Client connected: {}", clientSocket);

            try (var inputBufferedReader = new BufferedReader(new InputStreamReader(clientInputStream(clientSocket)))) {

                try (var outputStream = new DataOutputStream(clientSocket.getOutputStream())) {

                    handleClientMessageOnLoop(clientSocket, inputBufferedReader, outputStream);
                }
            }
            handleClientConnectionThread(socket);

        } catch (IOException e) {
            throw ServerRuntimeException.wrap(e);
        }
    }

    private void handleClientMessageOnLoop(Socket clientSocket,
                                           BufferedReader inputBufferedReader,
                                           DataOutputStream outputStream) throws IOException {

        while (true) {
            Thread.onSpinWait();

            log.debug("Waiting for message from client: {}", clientSocket);
            var input = readInputFromClient(inputBufferedReader);

            if (clientSocket.isClosed() || clientSocket.isInputShutdown() || Objects.isNull(input)) {
                log.info("Client disconnected");
                break;
            }

            log.info("Received message from client: {}: {}", clientSocket, input);

            writeOutputToClient(input, outputStream);
        }
    }

    private void writeOutputToClient(String input, DataOutputStream outputStream) throws IOException {
        var outputMessage = outputMessage(input);

        if (Objects.isNull(outputMessage)) {
            log.debug("No message to send to client");
            return;
        }

        outputStream.writeBytes(outputMessage);
        log.debug("Sent message to client: {}", outputMessage);
    }

    protected abstract String outputMessage(String input);

    private void signalRunning() {
        running = Boolean.TRUE;
        log.info("{} Server listening on port: {}", serverName, port);
    }

    private void closeServerSocket() throws IOException {
        if (serverSocketAvailable()) {
            getServerSocket().close();
            log.info("Server socket closed");
        }
    }

    private synchronized ServerSocket createServerSocket() {
        if (serverSocketAvailable()) {
            return serverSocket;
        }
        try {
            var socket = new ServerSocket(this.port);
            socket.setReuseAddress(true);
            return socket;
        } catch (IOException e) {
            throw ServerRuntimeException.wrap(e);
        }
    }

    private ServerSocket getServerSocket() {
        return serverSocketAvailable() ? serverSocket : createServerSocket();
    }

    private boolean serverSocketAvailable() {
        return Objects.nonNull(serverSocket) && serverSocketOpen();
    }

    private boolean serverSocketOpen() {
        return !serverSocket.isClosed();
    }

}
