import java.io.*;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
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
            userIn = new Scanner(System.in);

            if (client.getCurrentUsername().equals("")) {
                System.out.println("Please enter a username: ");
                boolean goodName = false;
                String username = "";
                while(!goodName) {
                     username = userIn.nextLine();
                    if(!username.contains(" ") && username.length()>2){
                        goodName = true;
                    }else{
                        System.out.println("Username not accepted try again.");
                    }
                }
                client.setCurrentUsername(username);
            }

            int tries = 4;
            while (!usernameAccepted && !kill) {
                if (tries == 0) {
                    System.out.println("tried login 4 times, reconnecting");
                    client.killProcesses();
                    client.reconnect();
                } else {
                    System.out.println("sending login username: " + client.getCurrentUsername());
                    sendMessage("HELO " + client.getCurrentUsername());

                    latch = new CountDownLatch(1);
                    client.setWaitingForData();
                    latch.await(2000, TimeUnit.MILLISECONDS);
                    tries--;
                }
            }

            while (!kill) {
                String message = "";
                if (client.getLastDroppedMessage().equals("")) {
                    boolean messageRead = true;
                    while(messageRead) {
                        messageRead = false;
                        try {
                            message = userIn.nextLine();
                        } catch (NoSuchElementException nsee) {
                            System.out.println("could not read your last message.");
                            userIn.close();
                            userIn = new Scanner(System.in);
                            messageRead = true;
                        }
                    }
                } else {
                    message = client.getLastDroppedMessage();
                }

                int broadcasttries = 4;
                dataAccepted = false;
                while (!dataAccepted && !kill) {
                    if(broadcasttries == 0){
                        if(message.equals("QUIT")) {
                            client.stopConnecting();
                        }else {
                            System.out.println("tried message 4 times, reconnecting");
                            client.setLastDroppedMessage(message);
                            client.killProcesses();
                            client.reconnect();
                        }
                    } else {
                        System.out.println("sending message: " + message + "...");
                        if(message.equals("QUIT")){
                            sendMessage(message);
                        }else {
                            sendMessage("BCST " + message);
                        }
                        latch = new CountDownLatch(1);
                        client.setWaitingForData();
                        latch.await(2000, TimeUnit.MILLISECONDS);
                        broadcasttries--;
                    }
                }
            }

        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        System.out.println("killing sender");
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
         System.out.println("DATASENDER GOT DATA: " + data);
        if (data.equals("+OK " + client.getCurrentUsername())) {
            usernameAccepted = true;
            latch.countDown();
        } else if (data.equals("+OK")) {
            client.setLastDroppedMessage("");
            dataAccepted = true;
            latch.countDown();
        } else if (data.equals("-ERR user already logged in")) {
            System.out.println("Username Already loggedin, serverside error. will keep trying");
        }else if (data.equals("+OK Goodbye")){
            System.out.println("Quitting succesfull.");
            latch.countDown();
            dataAccepted = true;
            client.stopConnecting();
            client.killProcesses();
        }
    }

    public void kill() {
        userIn.close();
        this.kill = true;
    }
}