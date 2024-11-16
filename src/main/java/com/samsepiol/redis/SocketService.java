package com.samsepiol.redis;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class SocketService implements AutoCloseable, Closeable {
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final Integer port;

    public SocketService(Integer port) throws IOException {
        this.port = port;
        serverSocket = getServersocket();
        clientSocket = getClientSocket();
    }


    public void write(String message) throws IOException {
        getClientOutputStream().write(message.getBytes());
    }

    private static synchronized ServerSocket createServerSocket(Integer port) throws IOException {
        var socket = new ServerSocket(port);
        socket.setReuseAddress(true);
        return socket;
    }

    private ServerSocket getServersocket() throws IOException {
        return Objects.requireNonNullElse(serverSocket, createServerSocket(port));
    }

    private Socket createClientSocket() throws IOException {
        return getServersocket().accept();
    }

    private Socket getClientSocket() throws IOException {
        return Objects.requireNonNullElse(clientSocket, createClientSocket());
    }

    private OutputStream getClientOutputStream() throws IOException {
        return getClientSocket().getOutputStream();
    }

    @Override
    public void close() throws IOException {
        closeClientSocket();
        closeServerSocket();
    }

    private void closeServerSocket() throws IOException {
        if (Objects.nonNull(serverSocket)) {
            serverSocket.close();
        }
    }

    private void closeClientSocket() throws IOException {
        if (Objects.nonNull(clientSocket)) {
            clientSocket.close();
        }
    }
}
