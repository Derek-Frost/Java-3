package ru.gb.jtwo.chat.server.core;

import ru.gb.jtwo.chat.library.Library;
import ru.gb.jtwo.network.ServerSocketThread;
import ru.gb.jtwo.network.ServerSocketThreadListener;
import ru.gb.jtwo.network.SocketThread;
import ru.gb.jtwo.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;



public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {

    private ServerSocketThread server;
    private ChatServerListener listener;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private Vector<SocketThread> clients = new Vector<>();

    public ChatServer(ChatServerListener listener) {
        this.listener = listener;
    }

    public void start(int port) {
        if (server != null && server.isAlive()) {
            writeLog("Server is already running");
        } else {
            server = new ServerSocketThread(this, "Server", port, 2000);
            writeLog("Server thread started at port: " + port);
        }
    }

    public void stop() {
        if (server == null || !server.isAlive()) {
            writeLog("Server is not running!");
        } else {
            server.interrupt();
            writeLog("Server interrupted");
        }
    }

    private void writeLog(String msg) {
        msg = dateFormat.format(System.currentTimeMillis()) +
                Thread.currentThread().getName() +
                ": " + msg;
        listener.onServerMessage(msg);
    }

    /**
     * ServerSocketThread events
     * */

    @Override
    public void onServerSocketThreadStart(ServerSocketThread thread) {
        SqlClient.connect();
        writeLog("SST Start");
    }

    @Override
    public void onServerSocketThreadStop(ServerSocketThread thread) {
        SqlClient.disconnect();
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).close();
        }
        writeLog("SST Stop");
    }

    @Override
    public void onServerSocketCreate(ServerSocketThread thread, ServerSocket server) {
        writeLog("Server Socket created");
    }

    @Override
    public void onServerSocketAcceptTimeout(ServerSocketThread thread, ServerSocket server) {
//        writeLog("Server socket accept timed out");
    }

    @Override
    public void onSocketAccept(ServerSocketThread thread, Socket socket) {
        String threadName = "Socket thread " + socket.getInetAddress() + ":" + socket.getPort();
        new ClientThread(this, threadName, socket);
    }

    @Override
    public void onServerSocketThreadException(ServerSocketThread thread, Throwable throwable) {
        writeLog("Server socket thread exception: " + throwable.getMessage());
    }

    /**
     * SocketThread events
     * */

    @Override
    public synchronized void onSocketThreadStart(SocketThread thread, Socket socket) {
        writeLog("SocketThread started");
    }

    @Override
    public synchronized void onSocketThreadStop(SocketThread thread) {
        ClientThread client = (ClientThread) thread;
        clients.remove(thread);
        if (client.isAuthorized() && !client.isReconnect()) {
            sendToAllAuthenticatedClients(Library.getTypeBroadcast("Server", client.getNickname() + " disconnected"));
            sendToAllAuthenticatedClients(Library.getUserList(getUsers()));
        }
    }

    @Override
    public synchronized void onSocketThreadReady(SocketThread thread, Socket socket) {
        clients.add(thread);
    }

    @Override
    public synchronized void onReceiveString(SocketThread thread, Socket socket, String msg) {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized()) {
            handleAuthMessage(client, msg);
        } else {
            handleNonAuthMessage(client, msg);
        }
    }

    @Override
    public synchronized void onSocketThreadException(SocketThread thread, Throwable throwable) {
        writeLog("SocketThread Exception: " + throwable.getMessage());
        clients.remove(thread);
    }

    private void sendToAllAuthenticatedClients(String msg) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            client.sendMessage(msg);
        }
    }

    private void handleAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Library.TYPE_BCAST_CLIENT:
                sendToAllAuthenticatedClients(msg);
                break;
            case Library.TYPE_BROADCAST:
//                sendToAllAuthenticatedClients(msg);
                break;
            case Library.MSG_FORMAT_ERROR:
                client.sendMessage(Library.getMsgFormatError(msg));
                break;
            default:
                //throw new RuntimeException("You are trying to hack me! " + msg);
                client.sendMessage(Library.getMsgFormatError(msg));
        }
    }

    private void handleNonAuthMessage(ClientThread newClient, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        if (arr.length != 3 || !arr[0].equals(Library.AUTH_REQUEST)) {
            newClient.msgFormatError(msg);
            return;
        }
        String login = arr[1];
        String password = arr[2];
        String nickname = SqlClient.getNickname(login, password);
        if (nickname == null) {
            writeLog(String.format("Invalid login attempt: l='%s', p='%s'", login, password));
            newClient.authFail();
            return;
        }
        ClientThread oldClient = findClientByNick(nickname);
        newClient.authAccept(nickname);
        if (oldClient == null) {
            sendToAllAuthenticatedClients(Library.getTypeBroadcast("Server", nickname + " connected!"));
        } else {
            oldClient.reconnect();
            clients.remove(oldClient);
        }
        sendToAllAuthenticatedClients(Library.getUserList(getUsers()));

    }

    private String getUsers() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if(!client.isAuthorized()) continue;
            sb.append(client.getNickname()).append(Library.DELIMITER);
        }
        return sb.toString();
    }

    private ClientThread findClientByNick(String nick) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if(!client.isAuthorized()) continue;
            if (client.getNickname().equals(nick))
                return client;
        }
        return null;
    }
}
