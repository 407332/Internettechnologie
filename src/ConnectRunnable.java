import org.omg.PortableServer.THREAD_POLICY_ID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ConnectRunnable implements Runnable {

    private final int TIMEOUT_TRIES = 7;

    private Socket socket;
    private boolean isConnected;
    private boolean hasConnectedBefore;
    private Client client;
    private boolean keepConnecting = true;

    public ConnectRunnable (Client client){
        this.client = client;
    }

    @Override
    public void run() {
        int connectingCounter = TIMEOUT_TRIES;
        while (keepConnecting) {
            if (!isConnected) {
                try {
                    String welcomeLine = "";
                    while (!welcomeLine.equals("HELO Welkom to WhatsUpp!") && keepConnecting) {
                        connectingCounter --;
                        if(connectingCounter == 0){
                            keepConnecting = false;
                            client.killUserInput();
                        }else {
                            //System.out.println("connecting...");
                            if (socket != null) {
                                socket.close();
                                socket = null;
                            }
                            this.socket = new Socket("LOCALHOST", 1337);

                            CountDownLatch latch = new CountDownLatch(1);

                            DataGetterRunnable getterRunnable = new DataGetterRunnable(socket,latch);
                            Thread getterThread = new Thread(getterRunnable);
                            getterThread.start();

                            latch.await(500, TimeUnit.MILLISECONDS);

                            welcomeLine = getterRunnable.getConnectionResult();
                        }
                    }
                    if(welcomeLine.equals("HELO Welkom to WhatsUpp!")) {
                        connectingCounter = TIMEOUT_TRIES;
                        isConnected = true;
                        client.connected();

                        if(!hasConnectedBefore){
                            System.out.println(welcomeLine);
                            hasConnectedBefore = true;
                        }
                    }else{
                        System.out.println("Can't reach server shutting down....");
                    }
                } catch (IOException ioe) {
                    ioe.getStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(500);
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

    public void kill(){
        keepConnecting = false;
    }
}
