import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ConnectRunnable implements Runnable {

    private Socket socket;
    private boolean isConnected;
    private boolean hasConnectedBefore;
    private Client client;

    public ConnectRunnable (Client client){
        this.client = client;
    }

    @Override
    public void run() {
        while (true) {
            while (!isConnected) {
                try {
                    String welcomeLine = "";
                    while (!welcomeLine.equals("HELO Welkom to WhatsUpp!")) {
                        System.out.println("connecting...");
                        if (socket != null) {
                            socket.close();
                            socket = null;
                        }
                        this.socket = new Socket("LOCALHOST", 1337);

                        InputStream inputStream = socket.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                        CountDownLatch latch = new CountDownLatch(1);

                        BufferReaderRunnable bufferReaderRunnable = new BufferReaderRunnable(bufferedReader, latch);
                        Thread bufferReaderThread = new Thread(bufferReaderRunnable);
                        bufferReaderThread.start();

                        latch.await(1000, TimeUnit.MILLISECONDS);

                        welcomeLine = bufferReaderRunnable.getResult();
                    }
                    isConnected = true;
                    client.connected();

                    if(!hasConnectedBefore){
                        System.out.println(welcomeLine);
                        hasConnectedBefore = true;
                    }

                } catch (IOException ioe) {
                    ioe.getStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void reconnect() {
        isConnected = false;
    }
}
