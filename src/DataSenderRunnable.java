import java.io.*;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DataSenderRunnable implements Runnable {

    private Socket socket;
    private Scanner userIn;
    private Client client;
    private String currentUsername = "";
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

            if (currentUsername.equals("")) {
                System.out.println("Please enter a username: ");
                currentUsername = userIn.nextLine();
            }

            int tries = 2;
            while (!usernameAccepted && !kill) {
                if (tries == 0) {
                    client.reconnect();
                    client.killProcesses();
                } else {
                    sendMessage("HELO " + currentUsername);

                    latch = new CountDownLatch(1);
                    client.setWaitingForData();
                    latch.await(1000, TimeUnit.MILLISECONDS);
                    tries--;
                }
            }

            //TODO: AFMAKEN
            while(!kill) {
                String message = userIn.nextLine();

                int broadcasttries = 2;
                while (!dataAccepted && !kill) {
                    if (broadcasttries == 0) {
                        client.reconnect();
                        client.killProcesses();
                    } else {
                        sendMessage("BCST " + message);

                        latch = new CountDownLatch(1);
                        client.setWaitingForData();
                        latch.await(1000, TimeUnit.MILLISECONDS);
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
            System.out.println("ERROR");
        }
    }

    public void receivedData(String data) {
        System.out.println("DATASENDER GOT DATA: " + data);
        if (data.equals("+OK " + currentUsername)) {
            usernameAccepted = true;
            latch.countDown();
        } else if (data.equals("+OK")) {
            dataAccepted = true;
            latch.countDown();
        }
    }

    public void kill() {
        this.kill = true;
    }
}