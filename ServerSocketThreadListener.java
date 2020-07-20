package ru.gb.jtwo.network;

import java.net.ServerSocket;
import java.net.Socket;

public interface ServerSocketThreadListener {
    void onServerSocketThreadStart(ServerSocketThread thread);
    void onServerSocketThreadStop(ServerSocketThread thread);

    void onServerSocketCreate(ServerSocketThread thread, ServerSocket server);
    void onServerSocketAcceptTimeout(ServerSocketThread thread, ServerSocket server);
    void onSocketAccept(ServerSocketThread thread, Socket socket);

    void onServerSocketThreadException(ServerSocketThread thread, Throwable throwable);
}
