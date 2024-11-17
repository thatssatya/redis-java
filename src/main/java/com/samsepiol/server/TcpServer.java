package com.samsepiol.server;

import java.io.Closeable;

public interface TcpServer extends AutoCloseable, Closeable {

    boolean isRunning();
}
