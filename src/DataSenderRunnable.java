import java.io.*;
import java.io.IOException;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DataSenderRunnable implements Runnable {

    private Socket socket;
    private Scanner userIn;
    private Client client;
    private CountDownLatch latch;

    private boolean kill;
    private boolean usernameAccepted;
    private boolean dataAccepted;


    public DataSenderRunnable(Client client, Socket socket) {
        this.client = client;
        this.socket = socket;
        this.userIn = new Scanner(System.in);
    }

    @Override
    public void run() {
        try {

            while (!usernameAccepted && !kill) {
                if (client.getCurrentUsername().equals("")) {
                    System.out.println("Please enter a username: ");
                    boolean goodName = false;
                    String username = "";
                    while (!goodName) {
                        while (!client.hasMessages() && !kill) {
                            Thread.sleep(500);
                        }
                        if (client.hasMessages()) {
                            username = client.getNextMessage();
                            if (!username.contains(" ") && username.length() > 2 && username.matches("[a-zA-Z0-9_]{3,14}")) {
                                goodName = true;
                            } else {
                                System.out.println("Username not accepted try again.");
                            }
                        }
                    }
                    client.setCurrentUsername(username);
                } else {
                    // System.out.println("sending login username: " + client.getCurrentUsername());
                    sendMessage("HELO " + client.getCurrentUsername());

                    latch = new CountDownLatch(1);
                    client.setWaitingForData();
                    latch.await(2000, TimeUnit.MILLISECONDS);
                }
            }


            while (!kill) {
                String message = "";
                if (client.getLastMessage().equals("")) {
                    while (!client.hasMessages() && !kill) {
                        Thread.sleep(500);
                    }
                    if (client.hasMessages()) {
                        message = client.getNextMessage();
                        client.setLastMessage(message);
                    }
                } else {
                    message = client.getLastMessage();
                }

                //int broadcasttries = 4;
                dataAccepted = false;
                while (!dataAccepted && !kill) {
                    // System.out.println("sending message: " + message + "...");
<<<<<<< HEAD
                    if (message.equals("QUIT") || message.equals("LSTUS") || message.startsWith("MSG") || message.startsWith("LSTGRP") || message.startsWith("JNGRP")) {
=======
                    if (message.equals("QUIT") || message.equals("LSTUS") || message.startsWith("MSG") || message.equals("LSTGRP")) {
>>>>>>> 934d2763ddcdf710726281593b18a791e574a56d
                        sendMessage(message);
                    } else if (message.startsWith("MKGRP")) {
                        String[] parse = message.split(" ");
                        boolean isValidGroupname = false;
                        if (parse.length > 1){
                            String groupname = parse[1];
                            isValidGroupname = groupname.matches("[a-zA-Z0-9_]{3,15}");
                        }else{
                            System.out.println("Try again");
                            client.setLastMessage("");
                            break;
                        }
                        if (isValidGroupname) {
                            sendMessage(message);
                        } else {
                            System.out.println("Groupname not accepted");
                        }
                    }else if(message.startsWith("BCGRP")){
                        String[] parse = message.split(" ");
                        boolean isValidGroupname = false;
                        if (parse.length > 2){
                            String groupname = parse[1];
                            isValidGroupname = groupname.matches("[a-zA-Z0-9_]{3,15}");
                        }else{
                            System.out.println("invalid message");
                            client.setLastMessage("");
                            break;
                        }
                        if (isValidGroupname) {
                            sendMessage(message);
                        } else {
                            System.out.println("Groupname not accepted");
                            client.setLastMessage("");
                        }

                    }else{
                        sendMessage("BCST " + message);
                    }
                    latch = new CountDownLatch(1);
                    client.setWaitingForData();
                    latch.await(1000, TimeUnit.MILLISECONDS);
                }
            }

        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        //System.out.println("killing sender");
    }

    public void sendMessage(String message) {
        try {
            OutputStream outputStream = socket.getOutputStream();

            PrintWriter writer = new PrintWriter(outputStream);
            writer.println(message);
            writer.flush();
        } catch (IOException ioe) {

        }
    }

    public void receivedData(String data) {
//        System.out.println(data);
        if (data.equals("+OK " + client.getCurrentUsername())) {
            usernameAccepted = true;
        } else if (data.startsWith("+OK")) {
            client.setLastMessage("");
            dataAccepted = true;
            if (data.length() > 3) {
                System.out.println(data.substring(4));
            }
        }
        else if (data.equals("-ERR user already logged in")) {
            System.out.println("Username Already loggedin.");
            client.setCurrentUsername("");
        } else if (data.equals("-ERR Username doesn't exist.")) {
            System.out.println("Username Doesn't exist");
            dataAccepted = true;
            client.setLastMessage("");
        } else if(data.equals("-ERR Group doesn't exist.")) {
            System.out.println("Group Doesn't exist");
            dataAccepted = true;
            client.setLastMessage("");
        }else if (data.equals("+OK Goodbye")) {
            System.out.println("Quitting succesfull.");
            dataAccepted = true;
            client.stopConnecting();
            client.killProcesses();
        } else if (data.equals("-ERR groupname already exists")){
            System.out.println("Group already exists");
            dataAccepted = true;
            client.setLastMessage("");
        }else if (data.startsWith("+OK Groups:")){
            System.out.println(data);
        }
        latch.countDown();
    }

    public void kill() {
        this.kill = true;
    }
}