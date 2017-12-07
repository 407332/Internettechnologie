import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;


public class DataGetterRunnable implements Runnable {

    private Socket socket;
    private BufferedReader bufferedReader;
    private Client client;

    private boolean waitingForData;
    private boolean kill;

    private CountDownLatch latch;
    private String connectionResult;


    public DataGetterRunnable(Client client, Socket socket) {
        this.client = client;
        this.socket = socket;
    }

    public DataGetterRunnable(Socket socket, CountDownLatch latch) {
        this.socket = socket;
        this.latch = latch;
        connectionResult = "";
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while (!kill) {
                String line = bufferedReader.readLine();
                if(latch != null){
                    connectionResult = line;
                    latch.countDown();
                    kill = true;
                }else {
                    if (waitingForData && line != null) {
                        client.confirmData(line);
                        waitingForData = false;
                    }
                    else if (line == null) {
                        client.killProcesses();
                    }else if (line.contains("BCST")) {
                        System.out.println(line);
                    }
                }
            }
        }catch (SocketException se){

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if(latch ==  null) {
            //System.out.println("killing getter");
        }
    }

    public void setWaitingForData() {
        this.waitingForData = true;
    }

    public void kill() {
        this.kill = true;
    }

    public String getConnectionResult() {
        return connectionResult;
    }
}
