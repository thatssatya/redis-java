package com.samsepiol.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class Server implements AutoCloseable, Closeable {
    private static final Logger log = LogManager.getLogger(Server.class);
    private volatile Boolean running = Boolean.FALSE;
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final Integer port;

    private BufferedReader clientInputBufferedReader;

    public Server(Integer port) throws IOException {
        this.port = port;
        serverSocket = getServersocket();
        clientSocket = getClientSocket();
    }

    protected void signalRunning() {
        running = Boolean.TRUE;
        log.info("Server running");
    }

    public boolean isRunning() {
        return running;
    }

    protected void write(String message) throws IOException {
        getClientOutputStream().write(message.getBytes());
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

    private Socket createClientSocket() throws IOException {
        return getServersocket().accept();
    }

    private Socket getClientSocket() throws IOException {
        if (Objects.nonNull(clientSocket)) {
            return clientSocket;
        }
        return createClientSocket();
    }

    private OutputStream getClientOutputStream() throws IOException {
        return getClientSocket().getOutputStream();
    }

    private InputStream getClientInputStream() throws IOException {
        return getClientSocket().getInputStream();
    }

    private BufferedReader getClientBufferedReader() throws IOException {
        if (Objects.isNull(clientInputBufferedReader)) {
            clientInputBufferedReader = new BufferedReader(new InputStreamReader(getClientInputStream()));
        }
        return clientInputBufferedReader;
    }

    protected String readLine() throws IOException {
        return getClientBufferedReader().readLine();
    }

    @Override
    public void close() throws IOException {
        closeClientSocket();
        closeServerSocket();
        closeClientInputBufferedReader();
        running = Boolean.FALSE;
        log.info("Server terminated");
    }

    private void closeClientInputBufferedReader() throws IOException {
        if (Objects.nonNull(clientInputBufferedReader)) {
            clientInputBufferedReader.close();
            log.info("Client input buffer reader closed");
        }
    }

    private void closeServerSocket() throws IOException {
        if (Objects.nonNull(serverSocket)) {
            getServersocket().close();
            log.info("Server socket closed");
        }
    }

    private void closeClientSocket() throws IOException {
        if (Objects.nonNull(clientSocket)) {
            getClientSocket().close();
            log.info("Client socket closed");
        }
    }
}
