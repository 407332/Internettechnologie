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
                if (latch != null) {
                    connectionResult = line;
                    latch.countDown();
                    kill = true;
                } else {
                    if (waitingForData && line != null) {
                        client.confirmData(line);
                        waitingForData = false;
                    } else if (line == null) {
                        client.killProcesses();
                    } else if (line.startsWith("WHISPER")) {
                        String[] split = line.split(" ");
                        System.out.println("[" + split[1] + "](WHISPER) " + line.substring(split[0].length() + split[1].length() + 2));
                    } else if (line.startsWith("TRNSFR")) {
                        String[] split = line.split(" ");
                        try {
                            if (split.length > 1){
                            System.out.println("getting a file from " + split[2]);
                            receivedata();
                            }else {
                                System.out.println("Filetransfer had a problem");
                            }
                        } catch (IOException io) {
                            System.out.println("Filetransfer had a problem");
                        }
                    }
                    if (line.startsWith("BCST")) {
                        System.out.println(line.substring(line.split(" ")[0].length() + 1));
                    }
                }
            }
        } catch (SocketException se) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (latch == null) {
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

    public void receivedata() throws IOException {
        DataInputStream is = new DataInputStream(socket.getInputStream());
        byte[] buffer = new byte[4096];

        is.read(buffer, 0, buffer.length);
        String file = new String(buffer).trim();

        is.read(buffer, 0, buffer.length);
        int filesize = Integer.parseInt(new String(buffer).trim());

        FileOutputStream fos = new FileOutputStream(file);
        int read = 0;
        int totalRead = 0;
        int remaining = filesize;
        while ((read = is.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            System.out.println("read " + totalRead + " bytes.");
            fos.write(buffer, 0, read);
        }
        if (totalRead == filesize){
            System.out.println("the file has been received");
            fos.close();
        }else {
            System.out.println("Filetransfer had a problem");
            fos.close();
        }
    }
}
