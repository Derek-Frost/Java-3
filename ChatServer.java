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

/*

1. Почему мы не используем Java Console для дебага? Вместо этого логируем
пока события внутри окон?

2. ClientGUI: зачем нам showException? мы же можем внутри окна чата известить,
что мол сервер "ушел отдыхать по непонятным причинам" и др ошибки.

3. ChatServer: Connection reset сообщение прилетает, при остановке клиента.
Я так понимаю хотим Connection lost вместо этого?

1. Я решил понять, как происходит передача сообщения, и весь его путь мне понятен до
того момента, когда мы решаем: чье это сообщение, и кому оно будет отправлено. Мое
непонимание заключается в том, что в случае через метод handleAuthMessage(client, msg);
вызывается client.sendMessage(msg); а из этого метода мы (как я понял) не можем попасть
в событие onReceiveString, чтобы записать сообщение в лог клиента. Или можем? Как
именно здесь выражено взаимодействие сокета клиента и сокета сервера?

Не до конца понятен момент с метадом handleNonAuthMessage(client, msg);, а именно:
авторизация не автроизованного пользователя с учетом того, что подключения без
авторизации произойти не может (пока что). И ведь должна же быть регистрация? т.к.
подключится с другим логином не получится и тем более выбрать себе ник.

Проблема с дисконектом. Он не проходит мирно. Вылетают эксепшины(стек-трейсы),:
java.net.SocketException: Socket closed at .....

Не понимаю смысл chat_library. Вот прямо не понимаю какую проблему он решает.
Может быть и имею представление, но только очень отдаленно.

1) При нажатии кнопки Disconect на клиенте ничего не происходит,
он остается соединенным. Что случилось с программой?

2) Для чего это сделано и что это дает? Что это за метод потока:
Thread.setDefaultUncaughtExceptionHandler(this); //ClientGUI:str78

3) Что это за метод и что он делает? setLocationRelativeTo(null); //ClientGUI:str78

4) Для чего используется эта переменная? Что делает эта заготовка в методе на 164
строке? boolean shownIoErrors = false; //ClientGUI:str152

5) Что это за метод и что значит его параметр: System.exit(1) //ClientGUI:str120)

6) Что это за странная конструкция и что она дает (делает):
if ("".equals(msg)) return; //ClientGUI:str152 and str171
И почему здесь есть return хотя оба этих метода void типа? (Это похоже на прерывание
выполнения следующих строк метода если строка)

7) Я так понимаю, это метод превращения строки в массив строк стандартным методом
split, при этом разбивка идет по символу §?
String[] arr = msg.split(Library.DELIMITER); //ChatServer:str136

8) Что это за класс такой Vector<SocketThread> clients = new Vector<>(); //ChatServer:str20
Почему нельзя здесь использовать другой тип (массив или список), почему клиенты
сохраняются именно в Векторе и какие преимущества это дает и что диктует делать именно так?

9) Для чего мы делаем приведение clients.get(i) и thread к классу ClientThread?
Изначально как я понимаю обе эти переменные являются наследниками класса Tread?
Что у них общего с ClientThread?
ClientThread client = (ClientThread) thread;
ClientThread client = (ClientThread) clients.get(i); //ChatServer:str125

10) Что такое connection = DriverManager.getConnection; //SqlClient:str13 ?
Это поток для подключения к базе данных или просто еденичный метод?

11) Что такое statement = connection.createStatement(); //SqlClient:str14 ?
Зачем мы его создаем здесь, если потом используем только один раз в методе getNickname

Какие еще программы, кроме БД, могут подключаться к чату? Можно для этого использовать
обычный текстовый файл?

Какие вообще обычно программы подкючаются, можно найти где-то как именно в IntelliJ IDEA?

1) Вопрос не по чату, а в целом отношениям клиента и сервера: Клиент с сервером всегда
по одному сокету общаются или их может быть несколько для разных целей?

2) Расскажите про flush() подробнее. Ещё  сильно путают методы stop, close, interupt,
start, run, но это серее реплика, а не вопрос.

3) Как выбирать какую коллекцию использовать (Vector, ArrayList и тд.), почему в чате
мы используем Vector?

3) То как мы форматируем наши сообщения в Library, это «авторская пунктуация» или есть
какие то стандарты?

4) Возможно ли переписать наш ClientGui под android?

* */

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
