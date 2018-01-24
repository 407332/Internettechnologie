import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.crypto.Cipher;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
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

    private PrivateKey privateKey;
    private PublicKey publicKey;


    public DataSenderRunnable(Client client, Socket socket) {
        this.client = client;
        this.socket = socket;
        this.userIn = new Scanner(System.in);
    }

    @Override
    public void run() {
        privateKey = client.getPrivateKey();
        publicKey = client.getPublicKey();
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
                    if (message.equals("QUIT") || message.equals("LSTUS") || message.startsWith("LSTGRP") || message.startsWith("JNGRP")) {
                        sendMessage(message);
                    } else if (message.startsWith("MSG")) {
                        String[] parse = message.split(" ");
                        if (parse.length > 2) {
                            String recipient = parse[1];
                            message.replace("MSG " + recipient, "");
                            sendPrivateMessage(recipient, message);
                        } else{
                            System.out.println("Invalid message");
                        }
                    } else if (message.startsWith("MKGRP")) {
                        String[] parse = message.split(" ");
                        boolean isValidGroupname = false;
                        if (parse.length > 1) {
                            String groupname = parse[1];
                            isValidGroupname = groupname.matches("[a-zA-Z0-9_]{3,15}");
                        } else {
                            System.out.println("Try again");
                            client.setLastMessage("");
                            break;
                        }
                        if (isValidGroupname) {
                            sendMessage(message);
                        } else {
                            System.out.println("Groupname not accepted");
                            client.setLastMessage("");
                        }
                    } else if (message.startsWith("BCGRP")) {
                        String[] parse = message.split(" ");
                        boolean isValidGroupname = false;
                        if (parse.length > 2) {
                            String groupname = parse[1];
                            isValidGroupname = groupname.matches("[a-zA-Z0-9_]{3,15}");
                        } else {
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
                    } else if (message.startsWith("LVGRP")) {
                        String[] parse = message.split(" ");
                        boolean isValidGroupname = false;
                        if (parse.length > 1) {
                            String groupname = parse[1];
                            isValidGroupname = groupname.matches("[a-zA-Z0-9_]{3,15}");
                        } else {
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
                    } else if (message.startsWith("KICK")) {
                        String[] parse = message.split(" ");
                        boolean isValidGroupname = false;
                        boolean isValidUsername = false;
                        if (parse.length > 2) {
                            String groupname = parse[1];
                            String username = parse[2];
                            isValidGroupname = groupname.matches("[a-zA-Z0-9_]{3,15}");
                            isValidUsername = username.matches("[a-zA-Z0-9_]{3,14}");
                        } else {
                            System.out.println("invalid ");
                            client.setLastMessage("");
                            break;
                        }
                        if (isValidGroupname && isValidUsername) {
                            sendMessage(message);
                        } else {
                            System.out.println("Groupname or Username not accepted");
                            client.setLastMessage("");
                        }
                    } else if (message.startsWith("TRNSFR")) {
                        String[] parse = message.split(" ");
                        String file = "";
                        String username = "";
                        boolean isValidUsername = false;
                        if (parse.length > 2) {
                            username = parse[1];
                            file = parse[2];
                            isValidUsername = username.matches("[a-zA-Z0-9_]{3,14}");
                        } else {
                            System.out.println("invalid");
                            client.setLastMessage("");
                            break;
                        }
                        if (isValidUsername && !file.equals("")) {
                            try {
                                File f = new File(file);
                                sendFile(f, username);
                                file = "";
                                client.setLastMessage("");
                                break;
                            } catch (IOException io) {
                                client.setLastMessage("");
                                System.out.println("A problem occured with the file Transfer");
                            }
                        } else {
                            System.out.println("Username not accepted");
                            client.setLastMessage("");
                        }
                    } else {
                        sendMessage("BCST " + message);
                    }
                    latch = new CountDownLatch(1);
                    client.setWaitingForData();
                    latch.await(1500, TimeUnit.MILLISECONDS);
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

    public void sendPrivateMessage(String recepient, String message) {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            byte[] encryptmsg = encrypt(privateKey, message);
            if (encryptmsg != null) {

                byte[] size = (encryptmsg.length + "").getBytes();
                size = Arrays.copyOf(size, 4096);

                PrintWriter writer = new PrintWriter(dos);
                writer.println("MSG " + recepient);
                writer.flush();

                dos.write(size);
            }
        } catch (IOException ioe) {

        }
    }

    public void sendFile(File file, String username) throws IOException {
        sendMessage("TRNSFR " + username);

        FileInputStream fis = new FileInputStream(file);
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        byte[] buffer = new byte[4096];

        byte[] name = file.getName().getBytes();
        name = Arrays.copyOf(name, 4096);
        dos.write(name);

        byte[] size = (file.length() + "").getBytes();
        size = Arrays.copyOf(size, 4096);
        dos.write(size);

        while (fis.read(buffer) > 0) {
            dos.write(buffer);
        }
        fis.close();
    }

    public void receivedData(String data) {
//        System.out.println(data);
        if (data.equals("+OK " + client.getCurrentUsername())) {
//            sendPublicKey();
            usernameAccepted = true;
        } else if (data.startsWith("+OK")) {
            acceptance();
            if (data.length() > 3) {
                System.out.println(data.substring(4));
            }
        } else if (data.equals("-ERR user already logged in")) {
            System.out.println("Username Already loggedin.");
            client.setCurrentUsername("");
        } else if (data.equals("-ERR Username doesn't exist.")) {
            System.out.println("Username Doesn't exist");
            acceptance();
        } else if (data.equals("-ERR Group doesn't exist.")) {
            System.out.println("Group Doesn't exist");
            acceptance();
        } else if (data.equals("+OK Goodbye")) {
            System.out.println("Quitting succesfull.");
            dataAccepted = true;
            client.stopConnecting();
            client.killProcesses();
        } else if (data.equals("-ERR groupname already exists")) {
            System.out.println("Group already exists");
            acceptance();
        } else if (data.equals("-ERR already joined this group.")) {
            System.out.println("You already joined this group.");
            acceptance();
        } else if (data.equals("-ERR not in this group")) {
            System.out.println("You are not in this group.");
            acceptance();
        } else if (data.equals("-ERR User is not in this group")) {
            System.out.println("The User is not in this group.");
            acceptance();
        } else if (data.equals("-ERR You are not the owner")) {
            System.out.println("You are not the owner of that group.");
            acceptance();
        } else if (data.equals("+OK kicked From group")) {
            System.out.println(data.substring(4));
            acceptance();
        } else if (data.equals("-ERR You cannot kick yourself")) {
            System.out.println("You cannot kick yourself.");
            acceptance();
        } else if (data.equals("-ERR Failed to receive file")) {
            acceptance();
            System.out.println("File transfer failed");
        } else if (data.startsWith("+OK Groups:")) {
            System.out.println(data);
        }
        latch.countDown();
    }

    private void sendPublicKey() throws IOException{


    }

    public static byte[] encrypt(PrivateKey privateKey, String message) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return cipher.doFinal(message.getBytes());
        } catch (Exception e) {
            return null;
        }
    }

    public void kill() {
        this.kill = true;
    }

    private void acceptance() {
        dataAccepted = true;
        client.setLastMessage("");
    }
}