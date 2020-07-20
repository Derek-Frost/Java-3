package ru.gb.jtwo.chat.server.gui;

import ru.gb.jtwo.chat.server.core.ChatServer;
import ru.gb.jtwo.chat.server.core.ChatServerListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, ChatServerListener {

    private static final int POS_X = 800;
    private static final int POS_Y = 200;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 300;

    private final ChatServer chatServer = new ChatServer(this);
    private final JButton btnStart = new JButton("Start");
    private final JButton btnStop = new JButton("Stop");
    private final JPanel panelTop = new JPanel(new GridLayout(1, 2));
    private final JTextArea log = new JTextArea();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ServerGUI();
            }
        });
    }

    private ServerGUI() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Thread.setDefaultUncaughtExceptionHandler(this);
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setTitle("Server GUI");
        setResizable(false);
        setAlwaysOnTop(true);

        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(log);
        btnStop.addActionListener(this);
        btnStart.addActionListener(this);
        panelTop.add(btnStart);
        panelTop.add(btnStop);
        add(panelTop, BorderLayout.NORTH);
        add(scrollLog, BorderLayout.CENTER);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnStart) {
            chatServer.start(8189);
        } else if (src == btnStop) {
            chatServer.stop();
        } else {
            throw new RuntimeException("Unknown source: " + src);
        }
    }

    private void putLog(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + '\n');
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
       e.printStackTrace();
       StackTraceElement[] ste = e.getStackTrace();
       String msg = String.format("Exception in thread %s: %s: %s\n\t at %s",
               t.getName(),
               e.getClass().getCanonicalName(),
               e.getMessage(),
               ste[0]
       );
//       JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        putLog(msg);
    }

    @Override
    public void onServerMessage(String msg) {
        putLog(msg);
    }
}
