package com.samsepiol.server.exception;

public class ServerRuntimeException extends RuntimeException {

    public ServerRuntimeException(String message) {
        super(message);
    }

    public ServerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ServerRuntimeException wrap(Throwable throwable) {
        return new ServerRuntimeException(throwable.getMessage(), throwable);
    }
}
