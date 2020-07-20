package ru.gb.jtwo.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerSocketThread extends Thread {
    private ServerSocketThreadListener listener;
    private final int port;
    private final int timeout;

    public ServerSocketThread(ServerSocketThreadListener listener, String name, int port, int timeout) {
        super(name);
        this.port = port;
        this.timeout = timeout;
        this.listener = listener;
        start();
    }

    @Override
    public void run() {
        listener.onServerSocketThreadStart(this);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            listener.onServerSocketCreate(this, serverSocket);
            serverSocket.setSoTimeout(timeout);
            while (!isInterrupted()) {
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    listener.onServerSocketAcceptTimeout(this, serverSocket);
                    continue;
                }
                listener.onSocketAccept(this, socket);
            }
        } catch (IOException e) {
            listener.onServerSocketThreadException(this, e);
        } finally {
            listener.onServerSocketThreadStop(this);
        }
    }

}
