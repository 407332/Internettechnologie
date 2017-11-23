import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Cient {

    private Socket socket;

    public static void main(String[] args) throws IOException {
        new Cient().run();
    }

    public void run(){
        connect();

        DataSenderRunnable dataSenderRunnable = new DataSenderRunnable(socket);
        Thread senderThread = new Thread(dataSenderRunnable);
        senderThread.start();

        DataGetterRunnable dataGetterRunnable = new DataGetterRunnable(socket,dataSenderRunnable);
        Thread getterThread = new Thread(dataGetterRunnable);
        getterThread.start();
    }



    private void connect() {
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

                BufferReaderRunnable bufferReaderRunnable = new BufferReaderRunnable(bufferedReader,latch);
                Thread bufferReaderThread = new Thread(bufferReaderRunnable);
                bufferReaderThread.start();

                latch.await(1000, TimeUnit.MILLISECONDS);

                welcomeLine = bufferReaderRunnable.getResult();
            }
            System.out.println(welcomeLine);

        } catch (IOException ioe) {
            ioe.getStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
