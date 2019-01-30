//package com.storm.abod3;
//
//import android.os.Handler;
//import android.os.Message;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.Socket;
//import java.util.concurrent.ScheduledExecutorService;
//
//public class NetworkTask implements Runnable {
//
//    private final ScheduledExecutorService serverPingerScheduler;
//    private int port;
//    private String ipAddress;
//    private Socket socket = null;
//    private BufferedReader input = null;
//    private String response = null;
//    private static final int SERVER_RESPONSE = 1;
//    private Handler handler = null;
//
//    public NetworkTask(String ipAddress, int port, Handler generalHandler, ScheduledExecutorService serverPingerScheduler){
//        this.port = port;
//        this.ipAddress = ipAddress;
//        this.handler = generalHandler;
//        this.serverPingerScheduler = serverPingerScheduler;
//    }
//
//    @Override
//    public void run() {
//
//        try {
//            socket = new Socket(ipAddress, port);
//            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//            System.out.println("Connection Successful!");
//
//            String line;
//            while ((line = input.readLine()) != null) {
//               // System.out.println(line);
//                        Message message = new Message();
//                        message.what = SERVER_RESPONSE;
//                        message.obj = line;
//                        handler.sendMessage(message);
//            }
//
//
////            final Runnable pinger = new Runnable() {
////
////                @Override
////                public void run() {
////                    try {
////                        response = input.readLine();
////                        Message message = new Message();
////                        message.what = SERVER_RESPONSE;
////                        message.obj = response;
////                        handler.sendMessage(message);
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
////                }
////            };
////
////            serverPingerScheduler.scheduleAtFixedRate(pinger, 50, 50, TimeUnit.MILLISECONDS);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
////        try {
////            while (!Thread.currentThread().isInterrupted()) {
////
////                Thread.sleep(pollInterval);
////
////                try {
////                    response = input.readLine();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////                if(!response.equals("doNothing")){
////                    Message message = new Message();
////                    message.what = SERVER_RESPONSE;
////                    message.obj = response;
////                    handler.sendMessage(message);
////                }else if(response.equals("No Robot Connected to Server!")){
////                    Message message = new Message();
////                    message.what = SERVER_RESPONSE;
////                    message.obj = response;
////                    handler.sendMessage(message);
////                }
////            }
////        } catch (InterruptedException threadE) {
////            try {
////                input.close();
////                socket.close();
////            } catch (IOException socketE) {
////                socketE.printStackTrace();
////            }
////            Log.i("NETWORK_TASK_THREAD", "NETWORK TASK closed gracefully!");
////        }
//
//        System.out.println("NetworkTask Runnable completed!");
//
//    }
//}
