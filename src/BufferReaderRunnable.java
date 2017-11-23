import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

public class BufferReaderRunnable implements Runnable {

    private String result;
    private BufferedReader bufferedReader;
    private CountDownLatch latch;

    public BufferReaderRunnable(BufferedReader bufferedReader, CountDownLatch latch){
        this.bufferedReader = bufferedReader;
        this.result = "";
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            result = this.bufferedReader.readLine();
            latch.countDown();
        }catch (SocketException se){
          //nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getResult() {
        return result;
    }
}
