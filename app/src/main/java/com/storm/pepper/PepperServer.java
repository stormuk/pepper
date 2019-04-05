package com.storm.pepper;

import com.storm.posh.Planner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class PepperServer {
    private static final String TAG = Planner.class.getSimpleName();
    private PepperLog pepperLog;
    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    Thread serverThread = null;

    public static final int SERVER_PORT = 3003;

    public PepperServer(PepperLog pepperLog) {
        this.pepperLog = pepperLog;
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
        logIpAddress();
    }

    public void logIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip = String.format("Server running at: %s:%d", inetAddress.getHostAddress(), SERVER_PORT);
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip = "Something Wrong! " + e.toString();
        }

        pepperLog.appendLog(TAG, ip, false);
    }

    public void sendMessage(final String message) {
        try {
            if (null != tempClientSocket) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter out = null;
                        try {
                            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(tempClientSocket.getOutputStream())),true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        out.println(message);
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
                pepperLog.appendLog(TAG, "Error Starting Server : " + e.getMessage(), false);
            }
            if (null != serverSocket) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                        pepperLog.appendLog(TAG,"Error Communicating to Client :" + e.getMessage(), false);
                    }
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
                pepperLog.appendLog(TAG, "Error Connecting to Client!!", false);
            }
            pepperLog.appendLog(TAG, "Connected to Client!!", false);
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    if (null == read || "Disconnect".contentEquals(read)) {
                        Thread.interrupted();
                        read = "Client Disconnected";
                        pepperLog.appendLog(TAG, "Client : " + read, false);
                        break;
                    }
                    pepperLog.appendLog(TAG,"Client : " + read, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    protected void destroy() {
        if (null != serverThread) {
            sendMessage("Disconnect");
            serverThread.interrupt();
            serverThread = null;
        }
    }
}
