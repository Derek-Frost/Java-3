package ru.gb.jtwo.network;

import java.net.Socket;

public interface SocketThreadListener {
    void onSocketThreadStart(SocketThread thread, Socket socket);
    void onSocketThreadStop(SocketThread thread);

    void onSocketThreadReady(SocketThread thread, Socket socket);
    void onReceiveString(SocketThread thread, Socket socket, String msg);

    void onSocketThreadException(SocketThread thread, Throwable throwable);
}
