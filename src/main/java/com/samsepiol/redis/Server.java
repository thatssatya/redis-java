package com.samsepiol.redis;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class Server implements AutoCloseable, Closeable {
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final Integer port;

    public Server(Integer port) throws IOException {
        this.port = port;
        serverSocket = getServersocket();
        clientSocket = getClientSocket();
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
