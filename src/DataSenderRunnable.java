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
                    if (message.equals("QUIT")) {
                        sendMessage(message);
                    } else {
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
        if (data.equals("+OK " + client.getCurrentUsername())) {
            usernameAccepted = true;
            latch.countDown();
        } else if (data.equals("+OK")) {
            client.setLastMessage("");
            dataAccepted = true;
            latch.countDown();
        } else if (data.equals("-ERR user already logged in")) {
            System.out.println("Username Already loggedin.");
            client.setCurrentUsername("");
            latch.countDown();
        }else if (data.equals("+OK Goodbye")) {
            System.out.println("Quitting succesfull.");
            latch.countDown();
            dataAccepted = true;
            client.stopConnecting();
            client.killProcesses();
        }
    }

    public void kill() {
        this.kill = true;
    }
}